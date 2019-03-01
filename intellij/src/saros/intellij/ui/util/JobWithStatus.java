package saros.intellij.ui.util;

import saros.negotiation.ProjectNegotiation;

/** Helper class to run Runnables that return a */
public abstract class JobWithStatus implements Runnable {

  public ProjectNegotiation.Status status;
}
