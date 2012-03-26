package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ResourceSelectionComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ResourceSelectionChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ResourceSelectionListener;
import de.fu_berlin.inf.dpp.util.Utils;

public class ProjectSelectionWizardPage extends WizardPage {
    Logger log = Logger.getLogger(this.getClass());

    protected ResourceSelectionComposite resourceSelectionComposite;

    /**
     * This {@link ResourceSelectionListener} changes the {@link WizardPage} 's
     * state according to the selected {@link IProject}.
     */
    protected ResourceSelectionListener resourceSelectionListener = new ResourceSelectionListener() {
        public void resourceSelectionChanged(ResourceSelectionChangedEvent event) {
            if (resourceSelectionComposite != null
                && !resourceSelectionComposite.isDisposed()) {
                if (!resourceSelectionComposite.hasSelectedResources()) {
                    setErrorMessage(Messages.ProjectSelectionWizardPage_selected_no_project);
                    setPageComplete(false);
                } else {
                    setErrorMessage(null);
                    setPageComplete(true);
                }
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
        setTitle(Messages.ProjectSelectionWizardPage_title);
        setDescription(Messages.ProjectSelectionWizardPage_description);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(1, false));

        createProjectSelectionComposite(composite);
        this.resourceSelectionComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.FILL, true, true));
    }

    /**
     * Create the composite and initialize it's selection asynchronously with
     * the current selection of the active "navigator"-type views in the current
     * workspace perspective
     * 
     * @param parent
     */
    protected void createProjectSelectionComposite(Composite parent) {
        if (this.resourceSelectionComposite != null
            && !this.resourceSelectionComposite.isDisposed())
            this.resourceSelectionComposite.dispose();

        this.resourceSelectionComposite = new ResourceSelectionComposite(
            parent, SWT.BORDER | SWT.V_SCROLL, PlatformUI.getPreferenceStore()
                .getBoolean(
                    PreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS));

        /*
         * Initialize the selection asynchronously, so the wizard opens
         * INSTANTLY instead of waiting up to XX seconds with flickering cursor
         * until the selection was applied.
         */
        Runnable takeOverPerspectiveSelection = new Runnable() {
            @Override
            public void run() {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    @Override
                    public void run() {
                        List<IResource> selection = SelectionRetrieverFactory
                            .getSelectionRetriever(IResource.class)
                            .getSelection();
                        resourceSelectionComposite
                            .setSelectedResources(selection);
                        resourceSelectionComposite
                            .addResourceSelectionListener(resourceSelectionListener);
                        /*
                         * If nothing is selected and only one project exists in
                         * the workspace, select it in the Wizard.
                         */
                        if (selection.size() == 0) {
                            if (resourceSelectionComposite.getProjectsCount() == 1) {
                                selection = resourceSelectionComposite
                                    .getResources();
                                resourceSelectionComposite
                                    .setSelectedResources(selection);
                            }
                        }
                        /*
                         * Add the current automatically appliedselection to the
                         * undo-stack, so the user can undo it, and undo/redo
                         * works properly.
                         */
                        resourceSelectionComposite.rememberSelection();
                        setPageComplete(selection.size() > 0);
                    }
                });
            }
        };

        BusyIndicator.showWhile(Display.getDefault(),
            takeOverPerspectiveSelection);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;
        de.fu_berlin.inf.dpp.ui.views.SarosView.clearNotifications();
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

    /**
     * Saves the current selection under a fixed name "-Last selection-"
     */
    public void rememberCurrentSelection() {
        resourceSelectionComposite.saveSelectionWithName("-Last selection-");
    }
}
