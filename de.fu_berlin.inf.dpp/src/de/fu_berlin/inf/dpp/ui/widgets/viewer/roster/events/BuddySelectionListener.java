package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.BuddySelectionComposite;

/**
 * Listener for {@link BuddySelectionListener} events.
 */
public interface BuddySelectionListener extends BaseBuddySelectionListener {

    /**
     * Gets called whenever the
     * {@link BuddySelectionComposite#filterNonSarosBuddies} option changed.
     * 
     * @param event
     */
    public void filterNonSarosBuddiesChanged(
        FilterNonSarosBuddiesChangedEvent event);

}