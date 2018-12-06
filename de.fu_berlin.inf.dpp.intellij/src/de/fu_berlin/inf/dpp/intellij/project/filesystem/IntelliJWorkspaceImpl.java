package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImpl;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import java.io.IOException;
import org.apache.log4j.Logger;

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
