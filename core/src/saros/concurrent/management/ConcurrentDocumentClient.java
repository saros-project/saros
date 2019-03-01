package saros.concurrent.management;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import saros.activities.AbstractActivityReceiver;
import saros.activities.ChecksumActivity;
import saros.activities.FileActivity;
import saros.activities.IActivity;
import saros.activities.IActivityReceiver;
import saros.activities.JupiterActivity;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.TransformationException;
import saros.session.ISarosSession;

/**
 * ConcurrentDocumentClient is responsible for managing the Jupiter interaction on the local side of
 * the clients.
 *
 * <p>A client exists for every participant (also the host!) to take local TextEdits and transforms
 * them into JupiterActivities to send to the Server on the host-side.
 *
 * <p>When JupiterActivities are received from the server they are transformed by the
 * ConcurrentDocumentClient to TextEditActivities which can then be executed locally.
 */
public class ConcurrentDocumentClient {

  private static Logger log = Logger.getLogger(ConcurrentDocumentClient.class);

  private final ISarosSession sarosSession;

  private final JupiterClient jupiterClient;

  public ConcurrentDocumentClient(ISarosSession sarosSession) {

    this.sarosSession = sarosSession;
    this.jupiterClient = new JupiterClient(sarosSession);
  }

  /**
   * This is called when an activity has been caused by the local user This method checks if an
   * activity has to be transformed into a Jupiter-specific-activity and transforms it if
   * needed. @GUI Must be called on the GUI Thread to ensure proper synchronization
   *
   * @host and @client This is called whenever activities are created locally both on the client and
   *     on the host
   * @param activity The activity to be transformed
   * @return The transformed activity
   */
  public IActivity transformToJupiter(IActivity activity) {

    // assert isGUI() :
    // "CDC.transformOutgoing must be called on the SWT Thread";

    if (activity instanceof TextEditActivity) {
      // Transform textEdit-into jupiterActivities
      TextEditActivity textEdit = (TextEditActivity) activity;
      return jupiterClient.generate(textEdit);

    } else if (activity instanceof ChecksumActivity) {
      ChecksumActivity checksumActivity = (ChecksumActivity) activity;

      /** Only the host can generate Checksums */
      assert sarosSession.isHost();

      // Create Jupiter specific checksum
      return jupiterClient.withTimestamp(checksumActivity);

    } else {
      return activity;
    }
  }

  /**
   * This method is called when activities received over the network should be executed locally.
   *
   * <p>This method will transform them back from Jupiter-specific activities to locally executable
   * activities. @GUI Must be called on the GUI Thread to ensure proper synchronization
   *
   * @host and @client This is called whenever activities are received from REMOTELY both on the
   *     client and on the host
   * @param activity The activity to be transformed
   * @return A list of locally executable activities
   */
  public List<IActivity> transformFromJupiter(IActivity activity) {

    // assert isGUI() :
    // "CDC.transformIncoming must be called on the SWT Thread";

    List<IActivity> activities = new ArrayList<IActivity>();

    try {
      activity.dispatch(clientReceiver);

      if (activity instanceof JupiterActivity) {
        activities.addAll(receiveActivity((JupiterActivity) activity));

      } else if (activity instanceof ChecksumActivity) {
        activities.add(receiveChecksum((ChecksumActivity) activity));
      } else {
        activities.add(activity);
      }

    } catch (Exception e) {
      log.error("Error while transforming activity: " + activity, e);
    }
    return activities;
  }

  /**
   * Will receive an incoming ChecksumActivity and discard it if it is not valid within the current
   * local Jupiter timestamp
   */
  private IActivity receiveChecksum(ChecksumActivity activity) {

    try {
      if (jupiterClient.isCurrent(activity)) return activity;
    } catch (TransformationException e) {
      // TODO this should trigger a consistency check
      log.error("Error during transformation of: " + activity, e);
    }
    return activity;
  }

  /** Used to remove JupiterClientDocuments for deleted files */
  private final IActivityReceiver clientReceiver =
      new AbstractActivityReceiver() {
        @Override
        public void receive(FileActivity fileActivity) {
          if (fileActivity.getType() == FileActivity.Type.REMOVED) {
            jupiterClient.reset(fileActivity.getPath());
          }
        }
      };

  /**
   * Transforms the JupiterActivity back into textEditActivities.
   *
   * @client and @host
   */
  private List<IActivity> receiveActivity(JupiterActivity jupiterActivity) {

    List<IActivity> activities = new ArrayList<IActivity>();

    Operation op;
    try {
      op = jupiterClient.receive(jupiterActivity);
    } catch (TransformationException e) {
      log.error("Error during transformation of: " + jupiterActivity, e);
      // TODO this should trigger a consistency check
      return activities;
    }

    // Transform to TextEdit so it can be executed locally
    for (TextEditActivity textEdit :
        op.toTextEdit(jupiterActivity.getPath(), jupiterActivity.getSource())) {

      activities.add(textEdit);
    }

    return activities;
  }

  /**
   * Resets the JupiterClient for the given path.
   *
   * <p>When this is called on the client (or on the host for one of his JupiterClient), a call to
   * {@link ConcurrentDocumentServer#reset(saros.session.User, SPath)} should be executed at the
   * same time on the side of the given user.
   *
   * @client and @host This can be called on the host as well, if the host wants to reset his client
   *     document (which at the moment never happens, because the version of the host is the
   *     authoritative one and thus does not need to be reset).
   */
  public synchronized void reset(SPath path) {
    log.debug("Resetting jupiter client: " + path.toString());
    jupiterClient.reset(path);
  }

  public boolean isCurrent(ChecksumActivity checksumActivity) {
    try {
      return jupiterClient.isCurrent(checksumActivity);
    } catch (TransformationException e) {
      log.error("Error during transformation of: " + checksumActivity, e);
      // TODO this should trigger a consistency recovery. Difficult :-(
      return false;
    }
  }
}
