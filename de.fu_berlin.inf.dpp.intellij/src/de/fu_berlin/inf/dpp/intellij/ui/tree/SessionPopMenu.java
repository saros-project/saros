package de.fu_berlin.inf.dpp.intellij.ui.tree;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.picocontainer.annotations.Inject;

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
