package de.fu_berlin.inf.dpp.awareness;

import de.fu_berlin.inf.dpp.awareness.actions.ActionTypeDataHolder;

/**
 * A listener for updates of the awareness information by other session
 * participants
 * */
public interface AwarenessUpdateListener {

    /**
     * Is fired, when there are new awareness information received by other
     * session participants.
     * 
     * @param data
     *            The data holder containing information about the executed
     *            action
     * */
    public void update(ActionTypeDataHolder data);

}
