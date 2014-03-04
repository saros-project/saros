package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

public class EclipseResourceImpl implements IResource {

    protected final org.eclipse.core.resources.IResource delegate;

    EclipseResourceImpl(org.eclipse.core.resources.IResource delegate) {
        if (delegate == null)
            throw new NullPointerException("delegate is null");

        this.delegate = delegate;
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public IPath getFullPath() {
        return new EclipsePathImpl(delegate.getFullPath());
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public IContainer getParent() {
        org.eclipse.core.resources.IContainer container = delegate.getParent();

        if (container == null)
            return null;

        switch (container.getType()) {
        case org.eclipse.core.resources.IResource.FOLDER:
            return new EclipseFolderImpl(
                (org.eclipse.core.resources.IFolder) container);
        case org.eclipse.core.resources.IResource.PROJECT:
            return new EclipseProjectImpl(
                (org.eclipse.core.resources.IProject) container);
        default:
            // TODO support IWorkspaceRoot ?
            return null;
        }
    }

    @Override
    public IProject getProject() {
        org.eclipse.core.resources.IProject project = delegate.getProject();

        if (project == null)
            return null;

        return new EclipseProjectImpl(project);
    }

    @Override
    public IPath getProjectRelativePath() {
        return new EclipsePathImpl(delegate.getProjectRelativePath());
    }

    /**
     * Returns the original {@link org.eclipse.core.resources.IResource
     * IResource} object.
     * 
     * @return
     */
    public org.eclipse.core.resources.IResource getDelegate() {
        return delegate;
    }

    @Override
    public int getType() {
        int type = delegate.getType();

        switch (type) {
        case org.eclipse.core.resources.IResource.FILE:
            type = IResource.FILE;
            break;
        case org.eclipse.core.resources.IResource.FOLDER:
            type = IResource.FOLDER;
            break;
        case org.eclipse.core.resources.IResource.PROJECT:
            type = IResource.PROJECT;
            break;
        case org.eclipse.core.resources.IResource.ROOT:
            type = IResource.ROOT;
            break;
        default:
            type = 0;
        }
        return type;
    }

    @Override
    public boolean isAccessible() {
        return delegate.isAccessible();
    }

    @Override
    public boolean isDerived(boolean checkAncestors) {
        if (!checkAncestors)
            return delegate.isDerived();

        return delegate
            .isDerived(org.eclipse.core.resources.IResource.CHECK_ANCESTORS);
    }

    @Override
    public void refreshLocal() throws IOException {
        try {
            delegate.refreshLocal(
                org.eclipse.core.resources.IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof EclipseResourceImpl))
            return false;

        return delegate.equals(((EclipseResourceImpl) obj).delegate);
    }

    @Override
    public String toString() {
        return super.toString() + " - " + delegate.toString();
    }
}
