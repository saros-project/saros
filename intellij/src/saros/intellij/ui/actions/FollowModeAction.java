package saros.intellij.ui.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.editor.FollowModeManager;
import saros.session.ISarosSession;
import saros.session.User;
import saros.ui.util.ModelFormatUtils;

/** Action to activate or deactivate follow mode. */
public class FollowModeAction extends AbstractSarosAction {

  public static final String NAME = "follow";

  @Override
  public String getActionName() {
    return NAME;
  }

  public void execute(
      @NotNull ISarosSession session,
      @NotNull FollowModeManager followModeManager,
      @Nullable String userName) {

    User userToFollow = findUser(session, userName);

    followModeManager.follow(userToFollow);

    actionPerformed();
  }

  @Override
  public void execute() {
    // never called
  }

  private User findUser(@NotNull ISarosSession session, @Nullable String userName) {
    if (userName == null) {
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
