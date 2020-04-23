package saros.session.internal;

import org.apache.log4j.Logger;
import saros.activities.DeletionAcknowledgmentActivity;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Type;
import saros.activities.IActivity;
import saros.filesystem.IFile;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.session.User;

/**
 * Class reacting to the reception of activities containing the deletion of a file by sending an
 * acknowledgement.
 *
 * @see DeletionAcknowledgmentActivity
 */
public class DeletionAcknowledgmentDispatcher extends AbstractActivityProducer
    implements Startable {

  private static final Logger log = Logger.getLogger(DeletionAcknowledgmentDispatcher.class);

  private final ISarosSession sarosSession;

  private final IActivityConsumer activityConsumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(FileActivity fileActivity) {
          acknowledgeDeletionActivity(fileActivity);
        }
      };

  public DeletionAcknowledgmentDispatcher(ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
  }

  @Override
  public void start() {
    sarosSession.addActivityProducer(this);
    sarosSession.addActivityConsumer(activityConsumer, Priority.PASSIVE);
  }

  @Override
  public void stop() {
    sarosSession.removeActivityProducer(this);
    sarosSession.removeActivityConsumer(activityConsumer);
  }

  /**
   * Checks whether the passed file activity contains the deletion of a file and sends an
   * acknowledgment to all other participants.
   *
   * <p>Activities that contain file deletions are {@link FileActivity file activities} of the type
   * {@link Type#REMOVED} or {@link Type#MOVED}.
   *
   * <p>Ignores file move activities where the origin and destination is the same.
   *
   * @param fileActivity the file activity to check and acknowledge if applicable
   */
  private void acknowledgeDeletionActivity(FileActivity fileActivity) {
    IFile deletedFile;

    if (fileActivity.getType() == Type.MOVED
        && !fileActivity.getResource().equals(fileActivity.getOldResource())) {

      deletedFile = fileActivity.getOldResource();

    } else if (fileActivity.getType() == Type.REMOVED) {
      deletedFile = fileActivity.getResource();

    } else {
      return;
    }

    User localUser = sarosSession.getLocalUser();

    IActivity deletionAcknowledgementActivity =
        new DeletionAcknowledgmentActivity(localUser, deletedFile);

    log.debug("Sending deletion acknowledgment for " + deletedFile);
    fireActivity(deletionAcknowledgementActivity);
  }
}
