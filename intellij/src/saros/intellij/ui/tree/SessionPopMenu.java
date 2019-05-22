package saros.intellij.ui.tree;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import saros.SarosPluginContext;
import saros.editor.FollowModeManager;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.session.User;

/** Session pop-up menu that displays the option to follow a participant. */
class SessionPopMenu extends JPopupMenu {
  @Inject private static ISarosSessionManager sarosSessionManager;

  static {
    SarosPluginContext.initComponent(new SessionPopMenu());
  }

  /** NOP Constructor used for static dependency injection. */
  private SessionPopMenu() {
    // NOP
  }

  SessionPopMenu(final User user) {
    JMenuItem menuItemFollowParticipant = new JMenuItem("Follow participant");

    menuItemFollowParticipant.addActionListener(
        actionEvent -> {
          FollowModeManager followModeManager =
              sarosSessionManager.getSession().getComponent(FollowModeManager.class);

          followModeManager.follow(user);
        });

    add(menuItemFollowParticipant);
  }
}
