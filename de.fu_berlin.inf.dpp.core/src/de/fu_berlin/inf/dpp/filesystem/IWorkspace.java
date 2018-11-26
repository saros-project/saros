package de.fu_berlin.inf.dpp.filesystem;

import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
public interface IWorkspace {

  /**
   * Executes the given runnable at the next opportunity. If supported the whole workspace will be
   * locked while the operation takes place, i.e other operations cannot be performed until this
   * operation returns.
   *
   * @param runnable
   * @throws IOException
   * @throws OperationCanceledException
   */
  public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException;

  /**
   * Executes the given runnable at the next opportunity. If supported parts of the workspace will
   * be locked while the operation takes place. The parts to be lock are determined by the <code>
   * resources</code> argument, i.e the locked regions will start with the given resources and
   * cascade down to all children of each resource. If the <code>resources</code> argument is <code>
   * null</code> this call is equivalent to {@linkplain #run(IWorkspaceRunnable)}.
   *
   * @param runnable
   * @param resources
   * @throws IOException
   * @throws OperationCanceledException
   */
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException;

  public IProject getProject(String project);

  public IPath getLocation();
}
