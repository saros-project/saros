package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import org.picocontainer.annotations.Inject;

/** Action to activateor deactivate follow mode. */
public class FollowModeAction extends AbstractSarosAction {

  public static final String NAME = "follow";

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
