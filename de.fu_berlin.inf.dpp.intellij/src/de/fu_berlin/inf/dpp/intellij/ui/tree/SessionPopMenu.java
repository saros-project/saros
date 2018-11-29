package de.fu_berlin.inf.dpp.intellij.ui.tree;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.User;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.picocontainer.annotations.Inject;

/** Session pop-up menu that displays the option to follow a participant. */
class SessionPopMenu extends JPopupMenu {

  @Inject private EditorManager editorManager;

  public SessionPopMenu(final User user) {
    SarosPluginContext.initComponent(this);
    JMenuItem menuItemFollowParticipant = new JMenuItem("Follow participant");
    menuItemFollowParticipant.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            editorManager.setFollowing(user);
          }
        });
    add(menuItemFollowParticipant);
  }
}
