package saros.concurrent.watchdog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import saros.activities.ChecksumActivity;
import saros.activities.ChecksumErrorActivity;
import saros.activities.FileActivity;
import saros.annotations.Component;
import saros.editor.IEditorManager;
import saros.filesystem.IFile;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.monitoring.remote.RemoteProgressManager;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.util.CoreUtils;

/**
 * This class is responsible for two things:
 *
 * <ol>
 *   <li>Process checksums sent to us from the server by checking our locally existing files against
 *       them. See {@link #performCheck(ChecksumActivity)} If an inconsistency is detected the
 *       inconsistency state is set via the {@link IsInconsistentObservable}.
 *   <li>Send a ChecksumError to the host, if the user wants to recover from an inconsistency. See
 *       {@link #runRecovery}
 * </ol>
 *
 * This class both produces and consumes activities.
 */
@Component(module = "consistency")
public class ConsistencyWatchdogClient extends AbstractActivityProducer implements Startable {

  private static final Logger log = Logger.getLogger(ConsistencyWatchdogClient.class);

  private static final Random RANDOM = new Random();

  /**
   * boolean condition variable used to interrupt another thread from performing a recovery in
   * {@link #runRecovery}
   */
  private AtomicBoolean cancelRecovery = new AtomicBoolean();

  /** The number of files remaining in the current recovery session. */
  private AtomicInteger filesRemaining = new AtomicInteger();

  /** The id of the currently running recovery */
  private volatile String recoveryID;

  /**
   * Lock used exclusively in {@link #runRecovery} to prevent two recovery operations running
   * concurrently.
   */
  private Lock lock = new ReentrantLock();

  private final IsInconsistentObservable inconsistencyToResolve;

  private final IEditorManager editorManager;

  private final Set<IFile> filesWithWrongChecksums = new CopyOnWriteArraySet<>();

  private final RemoteProgressManager remoteProgressManager;

  private final ISarosSession session;

  public ConsistencyWatchdogClient(
      final ISarosSession session,
      final IsInconsistentObservable inconsistencyToResolve,
      final IEditorManager editorManager,
      final RemoteProgressManager remoteProgressManager) {
    this.session = session;
    this.inconsistencyToResolve = inconsistencyToResolve;
    this.editorManager = editorManager;
    this.remoteProgressManager = remoteProgressManager;
  }

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(ChecksumActivity checksumActivity) {
          performCheck(checksumActivity);
        }

        @Override
        public void receive(ChecksumErrorActivity error) {
          if (error.getSource().isHost()) {
            String myRecoveryID = recoveryID;
            if (myRecoveryID != null && myRecoveryID.equals(error.getRecoveryID())) {
              filesRemaining.set(0); // Host tell us he is done
            }
          }
        }

