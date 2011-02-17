package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;


/**
 * Listener for {@link BaseBuddySelectionListener} events.
 */
public interface BaseBuddySelectionListener {

    /**
     * Gets called whenever a buddy selection changed.
     * 
     * @param event
     */
    public void buddySelectionChanged(BuddySelectionChangedEvent event);

}