package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
import org.picocontainer.annotations.Inject;

/** Action to activateor deactivate follow mode. */
public class FollowModeAction extends AbstractSarosAction {

  public static final String NAME = "follow";

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
          ThreadUtils.runSafeAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  FollowModeAction.this.session = session;
                }
              });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          ThreadUtils.runSafeAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  session = null;
                }
              });
        }
      };

  @Inject public ISarosSessionManager sessionManager;

  @Inject public EditorManager editorManager;

  private ISarosSession session;

  public FollowModeAction() {
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public String getActionName() {
    return NAME;
  }

  public void execute(String userName) {
    if (session == null) {
      return;
    }

    editorManager.setFollowing(findUser(userName));

    actionPerformed();
  }

  @Override
  public void execute() {
    // never called
  }

  public User getCurrentlyFollowedUser() {
    return editorManager.getFollowedUser();
  }

  public List<User> getCurrentRemoteSessionUsers() {
    if (session == null) return new ArrayList<User>();

    return session.getRemoteUsers();
  }

  private User findUser(String userName) {
    if (userName == null) {
      return null;
    }

    for (User user : getCurrentRemoteSessionUsers()) {
      String myUserName = ModelFormatUtils.getDisplayName(user);
      if (myUserName.equalsIgnoreCase(userName)) {
        return user;
      }
    }

    return null;
  }
}
