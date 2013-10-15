package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ContactSelectionWizardPage;

/**
 * Wizard for adding buddies to a running session.
 * 
 * @author bkahlert
 */
public class AddContactsToSessionWizard extends Wizard {
    public static final String TITLE = Messages.ShareProjectAddBuddiesWizard_title;
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SHARE_PROJECT_ADD_BUDDIES;

    @Inject
    protected ISarosSessionManager sarosSessionManager;

    protected ContactSelectionWizardPage buddySelectionWizardPage = new ContactSelectionWizardPage();

    public AddContactsToSessionWizard() {
        SarosPluginContext.initComponent(this);
        this.setWindowTitle(TITLE);
        this.setDefaultPageImageDescriptor(IMAGE);

        this.setHelpAvailable(false);
    }

    @Override
    public void addPages() {
        this.addPage(this.buddySelectionWizardPage);
    }

    @Override
    public boolean performFinish() {
        List<JID> selectedBuddies = buddySelectionWizardPage
            .getSelectedContacts();
        if (selectedBuddies == null)
            return false;

        CollaborationUtils.addBuddiesToSarosSession(sarosSessionManager,
            selectedBuddies);

        return true;
    }
}
