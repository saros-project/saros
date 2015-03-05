package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered methods are equivalent to
 * their Eclipse counterpart.
 */
public interface IWorkspaceRunnable {
    public void run(IProgressMonitor monitor) throws IOException,
        OperationCanceledException;
}
