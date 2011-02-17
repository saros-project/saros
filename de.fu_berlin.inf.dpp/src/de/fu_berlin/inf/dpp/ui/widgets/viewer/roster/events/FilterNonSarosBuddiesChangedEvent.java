package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;

public class FilterNonSarosBuddiesChangedEvent {
    private boolean filterNonSarosBuddies;

    /**
     * @param filterNonSarosBuddies
     */
    public FilterNonSarosBuddiesChangedEvent(boolean filterNonSarosBuddies) {
        super();
        this.filterNonSarosBuddies = filterNonSarosBuddies;
    }

    public boolean isFilterNonSarosBuddies() {
        return filterNonSarosBuddies;
    }

}
