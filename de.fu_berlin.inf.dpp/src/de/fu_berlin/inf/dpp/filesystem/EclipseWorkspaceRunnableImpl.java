package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.monitoring.EclipseProgressMonitorImpl;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

/**
 * Takes an {@link org.eclipse.core.resources.IWorkspaceRunnable Eclipse
 * WorkspaceRunnable} and wraps it so it can be treated as IDE-independent.
 */
public class EclipseWorkspaceRunnableImpl implements IWorkspaceRunnable {

    private org.eclipse.core.resources.IWorkspaceRunnable delegate;

    public EclipseWorkspaceRunnableImpl(
        org.eclipse.core.resources.IWorkspaceRunnable runnable) {
        this.delegate = runnable;
    }

    @Override
    public void run(IProgressMonitor monitor) throws IOException {
        org.eclipse.core.runtime.IProgressMonitor mon = ((EclipseProgressMonitorImpl) monitor)
            .getDelegate();
        try {
            delegate.run(mon);
        } catch (CoreException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public org.eclipse.core.resources.IWorkspaceRunnable getDelegate() {
        return delegate;
    }
}
