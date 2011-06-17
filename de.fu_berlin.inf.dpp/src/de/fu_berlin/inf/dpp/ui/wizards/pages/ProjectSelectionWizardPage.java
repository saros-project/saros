package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ProjectResourceSelectionComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ProjectResourceSelectionChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ProjectResourceSelectionListener;

public class ProjectSelectionWizardPage extends WizardPage {
    public static final String TITLE = "Select Project";
    public static final String DESCRIPTION = "Select the file(s) to work on.";

    public static final String NO_PROJECT_SELECTED_ERROR_MESSAGE = "Select at least one file to work on.";

    protected ProjectResourceSelectionComposite projectResourceSelectionComposite;

    /**
     * This {@link ProjectResourceSelectionListener} changes the
     * {@link WizardPage} 's state according to the selected {@link IProject}.
     */
    protected ProjectResourceSelectionListener projectResourceSelectionListener = new ProjectResourceSelectionListener() {
        public void projectResourceSelectionChanged(
            ProjectResourceSelectionChangedEvent event) {
            if (projectResourceSelectionComposite != null
                && !projectResourceSelectionComposite.isDisposed()) {
                if (projectResourceSelectionComposite
                    .getSelectedProjectResources().size() == 0) {
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
        this.projectResourceSelectionComposite.setLayoutData(new GridData(
            SWT.FILL, SWT.FILL, true, true));

        /*
         * Page completion
         */
        updatePageCompletion();
    }

    protected void createProjectSelectionComposite(Composite parent) {
        if (this.projectResourceSelectionComposite != null
            && !this.projectResourceSelectionComposite.isDisposed())
            this.projectResourceSelectionComposite.dispose();

        this.projectResourceSelectionComposite = new ProjectResourceSelectionComposite(
            parent, SWT.BORDER | SWT.V_SCROLL, PlatformUI.getPreferenceStore()
                .getBoolean(
                    PreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS));
        this.projectResourceSelectionComposite
            .setSelectedProjectResources(SelectionRetrieverFactory
                .getSelectionRetriever(IResource.class).getOverallSelection());
        this.projectResourceSelectionComposite
            .addProjectResourceSelectionListener(projectResourceSelectionListener);

        /*
         * If no project is selected and only one project exists in the
         * workspace, select it in Wizard.
         */
        if (this.projectResourceSelectionComposite
            .getSelectedProjectResources().size() == 0) {
            List<IResource> resources = this.projectResourceSelectionComposite
                .getProjectResources();

            if (this.projectResourceSelectionComposite.getProjectsCount() == 1) {
                this.projectResourceSelectionComposite
                    .setSelectedProjectResources(resources);
            }
        }
    }

    protected void updatePageCompletion() {
        int selectedProjectsCount = this.projectResourceSelectionComposite
            .getSelectedProjectResources().size();
        setPageComplete(selectedProjectsCount > 0);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        this.projectResourceSelectionComposite.setFocus();
    }

    /*
     * WizardPage Results
     */

    public List<IResource> getSelectedProjectResources() {
        if (this.projectResourceSelectionComposite == null
            || this.projectResourceSelectionComposite.isDisposed())
            return null;
        return this.projectResourceSelectionComposite
            .getSelectedProjectResources();
    }
}
