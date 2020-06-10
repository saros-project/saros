package saros.intellij.ui.util;

import saros.negotiation.ResourceNegotiation;

/** Helper class to run Runnables that return a */
public abstract class JobWithStatus implements Runnable {

  public ResourceNegotiation.Status status;
}
