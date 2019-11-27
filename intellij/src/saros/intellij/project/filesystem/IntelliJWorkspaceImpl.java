package saros.intellij.project.filesystem;

import java.io.IOException;
import org.apache.log4j.Logger;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.NullProgressMonitor;

public class IntelliJWorkspaceImpl implements IWorkspace {
  public static final Logger LOG = Logger.getLogger(IntelliJWorkspaceImpl.class);

  @Override
  public void run(IWorkspaceRunnable procedure) throws IOException, OperationCanceledException {
    procedure.run(new NullProgressMonitor());
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {
    run(runnable);
  }

  /**
   * This method always throws an <code>UnsupportedOperationException</code>.
   *
   * <p>In Intellij, module names can not be used to uniquely identify modules in an application
   * context. There can be any number of open project that can each contain a module with the same
   * name. This makes it impossible to implement a general method that just uses the module name.
   *
   * @param moduleName the name of the module to instantiate an <code>IProject</code> object for
   * @return nothing as it always throws an <code>UnsupportedOperationException</code>
   * @throws UnsupportedOperationException always
   * @deprecated does not make sense in the context of Intellij IDEA
   */
  @Deprecated
  @Override
  public IProject getProject(final String moduleName) {
    throw new UnsupportedOperationException(
        "Modules names can not be used to uniquely identify modules in Intellij in an application context.");
  }

  /**
   * This method always throws an <code>UnsupportedOperationException</code>.
   *
   * <p>In Intellij, there is no such concept as a centralized workspace directory for the IDE. Each
   * Intellij application can have any number of projects open which each can have any number of
   * modules. Each module in turn can have any number of content roots which define the content of
   * the module. In the filesystem, there is no correlation between the location of any of the above
   * mentioned parts. This makes it impossible to determine a general workspace directory.
   *
   * @return nothing as it always throws an <code>UnsupportedOperationException</code>
   * @throws UnsupportedOperationException always
   * @deprecated does not make sense in the context of Intellij IDEA
   */
  @Deprecated
  @Override
  public IPath getLocation() {
    throw new UnsupportedOperationException(
        "There is no such concept as a centralized workspace directory for Intellij.");
  }
}
