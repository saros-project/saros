package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.log4j.Logger;

/**
 * Server implementation of the {@link IResource} interface. It represents each resource directly as
 * a folder or file in the physical file system.
 */
public abstract class ServerResourceImplV2 implements IResource {

  private static final Logger LOG = Logger.getLogger(ServerResourceImplV2.class);

  protected final IPath referencePointsPath;
  private final IPath referencePointRelativePath;

  /**
   * Creates a ServerResourceImplV2.
   *
   * @param referencePointsPath the root source's path
   * @param referencePointRelativePath the resource's path relative to root source
   */
  public ServerResourceImplV2(IPath referencePointsPath, IPath referencePointRelativePath) {
    this.referencePointsPath = referencePointsPath;
    this.referencePointRelativePath = referencePointRelativePath;
  }

  @Override
  public IPath getFullPath() {
    return referencePointRelativePath;
  }

  @Override
  public IPath getProjectRelativePath() {
    return getFullPath().removeFirstSegments(1);
  }

  @Override
  public String getName() {
    return getLocation().lastSegment();
  }

  @Override
  public IPath getLocation() {
    return referencePointsPath.append(referencePointRelativePath);
  }

  @Override
  public IFolder getParent() {
    IPath parentPath = getProjectRelativePath().removeLastSegments(1);
    IFolder sourceFolder = getReferenceFolder();
    return parentPath.segmentCount() == 0 ? sourceFolder : sourceFolder.getFolder(parentPath);
  }

  @Override
  public IFolder getReferenceFolder() {
    return new ServerFolderImpl(referencePointsPath, ServerPathImpl.fromString(new String()));
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

    if (this == obj) return true;

    if (!(obj instanceof ServerResourceImplV2)) return false;

    ServerResourceImplV2 other = (ServerResourceImplV2) obj;

    return getType() == other.getType()
        && getLocation().equals(other.getLocation())
        && getFullPath().equals(other.getFullPath());
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getType();
    result = prime * result + referencePointRelativePath.hashCode();
    result = prime * result + referencePointsPath.hashCode();
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
