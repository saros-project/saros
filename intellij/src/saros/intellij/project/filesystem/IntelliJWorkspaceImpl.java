package saros.intellij.project.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import java.io.IOException;
import org.apache.log4j.Logger;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.intellij.filesystem.Filesystem;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.monitoring.NullProgressMonitor;

public class IntelliJWorkspaceImpl implements IWorkspace {
  public static final Logger LOG = Logger.getLogger(IntelliJWorkspaceImpl.class);

  private Project project;

  public IntelliJWorkspaceImpl(Project project) {
    this.project = project;
  }

  @Override
  public void run(IWorkspaceRunnable procedure) throws IOException, OperationCanceledException {
    procedure.run(new NullProgressMonitor());
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {
    run(runnable);
  }

  @Override
  public IProject getProject(final String moduleName) {
    Module module =
        Filesystem.runReadAction(
            new Computable<Module>() {
              @Override
              public Module compute() {
                return ModuleManager.getInstance(project).findModuleByName(moduleName);
              }
            });

    return module != null ? new IntelliJProjectImpl(module) : null;
  }

  @Override
  public IPath getLocation() {
    return IntelliJPathImpl.fromString(project.getBasePath());
  }
}
