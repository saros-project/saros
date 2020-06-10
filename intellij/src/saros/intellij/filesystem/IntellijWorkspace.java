package saros.intellij.filesystem;

import java.io.IOException;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.NullProgressMonitor;

public class IntellijWorkspace implements IWorkspace {
  @Override
  public void run(IWorkspaceRunnable procedure) throws IOException, OperationCanceledException {
    procedure.run(new NullProgressMonitor());
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {
    run(runnable);
  }
}
