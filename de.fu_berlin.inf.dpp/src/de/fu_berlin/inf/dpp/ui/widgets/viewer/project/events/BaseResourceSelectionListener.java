package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import org.eclipse.core.resources.IFile;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.BaseResourceSelectionComposite;

/**
 * Listener for {@link BaseResourceSelectionComposite} events.
 */
public interface BaseResourceSelectionListener {

    /**
     * Gets called whenever a {@link IFile} selection changed.
     * 
     * @param event
     */
    public void resourceSelectionChanged(ResourceSelectionChangedEvent event);

}