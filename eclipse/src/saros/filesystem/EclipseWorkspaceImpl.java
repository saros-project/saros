package saros.filesystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

    final org.eclipse.core.resources.IWorkspaceRunnable eclipseRunnable;

    /*
     * Don't wrap the runnable again if its actually a wrapped Eclipse
     * runnable, but extract the delegate instead.
     */
    if (runnable instanceof EclipseWorkspaceRunnableImpl) {
      eclipseRunnable = ((EclipseWorkspaceRunnableImpl) runnable).getDelegate();
    } else {
      eclipseRunnable = new EclipseRunnableAdapter(runnable);
    }

    final List<org.eclipse.core.resources.IResource> eclipseResources =
        ResourceAdapterFactory.convertBack(resources != null ? Arrays.asList(resources) : null);

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

  /** @deprecated See {@link IWorkspace}. */
  @Override
  @Deprecated
  public IProject getProject(String project) {
    return ResourceAdapterFactory.create(delegate.getRoot().getProject(project));
  }

  /** @deprecated See {@link IWorkspace}. */
  @Override
  @Deprecated
  public IPath getLocation() {
    return ResourceAdapterFactory.create(delegate.getRoot().getLocation());
  }

  public IReferencePoint getReferencePoint(String name) {
    return getProject(name).getReferencePoint();
  }

  @Override
  public void run(
      IWorkspaceRunnable runnable,
      IReferencePoint[] referencePoints,
      IReferencePointManager referencePointManager)
      throws IOException, OperationCanceledException {

    IResource[] resources = null;

    if (referencePoints != null) {
      resources = new IResource[referencePoints.length];
      for (int i = 0; i < referencePoints.length; i++) {
        resources[i] = referencePointManager.getProject(referencePoints[i]);
      }
    }
    run(runnable, resources);
  }
}
