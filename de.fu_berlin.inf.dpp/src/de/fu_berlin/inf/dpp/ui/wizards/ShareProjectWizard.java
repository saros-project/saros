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
    public static final String TITLE = "Share Project";
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

    @Override
    public boolean performFinish() {
        List<IResource> selectedProjectResources = projectSelectionWizardPage
            .getSelectedProjectResources();
        List<JID> selectedBuddies = buddySelectionWizardPage
            .getSelectedBuddies();

        if (selectedProjectResources == null || selectedBuddies == null)
            return false;

        if (selectedProjectResources.isEmpty() || selectedBuddies.isEmpty())
            return false;

        CollaborationUtils.shareProjectResourcesWith(sarosSessionManager,
            selectedProjectResources, selectedBuddies);

        return true;
    }
}
