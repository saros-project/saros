package saros.filesystem;

import java.io.IOException;
import saros.exceptions.OperationCanceledException;

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

  /**
   * @deprecated the concept of a central workspace for the IDE instance does not apply to all IDE
   *     models (see the IntelliJ project/module model). This method will be removed from the
   *     interface in a future patch.
   */
  @Deprecated
  public IProject getProject(String project);

  /**
   * @deprecated the concept of a central workspace for the IDE instance does not apply to all IDE
   *     models (see the IntelliJ project/module model). This method will be removed from the
   *     interface in a future patch.
   */
  @Deprecated
  public IPath getLocation();

  /**
   * Returns the reference point with the given name which is a child of this workspace
   *
   * @param name the name of the reference point
   * @return a reference point with the given name
   */
  public IReferencePoint getReferencePoint(String name);
}
