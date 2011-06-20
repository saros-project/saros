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
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ResourceSelectionComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ResourceSelectionChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ResourceSelectionListener;

public class ProjectSelectionWizardPage extends WizardPage {
    public static final String TITLE = "Select Project";
    public static final String DESCRIPTION = "Select the file(s) to work on.";

    public static final String NO_PROJECT_SELECTED_ERROR_MESSAGE = "Select at least one file to work on.";

    protected ResourceSelectionComposite resourceSelectionComposite;

    /**
     * This {@link ResourceSelectionListener} changes the {@link WizardPage} 's
     * state according to the selected {@link IProject}.
     */
    protected ResourceSelectionListener resourceSelectionListener = new ResourceSelectionListener() {
        public void resourceSelectionChanged(ResourceSelectionChangedEvent event) {
            if (resourceSelectionComposite != null
                && !resourceSelectionComposite.isDisposed()) {
                if (resourceSelectionComposite.getSelectedResources().size() == 0) {
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
        this.resourceSelectionComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.FILL, true, true));

        /*
         * Page completion
         */
        updatePageCompletion();
    }

    protected void createProjectSelectionComposite(Composite parent) {
        if (this.resourceSelectionComposite != null
            && !this.resourceSelectionComposite.isDisposed())
            this.resourceSelectionComposite.dispose();

        this.resourceSelectionComposite = new ResourceSelectionComposite(
            parent, SWT.BORDER | SWT.V_SCROLL, PlatformUI.getPreferenceStore()
                .getBoolean(
                    PreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS));
        this.resourceSelectionComposite
            .setSelectedResources(SelectionRetrieverFactory
                .getSelectionRetriever(IResource.class).getOverallSelection());
        this.resourceSelectionComposite
            .addResourceSelectionListener(resourceSelectionListener);

        /*
         * If no project is selected and only one project exists in the
         * workspace, select it in Wizard.
         */
        if (this.resourceSelectionComposite.getSelectedResources().size() == 0) {
            List<IResource> resources = this.resourceSelectionComposite
                .getResources();

            if (this.resourceSelectionComposite.getProjectsCount() == 1) {
                this.resourceSelectionComposite.setSelectedResources(resources);
            }
        }
    }

    protected void updatePageCompletion() {
        int selectedProjectsCount = this.resourceSelectionComposite
            .getSelectedResources().size();
        setPageComplete(selectedProjectsCount > 0);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        this.resourceSelectionComposite.setFocus();
    }

    /*
     * WizardPage Results
     */

    public List<IResource> getSelectedResources() {
        if (this.resourceSelectionComposite == null
            || this.resourceSelectionComposite.isDisposed())
            return null;
        return this.resourceSelectionComposite.getSelectedResources();
    }
}
