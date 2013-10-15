package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;

public class FilterContactsChangedEvent {
    private boolean filterNonSarosContacts;

    /**
     * @param filterNonSarosContacts
     */
    public FilterContactsChangedEvent(boolean filterNonSarosContacts) {
        this.filterNonSarosContacts = filterNonSarosContacts;
    }

    public boolean isFilterNonSarosContacts() {
        return filterNonSarosContacts;
    }
}
