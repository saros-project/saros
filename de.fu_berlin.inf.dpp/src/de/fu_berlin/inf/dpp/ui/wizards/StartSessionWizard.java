package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ContactSelectionWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ResourceSelectionWizardPage;

/**
 * Wizard for starting a session.
 * <p>
 * Starts sharing the selected resource(s) with the selected contacts(s) on
 * finish.
 * 
 * @author bkahlert
 * @author kheld
 */
public class StartSessionWizard extends Wizard {

    public static final String TITLE = Messages.ShareProjectWizard_title;
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SHARE_PROJECT_OUTGOING;

    @Inject
    private ISarosSessionManager sarosSessionManager;

    private ResourceSelectionWizardPage resourceSelectionWizardPage = new ResourceSelectionWizardPage();
    private ContactSelectionWizardPage contactSelectionWizardPage = new ContactSelectionWizardPage(
        true);

    public StartSessionWizard() {
        SarosPluginContext.initComponent(this);
        setWindowTitle(TITLE);
        setDefaultPageImageDescriptor(IMAGE);
        setNeedsProgressMonitor(true);
        setHelpAvailable(false);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        /*
         * Remove any open notifications on page change in the wizard, in case
         * the user restored a selection in the ResourceSelectionComposite
         */
        SarosView.clearNotifications();
        return super.getNextPage(page);
    }

    @Override
    public void addPages() {
        addPage(resourceSelectionWizardPage);
        addPage(contactSelectionWizardPage);
    }

    /**
     * @JTourBusStop 2, Invitation Process:
     * 
     *               The chosen resources are put into collections to be sent to
     *               the chosen buddies.
     * 
     *               As a slight detour, notice that the call to
     *               CollaborationUtils.shareResourcesWith includes
     *               sarosSessionManager as an argument. However, when you look
     *               through this class you should find this variable is
     *               declared but never initialized! It is not null however.
     * 
     *               Notice that "@Inject" annotation above the
     *               sarosSessionManager declaration? That means that our
     *               PicoContainer has taken care of initializing the variable
     *               for us. Look up PicoContainer to find out more about this.
     */
    @Override
    public boolean performFinish() {

        List<IResource> selectedResources = resourceSelectionWizardPage
            .getSelectedResources();

        List<JID> selectedContacts = contactSelectionWizardPage
            .getSelectedContacts();

        if (selectedResources == null || selectedContacts == null)
            return false;

        if (selectedResources.isEmpty())
            return false;

        resourceSelectionWizardPage.rememberCurrentSelection();

        SarosView.clearNotifications();

        CollaborationUtils.startSession(sarosSessionManager,
            selectedResources, selectedContacts);

        return true;
    }
}
