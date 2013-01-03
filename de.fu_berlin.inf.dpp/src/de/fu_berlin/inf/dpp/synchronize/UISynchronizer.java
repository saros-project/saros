package de.fu_berlin.inf.dpp.synchronize;

public interface UISynchronizer {

    public void asyncExec(Runnable runnable);

    public void syncExec(Runnable runnable);

}
