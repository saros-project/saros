package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;

/**
 * Listener for {@link ContactSelectionListener} events.
 */
public interface ContactSelectionListener {

    /**
     * Gets called whenever a contact selection changed.
     * 
     * @param event
     */
    public void contactSelectionChanged(ContactSelectionChangedEvent event);

}