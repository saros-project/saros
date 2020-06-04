package saros.filesystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import saros.Saros;
import saros.exceptions.OperationCanceledException;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.ProgressMonitorAdapterFactory;

/**
 * Eclipse implementation of {@link IWorkspace}. Lets you execute {@link IWorkspaceRunnable}s via
 * {@link #run(IWorkspaceRunnable) run()}.
 */
public class EclipseWorkspaceImpl implements IWorkspace {

  /**
   * Takes an IDE-independent {@link IWorkspaceRunnable Saros WorkspaceRunnable} and wraps it, so it
   * can be treated as an Eclipse WorkspaceRunnable.
   *
   * <p>The {@link #run(org.eclipse.core.runtime.IProgressMonitor) run()}-method takes care of
   * converting the {@link IProgressMonitor progress monitor} and any thrown Exceptions.
   */
  private static class EclipseRunnableAdapter
      implements org.eclipse.core.resources.IWorkspaceRunnable {

    private IWorkspaceRunnable delegate;

    /** @param runnable a {@link IWorkspaceRunnable Saros WorkspaceRunnable} */
    public EclipseRunnableAdapter(IWorkspaceRunnable runnable) {
      this.delegate = runnable;
    }

    @Override
    public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws CoreException {

      try {
        delegate.run(ProgressMonitorAdapterFactory.convert(monitor));
      } catch (IOException e) {
        final Throwable cause = e.getCause();

        if (cause instanceof CoreException) throw (CoreException) cause;

        if (cause instanceof org.eclipse.core.runtime.OperationCanceledException)
          throw (org.eclipse.core.runtime.OperationCanceledException) cause;

        final IStatus status = new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);

        throw new CoreException(status);
      } catch (OperationCanceledException e) {
        org.eclipse.core.runtime.OperationCanceledException wrapped =
            new org.eclipse.core.runtime.OperationCanceledException();
        wrapped.initCause(e);
        throw wrapped;
      }
    }
  }

  private org.eclipse.core.resources.IWorkspace delegate;

  public EclipseWorkspaceImpl(org.eclipse.core.resources.IWorkspace workspace) {
    this.delegate = workspace;
  }

  @Override
  public void run(final IWorkspaceRunnable runnable)
      throws IOException, OperationCanceledException {
    run(runnable, null);
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {

    final org.eclipse.core.resources.IWorkspaceRunnable eclipseRunnable =
        new EclipseRunnableAdapter(runnable);

    final List<org.eclipse.core.resources.IResource> eclipseResources =
        resources == null
            ? null
            : Arrays.stream(resources)
                .map(ResourceConverter::getDelegate)
                .collect(Collectors.toList());

    final ISchedulingRule schedulingRule;

    if (eclipseResources != null && !eclipseResources.isEmpty()) {
      schedulingRule =
          new MultiRule(eclipseResources.toArray(new org.eclipse.core.resources.IResource[0]));
    } else {
      schedulingRule = delegate.getRoot();
    }

    try {
      delegate.run(
          eclipseRunnable,
          schedulingRule,
          org.eclipse.core.resources.IWorkspace.AVOID_UPDATE,
          null);
    } catch (CoreException e) {
      throw new IOException(e);
    } catch (org.eclipse.core.runtime.OperationCanceledException e) {
      Throwable cause = e.getCause();

      if (cause instanceof OperationCanceledException) throw (OperationCanceledException) cause;

      throw new OperationCanceledException(e);
    }
  }
}
