package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.BaseProjectSelectionComposite;

/**
 * Listener for {@link BaseProjectSelectionComposite} events.
 */
public interface BaseProjectSelectionListener {

    /**
     * Gets called whenever a {@link IProject} selection changed.
     * 
     * @param event
     */
    public void projectSelectionChanged(ProjectSelectionChangedEvent event);

}