package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ResourceSelectionWizardPage;

/**
 * Wizard for adding resources to a running session.
 * 
 * @author bkahlert
 */
public class ShareProjectAddProjectsWizard extends Wizard {
    public static final String TITLE = Messages.ShareProjectAddProjectsWizard_title;
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SHARE_PROJECT_ADD_PROJECTS;

    @Inject
    private ISarosSessionManager sarosSessionManager;

    private ResourceSelectionWizardPage resourceSelectionWizardPage = new ResourceSelectionWizardPage();

    public ShareProjectAddProjectsWizard() {
        SarosPluginContext.initComponent(this);
        setWindowTitle(TITLE);
        setDefaultPageImageDescriptor(IMAGE);
        setHelpAvailable(false);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        SarosView.clearNotifications();
        return super.getNextPage(page);
    }

    @Override
    public void addPages() {
        addPage(this.resourceSelectionWizardPage);
    }

    @Override
    public boolean performFinish() {
        List<IResource> selectedResources = resourceSelectionWizardPage
            .getSelectedResources();

        if (selectedResources == null || selectedResources.isEmpty())
            return false;

        resourceSelectionWizardPage.rememberCurrentSelection();

        SarosView.clearNotifications();

        CollaborationUtils.addResourcesToSarosSession(sarosSessionManager,
            selectedResources);

        return true;
    }
}
