package saros.filesystem;

import java.io.IOException;
import saros.exceptions.OperationCanceledException;

/**
 * Offers methods to run tasks in the local workspace.
 *
 * @see IWorkspaceRunnable
 */
// TODO rename to 'IWorkspaceRunner'
// TODO determine if workspace locking is something we want to support and remove otherwise
public interface IWorkspace {

  /**
   * Executes the given runnable at the next opportunity.
   *
   * <p>If supported, the whole workspace will be locked while the operation takes place (i.e other
   * operations cannot be performed until this operation returns).
   *
   * @param runnable the runnable to run
   * @throws IOException if the execution of the given runnable failed
   * @throws OperationCanceledException if the execution of the given runnable was canceled
   */
  void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException;

  /**
   * Executes the given runnable at the next opportunity.
   *
   * <p>If supported, parts of the workspace will be locked while the operation takes place. The
   * parts to be lock are determined by passed resources. The locked regions will start with the
   * given resources and cascade down to all children of each resource. If the given array of
   * resources is <code>null</code>, this call is equivalent to {@linkplain
   * #run(IWorkspaceRunnable)}.
   *
   * @param runnable the runnable to run
   * @param resources the resources to lock
   * @throws IOException if the execution of the given runnable failed
   * @throws OperationCanceledException if the execution of the given runnable was canceled
   */
  void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException;
}
