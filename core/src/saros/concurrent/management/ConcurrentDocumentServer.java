package saros.concurrent.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import saros.activities.ChecksumActivity;
import saros.activities.IActivity;
import saros.activities.JupiterActivity;
import saros.activities.QueueItem;
import saros.activities.SPath;
import saros.concurrent.jupiter.TransformationException;
import saros.filesystem.IFile;
import saros.repackaged.picocontainer.Startable;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;

/**
 * The ConcurrentDocumentServer is responsible for coordinating all JupiterActivities.
 *
 * <p>All clients (including the host!) will send their JupiterActivities to the
 * ConcurrentDocumentServer on the host, which transforms them (using Jupiter) and then sends them
 * to everybody else.
 *
 * <p>A ConcurrentDocumentServer exists only on the host!
 */
public class ConcurrentDocumentServer implements Startable {

  private static Logger log = Logger.getLogger(ConcurrentDocumentServer.class);

  private final ISarosSession sarosSession;

  private final JupiterServer server;

  private final ResourceActivityFilter resourceActivityFilter;

  /** {@link ISessionListener} for updating Jupiter documents on the host. */
  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void userStartedQueuing(final User user) {
          server.addUser(user);
        }

        @Override
        public void userLeft(final User user) {
          server.removeUser(user);
        }
      };

  public ConcurrentDocumentServer(final ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
    this.server = new JupiterServer(sarosSession);

    Consumer<IFile> deletedFileHandler =
        file -> {
          log.debug("Resetting jupiter server for " + file);
          server.removePath(file);
        };

    this.resourceActivityFilter = new ResourceActivityFilter(sarosSession, deletedFileHandler);
  }

  @Override
  public void start() {
    sarosSession.addListener(sessionListener);
    resourceActivityFilter.initialize();
  }

  @Override
  public void stop() {
    sarosSession.removeListener(sessionListener);
    resourceActivityFilter.dispose();
  }

  /**
   * Calls {@link ResourceActivityFilter#handleFileDeletion(IActivity)} and {@link
   * ResourceActivityFilter#handleFileCreation(IActivity)} with the given activity.
   *
   * @param activity the activity to handle
   */
  public void handleResourceChange(IActivity activity) {
    resourceActivityFilter.handleFileDeletion(activity);
    resourceActivityFilter.handleFileCreation(activity);
  }

  /**
   * Transforms the given activities on the server side and returns a list of QueueItems containing
   * the transformed activities and there receivers.
   *
   * <p>Drops activities that are reported as filtered out by {@link
   * ResourceActivityFilter#isFiltered(IActivity)}.
   *
   * @host
   * @sarosThread Must be executed in the Saros dispatch thread.
   * @notGUI This method may not be called from SWT, otherwise a deadlock might occur!!
   * @param activity Activity to be transformed
   * @return A list of QueueItems containing the activities and receivers
   */
  public List<QueueItem> transformIncoming(final IActivity activity) {

    assert sarosSession.isHost() : "CDS.transformIncoming must not be called on the client";

    // assert !isGUI() :
    // "CDS.transformIncoming must not be called from SWT";

    final List<QueueItem> result = new ArrayList<QueueItem>();

    if (resourceActivityFilter.isFiltered(activity)) {
      log.debug("Ignored activity for already deleted resource: " + activity);

      return result;
    }

    try {
      if (activity instanceof JupiterActivity) {
        result.addAll(receive((JupiterActivity) activity));

      } else if (activity instanceof ChecksumActivity) {
        result.addAll(withTimestamp((ChecksumActivity) activity));
      }
    } catch (Exception e) {
      log.error("failed to transform jupiter activity: " + activity, e);
    }

    return result;
  }

  /**
   * Does the actual work of transforming a clients JupiterActivity into specific JupiterActivities
   * for every client.
   */
  private List<QueueItem> receive(final JupiterActivity activity) {

    final List<QueueItem> result = new ArrayList<QueueItem>();

    // Sync jupiterActivity with jupiter document server
    final Map<User, JupiterActivity> outgoing;

    try {
      outgoing = server.transform(activity);
    } catch (TransformationException e) {
      log.error("failed to transform jupiter activity: " + activity, e);
      // TODO this should trigger a consistency check
      return result;
    }

    for (final Entry<User, JupiterActivity> entry : outgoing.entrySet()) {
      final User user = entry.getKey();
      final JupiterActivity transformed = entry.getValue();

      result.add(new QueueItem(user, transformed));
    }
    return result;
  }

  /**
   * Resets the JupiterServer for the given combination and path and user.
   *
   * <p>When this is called on the host, a call to {@link ConcurrentDocumentClient#reset(SPath)}
   * should be executed at the same time on the side of the given user.
   *
   * @host
   */
  public synchronized void reset(final User user, final IFile file) {

    assert sarosSession.isHost();

    log.debug("resetting jupiter server for user: " + user + ", path: " + file);

    server.reset(file, user);
  }

  /** Does the actual work of transforming a ChecksumActivity. */
  private List<QueueItem> withTimestamp(final ChecksumActivity activity) {

    final List<QueueItem> result = new ArrayList<QueueItem>();

    // Timestamp checksumActivity with jupiter document server
    final Map<User, ChecksumActivity> outgoing;

    try {
      outgoing = server.withTimestamp(activity);
    } catch (TransformationException e) {
      log.error("failed to transform checksum activity: " + activity, e);
      // TODO this should trigger a consistency check
      return result;
    }

    for (Entry<User, ChecksumActivity> entry : outgoing.entrySet()) {
      final User user = entry.getKey();
      final ChecksumActivity transformed = entry.getValue();

      result.add(new QueueItem(user, transformed));
    }
    return result;
  }
}
