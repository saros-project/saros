package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.BaseProjectSelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.NewProjectListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ProjectSelectionListener;

/**
 * This {@link Composite} extends {@link BaseProjectSelectionComposite} and
 * displays additional controls.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout and adding
 * sub {@link Control}s correctly.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dd>SWT.CHECK is used by default</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public class ProjectSelectionComposite extends BaseProjectSelectionComposite {
    protected boolean filterClosedProjects;
    protected Button filterClosedProjectsButton;
    protected ViewerFilter closedProjectsFilter = new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {
            if (element instanceof IProject) {
                IProject project = (IProject) element;
                return project.isOpen();
            }
            return false;
        }
    };

    /**
     * Constructs a new {@link ProjectSelectionComposite}
     * 
     * @param parent
     * @param style
     * @param filterClosedProjects
     *            true if initially closed projects should not be displayed
     */
    public ProjectSelectionComposite(Composite parent, int style,
        boolean filterClosedProjects) {
        super(parent, style);

        createControls();
        setFilterClosedProjects(filterClosedProjects);
    }

    /**
     * Creates additional controls
     */
    protected void createControls() {
        Composite controlComposite = new Composite(this, SWT.NONE);
        controlComposite.setLayoutData(LayoutUtils.createFillHGrabGridData());
        controlComposite.setLayout(new GridLayout(2, false));

        filterClosedProjectsButton = new Button(controlComposite, SWT.CHECK);
        filterClosedProjectsButton.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.CENTER, false, false));
        filterClosedProjectsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setFilterClosedProjects(filterClosedProjectsButton
                    .getSelection());
            }
        });
        filterClosedProjectsButton.setText("Hide closed projects");

        Button newProjectButton = new Button(controlComposite, SWT.PUSH);
        newProjectButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true,
            false));
        newProjectButton.setText("New Project...");
        newProjectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                newProject();
            }
        });
    }

    /**
     * Defines whether closed projects should be displayed or not
     * 
     * @param filterClosedProjects
     *            true if closed projects should not be displayed
     */
    public void setFilterClosedProjects(boolean filterClosedProjects) {
        if (this.filterClosedProjects == filterClosedProjects)
            return;

        this.filterClosedProjects = filterClosedProjects;

        if (this.filterClosedProjectsButton != null
            && !this.filterClosedProjectsButton.isDisposed()
            && this.filterClosedProjectsButton.getSelection() != filterClosedProjects) {
            this.filterClosedProjectsButton.setSelection(filterClosedProjects);
        }

        if (filterClosedProjects) {
            viewer.addFilter(closedProjectsFilter);
        } else {
            viewer.removeFilter(closedProjectsFilter);
        }

        notifyProjectSelectionListener(filterClosedProjects);
    }

    /**
     * Opens a wizard for {@link IProject} creation and sets the new project as
     * the selected one.
     */
    protected void newProject() {
        NewProjectListener listener = new NewProjectListener();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        workspace.addResourceChangeListener(listener);
        WizardUtils.openNewProjectWizard();
        workspace.removeResourceChangeListener(listener);

        IProject newProject = listener.getNewProject();
        if (newProject != null) {
            List<IProject> selectedProjects = this.getSelectedProjects();
            selectedProjects.add(newProject);
            this.setSelectedProjects(selectedProjects);
        }
    }

    /**
     * Notify all {@link ProjectSelectionListener}s about a changed
     * {@link ProjectSelectionComposite#filterClosedProjects} option.
     * 
     * @param filterClosedProjects
     */
    public void notifyProjectSelectionListener(boolean filterClosedProjects) {
        FilterClosedProjectsChangedEvent event = new FilterClosedProjectsChangedEvent(
            filterClosedProjects);
        for (BaseProjectSelectionListener projectSelectionListener : this.projectSelectionListeners) {
            if (projectSelectionListener instanceof ProjectSelectionListener)
                ((ProjectSelectionListener) projectSelectionListener)
                    .filterClosedProjectsChanged(event);
        }
    }
}
