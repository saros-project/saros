package saros.ui.wizards;

import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import saros.net.xmpp.JID;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;
import saros.ui.wizards.pages.ContactSelectionWizardPage;

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
