package de.fu_berlin.inf.dpp.intellij.ui.tree;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.picocontainer.annotations.Inject;

/** Session pop-up menu that displays the option to follow a participant. */
class SessionPopMenu extends JPopupMenu {

  @SuppressWarnings("FieldCanBeLocal")
  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession session) {
          followModeManager = session.getComponent(FollowModeManager.class);
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          followModeManager = null;
        }
      };

  @Inject private ISarosSessionManager sarosSessionManager;

  private volatile FollowModeManager followModeManager;

  SessionPopMenu(final User user) {
    SarosPluginContext.initComponent(this);

    sarosSessionManager.addSessionLifecycleListener(sessionLifecycleListener);

    JMenuItem menuItemFollowParticipant = new JMenuItem("Follow participant");
    menuItemFollowParticipant.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            FollowModeManager currentFollowModeManager = followModeManager;

            if (currentFollowModeManager != null) {
              currentFollowModeManager.follow(user);
            }
          }
        });
    add(menuItemFollowParticipant);
  }
}
