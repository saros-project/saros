package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ProjectSelectionWizardPage;

/**
 * Wizard for adding projects to a running session.
 * 
 * @author bkahlert
 */
public class ShareProjectAddProjectsWizard extends Wizard {
    public static final String TITLE = "Add Project(s) to Session";
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SHARE_PROJECT_ADD_PROJECTS;

    @Inject
    protected SarosSessionManager sarosSessionManager;

    protected ProjectSelectionWizardPage projectSelectionWizardPage = new ProjectSelectionWizardPage();

    public ShareProjectAddProjectsWizard() {
        SarosPluginContext.initComponent(this);
        this.setWindowTitle(TITLE);
        this.setDefaultPageImageDescriptor(IMAGE);

        this.setHelpAvailable(false);
    }

    @Override
    public void addPages() {
        this.addPage(this.projectSelectionWizardPage);
    }

    @Override
    public boolean performFinish() {
        List<IProject> selectedProjects = projectSelectionWizardPage
            .getSelectedProjects();
        if (selectedProjects == null)
            return false;

        CollaborationUtils.addProjectsToSarosSession(sarosSessionManager,
            selectedProjects);

        return true;
    }
}
