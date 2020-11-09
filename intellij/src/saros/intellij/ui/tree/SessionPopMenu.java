package saros.intellij.ui.tree;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import saros.editor.FollowModeManager;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;
import saros.session.ISarosSession;
import saros.session.User;

/** Session pop-up menu that displays the option to follow a participant. */
class SessionPopMenu extends JPopupMenu {
  private final User user;
  private final ISarosSession sarosSession;

  SessionPopMenu(final User user, ISarosSession sarosSession) {
    this.user = user;
    this.sarosSession = sarosSession;

    createMenuItemToFollowParticipant();

    if (sarosSession.isHost()) {
      createMenuItemToKickParticipant();
    }
  }

  private void createMenuItemToFollowParticipant() {
    JMenuItem menuItemFollowParticipant =
        new JMenuItem(Messages.SessionPopMenu_follow_user, IconManager.FOLLOW_ICON);

    menuItemFollowParticipant.addActionListener(
        actionEvent -> {
          FollowModeManager followModeManager = sarosSession.getComponent(FollowModeManager.class);

          followModeManager.follow(user);
        });

    add(menuItemFollowParticipant);
  }

  private void createMenuItemToKickParticipant() {
    JMenuItem menuItemKickParticipant =
        new JMenuItem(Messages.SessionPopMenu_kick_user, IconManager.REMOVE_USER_FROM_SESSION);

    menuItemKickParticipant.addActionListener(
        actionEvent -> {
          sarosSession.kickUser(user);
        });

    add(menuItemKickParticipant);
  }
}
