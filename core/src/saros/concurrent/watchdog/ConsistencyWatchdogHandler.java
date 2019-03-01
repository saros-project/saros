package saros.concurrent.watchdog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;
import saros.activities.ChecksumActivity;
import saros.activities.ChecksumErrorActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.activities.SPath;
import saros.activities.TargetedFileActivity;
import saros.annotations.Component;
import saros.editor.IEditorManager;
import saros.filesystem.IFile;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.session.User;
import saros.synchronize.StartHandle;
import saros.synchronize.UISynchronizer;
import saros.util.ThreadUtils;

/**
 * This component is responsible for handling Consistency Errors on the host. It both produces and
 * consumes activities.
 */
@Component(module = "consistency")
public final class ConsistencyWatchdogHandler extends AbstractActivityProducer
    implements Startable {

  private static final Logger LOG = Logger.getLogger(ConsistencyWatchdogHandler.class);

  private final IEditorManager editorManager;

  private final ISarosSession session;

  private final UISynchronizer synchronizer;

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(ChecksumErrorActivity checksumError) {
          if (session.isHost()) triggerRecovery(checksumError);
        }
      };

  @Override
  public void start() {
    session.addActivityConsumer(consumer, Priority.ACTIVE);
    session.addActivityProducer(this);
  }

  @Override
  public void stop() {
    session.removeActivityConsumer(consumer);
    session.removeActivityProducer(this);
  }

  public ConsistencyWatchdogHandler(
      final ISarosSession session,
      final IEditorManager editorManager,
      final UISynchronizer synchronizer) {
    this.session = session;
    this.editorManager = editorManager;
    this.synchronizer = synchronizer;
  }

  private void triggerRecovery(final ChecksumErrorActivity checksumError) {

    LOG.debug("received Checksum Error: " + checksumError);

    /*
     * fork a thread as this is normally called from the UI thread and so we
     * would not be able to receive lock confirmations and so the blocking
     * the session would cause a timeout in the StopManager
     */

    /*
     * TODO ensure that only one recovery is run at the same time ? i.e use
     * a single ThreadWorker ?
     */
    ThreadUtils.runSafeAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            runRecovery(checksumError);
          }
        });
  }

  private void runRecovery(final ChecksumErrorActivity checksumError) throws CancellationException {

    List<StartHandle> startHandles = null;

    try {

      startHandles = session.getStopManager().stop(session.getUsers(), "Consistency recovery");

      recoverFiles(checksumError);

      /*
       * We have to start the StartHandle of the inconsistent user first
       * (blocking!) because otherwise the other participants can be
       * started before the inconsistent user completely processed the
       * consistency recovery.
       */

      // find the StartHandle of the inconsistent user
      StartHandle inconsistentStartHandle = null;
      for (StartHandle startHandle : startHandles) {
        if (checksumError.getSource().equals(startHandle.getUser())) {
          inconsistentStartHandle = startHandle;
          break;
        }
      }
      if (inconsistentStartHandle == null) {
        LOG.error("could not find start handle" + " of the inconsistent user");
      } else {
        // FIXME evaluate the return value
        inconsistentStartHandle.startAndAwait();
        startHandles.remove(inconsistentStartHandle);
      }
    } finally {
      if (startHandles != null) for (StartHandle startHandle : startHandles) startHandle.start();
    }
  }

  private void recoverFiles(final ChecksumErrorActivity checksumError) {

    synchronizer.syncExec(
        new Runnable() {
          @Override
          public void run() {

            for (final SPath path : checksumError.getPaths()) {

              recoverFile(checksumError.getSource(), path);

              // Tell the user that we sent all files
              fireActivity(
                  new ChecksumErrorActivity(
                      session.getLocalUser(),
                      checksumError.getSource(),
                      null,
                      checksumError.getRecoveryID()));
            }
          }
        });
  }

  /**
   * Recover a single file for the given user (that is either send the file or tell the user to
   * remove it).
   */
  private void recoverFile(final User from, final SPath path) {

    final IFile file = path.getFile();

    // Reset jupiter
    session.getConcurrentDocumentServer().reset(from, path);

    final User user = session.getLocalUser();

    if (!file.exists()) {
      // Tell the client to delete the file
      fireActivity(
          new TargetedFileActivity(
              user, from, Type.REMOVED, path, null, null, null, Purpose.RECOVERY));
      fireActivity(
          new ChecksumActivity(
              user,
              path,
              ChecksumActivity.NON_EXISTING_DOC,
              ChecksumActivity.NON_EXISTING_DOC,
              null));
      return;
    }

    String charset = null;

    try {
      charset = file.getCharset();
    } catch (IOException e) {
      LOG.error("could not determine encoding for file: " + file, e);
      return;
    }

    byte[] content;
    String text;

    try {
      text = editorManager.getContent(path);

      if (text == null) {
        LOG.error("could retrieve content of file: " + file);
        return;
      }

      content = text.getBytes(charset);
    } catch (UnsupportedEncodingException e) {
      LOG.error("could not decode file: " + file, e);
      return;
    }

    fireActivity(
        new TargetedFileActivity(
            user, from, Type.CREATED, path, null, content, charset, Purpose.RECOVERY));

    /*
     * Immediately follow up with a new checksum activity so that the remote
     * side can verify the recovered file.
     */

    DocumentChecksum checksum = new DocumentChecksum(path);
    checksum.update(text);

    fireActivity(new ChecksumActivity(user, path, checksum.getHash(), checksum.getLength(), null));
  }
}
