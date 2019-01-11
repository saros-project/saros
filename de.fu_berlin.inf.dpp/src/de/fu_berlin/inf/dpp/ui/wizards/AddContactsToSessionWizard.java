package de.fu_berlin.inf.dpp.ui.wizards;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ContactSelectionWizardPage;
import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for adding contacts to a running session.
 *
 * @author bkahlert
 */
public class AddContactsToSessionWizard extends Wizard {
  public static final String TITLE = Messages.SessionAddContactsWizard_title;
  public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SESSION_ADD_CONTACTS;

  private final ContactSelectionWizardPage contactSelectionWizardPage =
      new ContactSelectionWizardPage();

  public AddContactsToSessionWizard() {
    setWindowTitle(TITLE);
    setDefaultPageImageDescriptor(IMAGE);
    setHelpAvailable(false);
  }

  @Override
  public void addPages() {
    addPage(contactSelectionWizardPage);
  }

  @Override
  public boolean performFinish() {
    List<JID> selectedContacts = contactSelectionWizardPage.getSelectedContacts();

    if (selectedContacts == null) return false;

    CollaborationUtils.addContactsToSession(selectedContacts);

    return true;
  }
}
