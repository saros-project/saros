package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.BuddySelectionWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ProjectSelectionWizardPage;

/**
 * Wizard for sharing project resources.
 * <p>
 * Starts sharing the selected resource(s) with the selected buddy(s) on finish.
 * 
 * @author bkahlert
 * @author kheld
 */
public class ShareProjectWizard extends Wizard {
    public static final String TITLE = Messages.ShareProjectWizard_title;
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SHARE_PROJECT_OUTGOING;

    @Inject
    protected SarosSessionManager sarosSessionManager;

    protected ProjectSelectionWizardPage projectSelectionWizardPage = new ProjectSelectionWizardPage();
    protected BuddySelectionWizardPage buddySelectionWizardPage = new BuddySelectionWizardPage();

    public ShareProjectWizard() {
        SarosPluginContext.initComponent(this);
        this.setWindowTitle(TITLE);
        this.setDefaultPageImageDescriptor(IMAGE);

        this.setHelpAvailable(false);
    }

    @Override
    public void addPages() {
        this.addPage(this.projectSelectionWizardPage);
        this.addPage(this.buddySelectionWizardPage);
    }

    /**
     * @JTourBusStop 2, Invitation Process:
     * 
     *               The the chosen resources are put into collections to be
     *               sent to the chosen buddies.
     * 
     *               As a slight detour, notice that the call to
     *               CollaborationUtils.shareResourcesWith includes
     *               sarosSessionManager as an argument. However, when you look
     *               through this class you should find this variable is
     *               declared but never initialised! It is not null however.
     * 
     *               Notice that "@Inject" annotation above the
     *               sarosSessionManager declaration? That means that the our
     *               PicoContainer has taken care of initialising the variable
     *               for us. Look up PicoContainer to find out more about this.
     */
    @Override
    public boolean performFinish() {
        List<IResource> selectedResources = projectSelectionWizardPage
            .getSelectedResources();
        List<JID> selectedBuddies = buddySelectionWizardPage
            .getSelectedBuddies();

        if (selectedResources == null || selectedBuddies == null)
            return false;

        if (selectedResources.isEmpty() || selectedBuddies.isEmpty())
            return false;

        CollaborationUtils.shareResourcesWith(sarosSessionManager,
            selectedResources, selectedBuddies);

        return true;
    }
}
