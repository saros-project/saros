package de.fu_berlin.inf.dpp.monitoring;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered method are equivalent to
 * their Eclipse counterpart.
 */
public interface IProgressMonitor {
    public void subTask(String task);

    public void worked(int amount);

    public void setCanceled(boolean canceled);

    public boolean isCanceled();

    public void beginTask(String string, int size);
}
