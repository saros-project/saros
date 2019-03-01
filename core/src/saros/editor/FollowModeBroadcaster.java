package de.fu_berlin.inf.dpp.editor;

import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import org.picocontainer.Startable;

/**
 * The FollowModeBroadcaster is responsible for distributing the current follow mode state of the
 * local user to the other session users.
 */
public final class FollowModeBroadcaster extends AbstractActivityProducer implements Startable {

  private final ISarosSession session;
  private final FollowModeManager followModeManager;

  private final IFollowModeListener followModeLister =
      new IFollowModeListener() {

        @Override
        public void stoppedFollowing(Reason reason) {
          fireActivity(new StopFollowingActivity(session.getLocalUser()));
        }

        @Override
        public void startedFollowing(User target) {
          fireActivity(new StartFollowingActivity(session.getLocalUser(), target));
        }
      };

  public FollowModeBroadcaster(
      final ISarosSession session, final FollowModeManager followModeManager) {
    this.session = session;
    this.followModeManager = followModeManager;
  }

  @Override
  public void start() {
    session.addActivityProducer(this);
    followModeManager.addListener(followModeLister);
  }

  @Override
  public void stop() {
    followModeManager.removeListener(followModeLister);
    session.removeActivityProducer(this);
  }
}
