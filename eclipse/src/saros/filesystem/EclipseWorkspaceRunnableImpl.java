package saros.filesystem;

import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.ProgressMonitorAdapterFactory;

/**
 * Takes an {@link org.eclipse.core.resources.IWorkspaceRunnable Eclipse WorkspaceRunnable} and
 * wraps it so it can be treated as IDE-independent.
 */
public class EclipseWorkspaceRunnableImpl implements IWorkspaceRunnable {

  private org.eclipse.core.resources.IWorkspaceRunnable delegate;

  public EclipseWorkspaceRunnableImpl(org.eclipse.core.resources.IWorkspaceRunnable runnable) {
    this.delegate = runnable;
  }

  @Override
  public void run(IProgressMonitor monitor) throws IOException {
    try {
      delegate.run(ProgressMonitorAdapterFactory.convert(monitor));
    } catch (CoreException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  public org.eclipse.core.resources.IWorkspaceRunnable getDelegate() {
    return delegate;
  }
}
