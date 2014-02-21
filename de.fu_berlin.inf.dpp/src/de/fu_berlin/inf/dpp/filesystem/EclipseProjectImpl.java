package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

public class EclipseProjectImpl extends EclipseContainerImpl implements
    IProject {

    EclipseProjectImpl(org.eclipse.core.resources.IProject delegate) {
        super(delegate);
    }

    @Override
    public IResource findMember(IPath path) {
        org.eclipse.core.resources.IResource resource = ((org.eclipse.core.resources.IProject) delegate)
            .findMember(((EclipsePathImpl) path).getDelegate());

        return ResourceAdapterFactory.create(resource);
    }

    @Override
    public IFile getFile(String name) {
        return new EclipseFileImpl(
            ((org.eclipse.core.resources.IProject) delegate).getFile(name));
    }

    @Override
    public IFile getFile(IPath path) {
        return new EclipseFileImpl(
            ((org.eclipse.core.resources.IProject) delegate)
                .getFile(((EclipsePathImpl) path).getDelegate()));
    }

    @Override
    public IFolder getFolder(String name) {
        return new EclipseFolderImpl(
            ((org.eclipse.core.resources.IProject) delegate).getFolder(name));
    }

    @Override
    public IFolder getFolder(IPath path) {
        return new EclipseFolderImpl(
            ((org.eclipse.core.resources.IProject) delegate)
                .getFolder(((EclipsePathImpl) path).getDelegate()));
    }

    @Override
    public boolean isOpen() {
        return ((org.eclipse.core.resources.IProject) delegate).isOpen();
    }

    @Override
    public void open() throws IOException {
        try {
            ((org.eclipse.core.resources.IProject) delegate).open(null);
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    /**
     * Returns the original {@link org.eclipse.core.resources.IProject IProject}
     * object.
     * 
     * @return
     */
    @Override
    public org.eclipse.core.resources.IProject getDelegate() {
        return (org.eclipse.core.resources.IProject) delegate;
    }
}
