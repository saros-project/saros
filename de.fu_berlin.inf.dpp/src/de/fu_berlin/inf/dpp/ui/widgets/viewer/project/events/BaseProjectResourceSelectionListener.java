package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import org.eclipse.core.resources.IFile;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.BaseProjectResourceSelectionComposite;

/**
 * Listener for {@link BaseProjectResourceSelectionComposite} events.
 */
public interface BaseProjectResourceSelectionListener {

    /**
     * Gets called whenever a {@link IFile} selection changed.
     * 
     * @param event
     */
    public void projectResourceSelectionChanged(
        ProjectResourceSelectionChangedEvent event);

}