package saros.filesystem;

import java.io.IOException;
import saros.exceptions.OperationCanceledException;
import saros.monitoring.IProgressMonitor;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
public interface IWorkspaceRunnable {
  public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException;
}
