package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.FilteredContactSelectionComposite;

/**
 * Listener for {@link FilteredContactSelectionListener} events.
 */
public interface FilteredContactSelectionListener extends
    ContactSelectionListener {

    /**
     * Gets called whenever the
     * {@link FilteredContactSelectionComposite#filterNonSarosContacts} option
     * changed.
     * 
     * @param event
     */
    public void filterNonSarosContactsChanged(FilterContactsChangedEvent event);

}