package saros.server.filesystem;

import java.nio.file.Files;
import java.nio.file.Path;
import saros.filesystem.IContainer;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.util.PathUtils;

/**
 * Server implementation of the {@link IResource} interface. It represents each resource directly as
 * a folder or file in the physical file system.
 */
public abstract class ServerResourceImpl implements IResource {

  private ServerWorkspaceImpl workspace;
  private Path path;

  /**
   * Creates a ServerResourceImpl.
   *
   * @param workspace the containing workspace
   * @param path the resource's path relative to the workspace's root
   */
  public ServerResourceImpl(ServerWorkspaceImpl workspace, Path path) {
    this.path = PathUtils.normalize(path);
    this.workspace = workspace;
  }

  /**
   * Returns the workspace the resource belongs to.
   *
   * @return the containing workspace
   */
  public ServerWorkspaceImpl getWorkspace() {
    return workspace;
  }

  public Path getFullPath() {
    return path;
  }

  @Override
  public Path getReferencePointRelativePath() {
    return PathUtils.removeFirstSegments(getFullPath(), 1);
  }

  @Override
  public String getName() {
    return getFullPath().getFileName().toString();
  }

  public Path getLocation() {
    return workspace.getLocation().resolve(path);
  }

  @Override
  public IContainer getParent() {
    Path parentPath = PathUtils.removeLastSegments(getReferencePointRelativePath(), 1);
    IReferencePoint project = getReferencePoint();
    return PathUtils.isEmpty(parentPath) ? project : project.getFolder(parentPath);
  }

  @Override
  public IReferencePoint getReferencePoint() {
    String projectName = getFullPath().getName(0).toString();
    return workspace.getProject(projectName);
  }

  @Override
  public boolean exists() {
    return Files.exists(getLocation());
  }

  @Override
  public boolean isIgnored() {
    return false;
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
    result = prime * result + getType().hashCode();
    result = prime * result + path.hashCode();
    result = prime * result + workspace.hashCode();
    return result;
  }
}
