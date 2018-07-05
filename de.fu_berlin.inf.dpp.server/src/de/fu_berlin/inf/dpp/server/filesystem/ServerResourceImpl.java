package de.fu_berlin.inf.dpp.server.filesystem;

import java.nio.file.Files;
import java.nio.file.Path;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;

/**
 * Server implementation of the {@link IResource} interface. It represents each
 * resource directly as a folder or file in the physical file system.
 */
public abstract class ServerResourceImpl implements IResource {

    private IWorkspace workspace;
    private IProject project;
    private IPath path;

    /**
     * Creates a ServerResourceImpl.
     * 
     * @param workspace
     *            the containing workspace
     * @param project
     *            the containing project
     * @param path
     *            the resource's path relative to the workspace's root
     */
    public ServerResourceImpl(IWorkspace workspace, IProject project, IPath path) {
        this.path = path;
        this.project = project;
        this.workspace = workspace;
    }

    /**
     * Returns the workspace the resource belongs to.
     * 
     * @return the containing workspace
     */
    public IWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * Returns the project the resource belongs to.
     *
     * @return the containing project
     */
    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public IPath getFullPath() {
        return path;
    }

    @Override
    public IPath getProjectRelativePath() {
        return new ServerPathImpl(
            project.getLocation().toFile().toPath().relativize(getLocation().toFile().toPath())
        );
    }

    @Override
    public String getName() {
        return getFullPath().lastSegment();
    }

    @Override
    public IPath getLocation() {
        return workspace.getLocation().append(path);
    }

    @Override
    public IContainer getParent() {
        IPath parentPath = getProjectRelativePath().removeLastSegments(1);
        IProject project = getProject();
        return parentPath.segmentCount() == 0 ? project : project
            .getFolder(parentPath);
    }

    @Override
    public boolean exists() {
        return Files.exists(toNioPath());
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    @Override
    public boolean isDerived(boolean checkAncestors) {
        return false;
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        return clazz.isInstance(this) ? this : null;
    }

    @Override
    public final boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof ServerResourceImpl))
            return false;

        ServerResourceImpl other = (ServerResourceImpl) obj;

        return getType() == other.getType()
            && getWorkspace().equals(other.getWorkspace())
            && getFullPath().equals(other.getFullPath());
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getType();
        result = prime * result + path.hashCode();
        result = prime * result + workspace.hashCode();
        return result;
    }

    /**
     * Returns the resource's location as a {@link java.nio.files.Path}. This is
     * for internal use in conjunction with the utility methods of the
     * {@link java.nio.file.Files} class.
     * 
     * @return location as {@link java.nio.files.Path}
     */
    Path toNioPath() {
        return ((ServerPathImpl) getLocation()).getDelegate();
    }
}
