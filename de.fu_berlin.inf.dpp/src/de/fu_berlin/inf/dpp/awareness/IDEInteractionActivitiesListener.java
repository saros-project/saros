package de.fu_berlin.inf.dpp.awareness;

/**
 * A listener for IDE interaction activities, like opening or closing dialogs
 * and activating or deactivating views.
 */
public interface IDEInteractionActivitiesListener {

    /**
     * Is fired, when a user opened or closed a dialog or activated or
     * deactivated a view.
     * */
    public void dialogOrViewInteractionChanged();
}