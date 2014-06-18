package de.fu_berlin.inf.dpp.core.monitor;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered methods are equivalent to
 * their Eclipse counterpart.
 * </p>
 * TODO Consolidate with interface in Saros/Core
 */
public interface IProgressMonitor {
    public static final int UNKNOWN = 0;

    boolean isCanceled();

    void setCanceled(boolean cancel);

    void worked(int delta);

    void subTask(String remaingTime);

    void setTaskName(String name);

    void done();

    void beginTask(String taskName, String type);

    void beginTask(String taskNam, int size);

    void internalWorked(double work);

    //TODO: Hack, ISubMonitor to be removed from Saros-I
    ISubMonitor convert();

    //TODO: Hack, ISubMonitor to be removed from Saros-I
    ISubMonitor convert(String title, int progress);
}
