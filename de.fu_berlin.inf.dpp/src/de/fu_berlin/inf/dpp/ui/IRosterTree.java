package de.fu_berlin.inf.dpp.ui;

/**
 * This interface handles the event updates for roster tree.
 * 
 * @author orieger
 * 
 */
public interface IRosterTree {

    /**
     * Refreshes the roster tree.
     * 
     * @param updateLabels
     *            <code>true</code> if item labels (might) have changed.
     *            <code>false</code> otherwise.
     */
    public void refreshRosterTree(final boolean updateLabels);
}
