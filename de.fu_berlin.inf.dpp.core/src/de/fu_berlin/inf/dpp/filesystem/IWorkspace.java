package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered methods are equivalent to
 * their Eclipse counterpart.
 */
public interface IWorkspace {
    public void run(IWorkspaceRunnable runnable) throws IOException,
        OperationCanceledException;

    public IProject getProject(String project);

    public IPath getLocation();
}
