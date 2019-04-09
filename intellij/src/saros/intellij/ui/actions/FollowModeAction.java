package saros.intellij.ui.actions;

import saros.editor.FollowModeManager;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.util.ModelFormatUtils;

/** Action to activate or deactivate follow mode. */
public class FollowModeAction extends AbstractSarosAction {

  public static final String NAME = "follow";

  @SuppressWarnings("FieldCanBeLocal")
  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
          FollowModeAction.this.session = session;
          followModeManager = session.getComponent(FollowModeManager.class);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          session = null;
          followModeManager = null;
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  private volatile ISarosSession session;
  private volatile FollowModeManager followModeManager;

  public FollowModeAction() {
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public String getActionName() {
    return NAME;
  }

  public void execute(String userName) {
    FollowModeManager currentFollowModeManager = followModeManager;
    User userToFollow = findUser(userName);

    if (currentFollowModeManager == null) {
      return;
    }

    currentFollowModeManager.follow(userToFollow);

    actionPerformed();
  }

  @Override
  public void execute() {
    // never called
  }

  private User findUser(String userName) {
    ISarosSession currentSession = session;

    if (userName == null || currentSession == null) {
      return null;
    }

    for (User user : session.getRemoteUsers()) {
      String myUserName = ModelFormatUtils.getDisplayName(user);
      if (myUserName.equalsIgnoreCase(userName)) {
        return user;
      }
    }

    return null;
  }
}
