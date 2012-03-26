package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ProjectSelectionComposite;

/**
 * Listener for {@link ProjectSelectionComposite} events.
 */
public interface ProjectSelectionListener extends BaseProjectSelectionListener {

    /**
     * Gets called whenever the
     * {@link ProjectSelectionComposite#filterClosedProjects} option changed.
     * 
     * @param event
     */
    public void filterClosedProjectsChanged(
        FilterClosedProjectsChangedEvent event);

}