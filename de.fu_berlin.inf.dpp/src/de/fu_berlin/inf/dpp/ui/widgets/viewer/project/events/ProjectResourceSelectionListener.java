package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ProjectResourceSelectionComposite;

/**
 * Listener for {@link ProjectResourceSelectionComposite} events.
 */
public interface ProjectResourceSelectionListener extends
    BaseProjectResourceSelectionListener {

    /**
     * Gets called whenever the
     * {@link ProjectResourceSelectionComposite#filterClosedProjects} option
     * changed.
     * 
     * @param event
     */
    public void filterClosedProjectsChanged(
        FilterClosedProjectsChangedEvent event);

}