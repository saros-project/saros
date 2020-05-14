package saros.filesystem;

import java.io.IOException;
import saros.exceptions.OperationCanceledException;
import saros.monitoring.IProgressMonitor;

/**
 * A task than can be run in the local workspace.
 *
 * @see IWorkspace
 */
public interface IWorkspaceRunnable {

  /**
   * Runs the contained task, reporting to the given progress monitor.
   *
   * @param monitor the progress monitor to report to or <code>null</code> if progress reporting and
   *     cancellation support is not needed
   * @throws IOException if the execution of the contained task failed
   * @throws OperationCanceledException if the execution of the contained task was canceled
   */
  void run(IProgressMonitor monitor) throws IOException, OperationCanceledException;
}
