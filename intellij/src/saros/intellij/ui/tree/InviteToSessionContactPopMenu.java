package saros.intellij.ui.tree;

import java.util.Collections;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import saros.core.ui.util.CollaborationUtils;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;

/**
 * Contact pop-up menu used to add new users to the session. This menu must only be displayed if
 * there is a running session and the local user is the host.
 */
public class InviteToSessionContactPopMenu extends JPopupMenu {

  private final ContactTreeRootNode.ContactInfo contactInfo;

  public InviteToSessionContactPopMenu(ContactTreeRootNode.ContactInfo contactInfo) {
    this.contactInfo = contactInfo;

    createMenuAddUserToSession();
  }

  /**
   * Creates and displays a menu entry to add the selected user to the current session. This menu
   * must only be displayed if there is a running session and the local user is the host.
   */
  private void createMenuAddUserToSession() {
    JMenuItem addToSession =
        new JMenuItem(
            Messages.InviteToSessionMenu_add_to_session_popup_text,
            IconManager.ADD_USER_TO_SESSION);

    addToSession.addActionListener(
        l ->
            CollaborationUtils.addContactsToSession(
                Collections.singletonList(contactInfo.getJid())));

    add(addToSession);
  }
}
