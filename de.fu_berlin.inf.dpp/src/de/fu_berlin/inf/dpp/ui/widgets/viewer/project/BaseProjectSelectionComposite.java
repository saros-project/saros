package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.BaseProjectSelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ProjectSelectionChangedEvent;

/**
 * This {@link Composite} extends {@link ProjectDisplayComposite} and allows to
 * check (via check boxes) {@link IProject}s.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
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
public class BaseProjectSelectionComposite extends ProjectDisplayComposite {
    protected List<BaseProjectSelectionListener> projectSelectionListeners = new ArrayList<BaseProjectSelectionListener>();

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {
        public void checkStateChanged(CheckStateChangedEvent event) {
            notifyProjectSelectionChanged((IProject) event.getElement(),
                event.getChecked());
        }
    };

    public BaseProjectSelectionComposite(Composite parent, int style) {
        super(parent, style | SWT.CHECK);

        ((CheckboxTableViewer) this.viewer)
            .addCheckStateListener(checkStateListener);

        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (viewer != null)
                    ((CheckboxTableViewer) viewer)
                        .removeCheckStateListener(checkStateListener);
            }
        });
    }

    @Override
    public void createViewer(int style) {
        this.viewer = new CheckboxTableViewer(new Table(this, style));
    }

    /**
     * Sets the currently selected {@link IProject}s.
     * 
     * @param projects
     */
    public void setSelectedProjects(List<IProject> projects) {
        ((CheckboxTableViewer) this.viewer).setCheckedElements(projects
            .toArray());
    }

    /**
     * Returns the currently selected {@link IProject}s.
     * 
     * @return
     */
    public List<IProject> getSelectedProjects() {
        List<IProject> projects = new ArrayList<IProject>();
        for (Object element : ((CheckboxTableViewer) this.viewer)
            .getCheckedElements()) {
            projects.add((IProject) element);
        }
        return projects;
    }

    /**
     * Adds a {@link BaseProjectSelectionListener}
     * 
     * @param projectSelectionListener
     */
    public void addProjectSelectionListener(
        BaseProjectSelectionListener projectSelectionListener) {
        this.projectSelectionListeners.add(projectSelectionListener);
    }

    /**
     * Removes a {@link BaseProjectSelectionListener}
     * 
     * @param projectSelectionListener
     */
    public void removeProjectSelectionListener(
        BaseProjectSelectionListener projectSelectionListener) {
        this.projectSelectionListeners.remove(projectSelectionListener);
    }

    /**
     * Notify all {@link BaseProjectSelectionListener}s about a changed
     * selection.
     * 
     * @param project
     *            {@link IProject} who's selection changed
     * 
     * @param isSelected
     *            new selection state
     */
    public void notifyProjectSelectionChanged(IProject project,
        boolean isSelected) {
        ProjectSelectionChangedEvent event = new ProjectSelectionChangedEvent(
            project, isSelected);
        for (BaseProjectSelectionListener projectSelectionListener : this.projectSelectionListeners) {
            projectSelectionListener.projectSelectionChanged(event);
        }
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
