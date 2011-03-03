package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ProjectSelectionComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.BaseProjectSelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ProjectSelectionChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ProjectSelectionListener;

public class ProjectSelectionWizardPage extends WizardPage {
    public static final String TITLE = "Select Project";
    public static final String DESCRIPTION = "Select the project(s) to work on.";

    public static final String NO_PROJECT_SELECTED_ERROR_MESSAGE = "Select at least one project to work on.";

    protected ProjectSelectionComposite projectSelectionComposite;

    /**
     * This {@link BaseProjectSelectionListener} changes the {@link WizardPage}
     * 's state according to the selected {@link IProject}.
     */
    protected ProjectSelectionListener projectSelectionListener = new ProjectSelectionListener() {
        public void projectSelectionChanged(ProjectSelectionChangedEvent event) {
            if (projectSelectionComposite != null
                && !projectSelectionComposite.isDisposed()) {
                if (projectSelectionComposite.getSelectedProjects().size() == 0) {
                    setErrorMessage(NO_PROJECT_SELECTED_ERROR_MESSAGE);
                } else {
                    setErrorMessage(null);
                }
                updatePageCompletion();
            }
        }

        public void filterClosedProjectsChanged(
            FilterClosedProjectsChangedEvent event) {
            PlatformUI.getPreferenceStore().setValue(
                PreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS,
                event.isFilterClosedProjects());
        }
    };

    @Inject
    Saros saros;

    public ProjectSelectionWizardPage() {
        super(ProjectSelectionWizardPage.class.getName());
        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        /*
         * Row 1
         */
        Label projectSelectionLabel = new Label(composite, SWT.NONE);
        projectSelectionLabel.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.TOP, false, true));
        projectSelectionLabel.setText("Projects:");

        createProjectSelectionComposite(composite);
        this.projectSelectionComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.FILL, true, true));

        /*
         * Page completion
         */
        updatePageCompletion();
    }

    protected void createProjectSelectionComposite(Composite parent) {
        if (this.projectSelectionComposite != null
            && !this.projectSelectionComposite.isDisposed())
            this.projectSelectionComposite.dispose();

        this.projectSelectionComposite = new ProjectSelectionComposite(parent,
            SWT.BORDER | SWT.V_SCROLL, PlatformUI.getPreferenceStore()
                .getBoolean(
                    PreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS));
        this.projectSelectionComposite
            .setSelectedProjects(SelectionRetrieverFactory
                .getSelectionRetriever(IProject.class).getOverallSelection());
        this.projectSelectionComposite
            .addProjectSelectionListener(projectSelectionListener);

        /*
         * If no project is selected and one project exists in the workspace,
         * use it.
         */
        if (this.projectSelectionComposite.getSelectedProjects().size() == 0) {
            List<IProject> projects = this.projectSelectionComposite
                .getProjects();
            if (projects.size() == 1) {
                this.projectSelectionComposite.setSelectedProjects(projects);
            }
        }
    }

    protected void updatePageCompletion() {
        int selectedProjectsCount = this.projectSelectionComposite
            .getSelectedProjects().size();
        setPageComplete(selectedProjectsCount > 0);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        this.projectSelectionComposite.setFocus();
    }

    /*
     * WizardPage Results
     */

    public List<IProject> getSelectedProjects() {
        if (this.projectSelectionComposite == null
            || this.projectSelectionComposite.isDisposed())
            return null;

        return this.projectSelectionComposite.getSelectedProjects();
    }
}
