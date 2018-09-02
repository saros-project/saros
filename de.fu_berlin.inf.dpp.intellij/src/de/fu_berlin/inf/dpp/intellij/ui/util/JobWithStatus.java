package de.fu_berlin.inf.dpp.intellij.ui.util;

import de.fu_berlin.inf.dpp.negotiation.ReferencePointNegotiation;

/**
 * Helper class to run Runnables that return a
 */
public abstract class JobWithStatus implements Runnable {

    public ProjectNegotiation.Status status;
}