        @Override
        public void receive(FileActivity fileActivity) {
          if (!fileActivity.isRecovery()) return;

          int currentValue;
          while ((currentValue = filesRemaining.get()) > 0) {
            if (filesRemaining.compareAndSet(currentValue, currentValue - 1)) {
              break;
            }
          }
        }
      };

  @Override
  public void start() {
    inconsistencyToResolve.setValue(false);

    session.addActivityConsumer(consumer, Priority.ACTIVE);
    session.addActivityProducer(this);
  }

  @Override
  public void stop() {
    session.removeActivityConsumer(consumer);
    session.removeActivityProducer(this);

    filesWithWrongChecksums.clear();

    // abort running recoveries
    cancelRecovery.set(true);
  }

  /**
   * Returns a copy of the set of files for which the ConsistencyWatchdog has identified an
   * inconsistency
   */
  public Set<IFile> getFilesWithWrongChecksums() {
    return new HashSet<>(filesWithWrongChecksums);
  }

  /**
   * Start a consistency recovery by sending a checksum error to the host and waiting for his reply.
   * <br>
   * The <strong>cancellation</strong> of this method is <strong>not implemented</strong>, so
   * canceling the given monitor does not have any effect.
   *
   * @noSWT This method should not be called from SWT
   * @blocking This method returns after the recovery has finished
   * @client Can only be called on the client!
   */
  public void runRecovery(IProgressMonitor monitor) {

    ISarosSession currentSession = session;

    if (currentSession == null) return;

    if (currentSession.isHost())
      throw new IllegalStateException("Can only be called on the client");

    /*
     * FIXME this is to lazy, make sure every recovery is terminated when
     * the session ends
     */
    if (!lock.tryLock()) {
      log.error("Restarting Checksum Error Handling" + " while another operation is running");
      try {
        // Try to cancel currently running recovery
        do {
          cancelRecovery.set(true);
        } while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
      } catch (InterruptedException e) {
        log.error("Not designed to be interruptible");
        return;
      }

      currentSession = session;

      if (currentSession == null) {
        lock.unlock();
        return;
      }
    }

    // Lock has been acquired
    try {
      cancelRecovery.set(false);

      final List<IFile> handledFiles = new ArrayList<>(filesWithWrongChecksums);

      monitor.beginTask("Consistency recovery", handledFiles.size());

      final IProgressMonitor remoteProgress =
          remoteProgressManager.createRemoteProgressMonitor(
              currentSession.getRemoteUsers(), new NullProgressMonitor());

      recoveryID = getNextRecoveryID();

      filesRemaining.set(handledFiles.size());

      remoteProgress.beginTask(
          "Consistency recovery for user "
              + CoreUtils.determineUserDisplayName(currentSession.getLocalUser()),
          filesRemaining.get());

      fireActivity(
          new ChecksumErrorActivity(
              currentSession.getLocalUser(), currentSession.getHost(), handledFiles, recoveryID));

      try {
        // block until all inconsistencies are resolved
        int filesRemainingBefore = filesRemaining.get();
        int filesRemainingCurrently;
        while ((filesRemainingCurrently = filesRemaining.get()) > 0) {

          if (cancelRecovery.get() || monitor.isCanceled()) return;

          if (filesRemainingCurrently < filesRemainingBefore) {
            int worked = filesRemainingBefore - filesRemainingCurrently;

            // Inform others for progress...
            monitor.worked(worked);
            remoteProgress.worked(worked);

            filesRemainingBefore = filesRemainingCurrently;
          }
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            return;
          }
        }
      } finally {
        // Inform others for progress...
        remoteProgress.done();
      }

    } finally {
      monitor.done();
      lock.unlock();
    }
  }

  private String getNextRecoveryID() {
    return Long.toHexString(RANDOM.nextLong());
  }

  private boolean isInconsistent(ChecksumActivity checksum) {

    final IFile file = checksum.getResource();

    final boolean existsFileLocally = file.exists();

    if (!checksum.existsFile() && existsFileLocally) {
      /*
       * If the checksum tells us that the file does not exist at the
       * host, check whether we still have it. If it exists, we do have an
       * inconsistency
       */
      log.debug(
          "Inconsistency detected -> resource found that does not exist on host side: " + file);

      return true;
    }

    if (checksum.existsFile() && !existsFileLocally) {
      /*
       * If the checksum tells us that the file exists, but we do not have
       * it, it is an inconsistency as well
       */
      log.debug(
          "Inconsistency detected -> no resource found that does exist on host side: " + file);

      return true;
    }

    if (!checksum.existsFile() && !existsFileLocally) {
      log.debug("Ignoring checksum activity for file that does not exist on both sides: " + file);

      return false;
    }

    final String normalizedEditorContent = editorManager.getNormalizedContent(file);

    if (normalizedEditorContent == null) {
      log.debug("Inconsistency detected -> no editor content found for resource: " + file);

      return true;
    }

    if ((normalizedEditorContent.length() != checksum.getLength())
        || (normalizedEditorContent.hashCode() != checksum.getHash())) {

      log.debug(
          String.format(
              "Inconsistency detected -> %s L(%d %s %d) H(%x %s %x)",
              file.toString(),
              normalizedEditorContent.length(),
              normalizedEditorContent.length() == checksum.getLength() ? "==" : "!=",
              checksum.getLength(),
              normalizedEditorContent.hashCode(),
              normalizedEditorContent.hashCode() == checksum.getHash() ? "==" : "!=",
              checksum.getHash()));

      return true;
    }

    return false;
  }

  private void performCheck(ChecksumActivity checksumActivity) {

    final ISarosSession currentSession = session;

    if (currentSession == null) return;

    if (currentSession.hasWriteAccess()
        && !currentSession.getConcurrentDocumentClient().isCurrent(checksumActivity)) return;

    boolean changed;

    if (isInconsistent(checksumActivity)) {
      changed = filesWithWrongChecksums.add(checksumActivity.getResource());
    } else {
      changed = filesWithWrongChecksums.remove(checksumActivity.getResource());
    }

    if (!changed) return;

    // Update InconsistencyToResolve observable
    if (filesWithWrongChecksums.isEmpty()) {
      if (inconsistencyToResolve.getValue()) {
        log.info("All Inconsistencies are resolved");
      }
      inconsistencyToResolve.setValue(false);
    } else {
      if (!inconsistencyToResolve.getValue()) {
        log.info("Inconsistencies have been detected");
      }
      inconsistencyToResolve.setValue(true);
    }
  }
}
