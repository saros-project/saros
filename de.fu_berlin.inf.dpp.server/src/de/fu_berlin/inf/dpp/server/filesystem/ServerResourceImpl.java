package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server implementation of the {@link IResource} interface. It represents each resource directly as
 * a folder or file in the physical file system.
 */
public abstract class ServerResourceImpl implements IResource {

  private IWorkspace workspace;
  private IPath path;

  /**
   * Creates a ServerResourceImpl.
   *
   * @param workspace the containing workspace
   * @param path the resource's path relative to the workspace's root
   */
  public ServerResourceImpl(IWorkspace workspace, IPath path) {
    this.path = path;
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

  @Override
  public IPath getFullPath() {
    return path;
  }

  @Override
  public IPath getProjectRelativePath() {
    return getFullPath().removeFirstSegments(1);
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
    return parentPath.segmentCount() == 0 ? project : project.getFolder(parentPath);
  }

  @Override
  public IProject getProject() {
    String projectName = getFullPath().segment(0);
    return workspace.getProject(projectName);
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
  public <T extends IResource> T adaptTo(Class<T> clazz) {
    return clazz.isInstance(this) ? clazz.cast(this) : null;
  }

  @Override
  public final boolean equals(Object obj) {

    if (this == obj) return true;

    if (!(obj instanceof ServerResourceImpl)) return false;

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
   * Returns the resource's location as a {@link java.nio.files.Path}. This is for internal use in
   * conjunction with the utility methods of the {@link java.nio.file.Files} class.
   *
   * @return location as {@link java.nio.files.Path}
   */
  Path toNioPath() {
    return ((ServerPathImpl) getLocation()).getDelegate();
  }
}
