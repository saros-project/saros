package saros.lsp.filesystem;

import java.nio.file.Files;
import java.nio.file.Path;
import saros.filesystem.IContainer;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

/**
 * Saros language server implementation of {@link IResource}.
 *
 * @implNote Based on the server implementation
 */
public abstract class LspResource implements IResource {
  private IWorkspacePath workspace;
  private IPath path;

  /**
   * Creates a ServerResourceImpl.
   *
   * @param workspace the containing workspace
   * @param path the resource's path relative to the workspace's root
   */
  public LspResource(IWorkspacePath workspace, IPath path) {
    assert !path.isAbsolute();

    if (workspace.isPrefixOf(path)) {
      path = path.removeFirstSegments(workspace.segmentCount());
    }

    this.path = path;
    this.workspace = workspace;
  }

  /**
   * Returns the workspace the resource belongs to.
   *
   * @return the containing workspace
   */
  public IWorkspacePath getWorkspace() {
    return workspace;
  }

  @Override
  public String getName() {
    String lastSegment = path.lastSegment();

    if (lastSegment == null) {
      return "";
    }

    return lastSegment;
  }

  public IPath getLocation() {
    return workspace.append(path);
  }

  @Override
  public IContainer getParent() {
    IPath parentPath = getReferencePointRelativePath().removeLastSegments(1);
    IReferencePoint project = getReferencePoint();
    return parentPath.segmentCount() == 0 ? project : project.getFolder(parentPath);
  }

  @Override
  public boolean exists() {
    return Files.exists(toNioPath());
  }

  @Override
  public boolean isIgnored() {
    return false;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof LspResource)) return false;

    LspResource other = (LspResource) obj;

    return getType() == other.getType() && path.equals(other.path);
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getType().hashCode();
    result = prime * result + path.hashCode();
    return result;
  }

  @Override
  public IReferencePoint getReferencePoint() {
    return workspace.getReferencePoint("");
  }

  @Override
  public IPath getReferencePointRelativePath() {
    return path;
  }

  /**
   * Returns the resource's location as a {@link java.nio.files.Path}. This is for internal use in
   * conjunction with the utility methods of the {@link java.nio.file.Files} class.
   *
   * @return location as {@link java.nio.files.Path}
   */
  Path toNioPath() {
    return ((LspPath) getLocation()).getDelegate();
  }
}
