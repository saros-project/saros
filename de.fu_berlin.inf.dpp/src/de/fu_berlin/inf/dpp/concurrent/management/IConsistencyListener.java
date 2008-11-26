package de.fu_berlin.inf.dpp.concurrent.management;

/**
 * A listener for the event that an inconsistency are detected.
 * 
 * @author chjacob
 */
public interface IConsistencyListener {

    /**
     * Notifies the listener about an inconsistency issue.
     */
    public void inconsistencyDetected();

    /**
     * Notifies the listener that a previous consistency issue have resolved.
     */
    public void inconsistencyResolved();
}
