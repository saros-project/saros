package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import org.eclipse.core.resources.IProject;

public class ProjectSelectionChangedEvent {
    private IProject project;
    private boolean isSelected;

    /**
     * @param project
     *            {@link IProject} who's selection changed
     * @param isSelected
     *            new selection state
     */
    public ProjectSelectionChangedEvent(IProject project, boolean isSelected) {
        super();
        this.project = project;
        this.isSelected = isSelected;
    }

    public IProject getProject() {
        return project;
    }

    public boolean isSelected() {
        return isSelected;
    }

}
