package saros.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import saros.util.PathUtils;

/** Eclipse implementation of the Saros reference point interface. */
public class EclipseReferencePoint implements IReferencePoint {
  private static Logger log = Logger.getLogger(EclipseReferencePoint.class);

  /** The delegate represented by the reference point. */
  private final org.eclipse.core.resources.IContainer delegate;

  /**
   * Instantiates an Eclipse reference point object.
   *
   * @param delegate the delegate resource represented by the reference point
   * @throws NullPointerException if the given delegate is <code>null</code>
   * @throws IllegalArgumentException if the given delegate does not exist
   */
  public EclipseReferencePoint(org.eclipse.core.resources.IContainer delegate) {
    Objects.requireNonNull(delegate, "The given delegate must not be null");

    if (!delegate.exists()) {
      throw new IllegalArgumentException("The given delegate must exist - " + delegate);
    }

    this.delegate = delegate;
  }

  /**
   * Returns the delegate represented by this reference point.
   *
   * @return the delegate represented by this reference point
   */
  org.eclipse.core.resources.IContainer getDelegate() {
    return delegate;
  }

  @Override
  public Path getReferencePointRelativePath() {
    return Paths.get("");
  }

  @Override
  public boolean isNested(IReferencePoint otherReferencePoint) {
    org.eclipse.core.runtime.IPath p1 = delegate.getFullPath();

    IContainer d2 = ResourceConverter.getDelegate(otherReferencePoint);
    org.eclipse.core.runtime.IPath p2 = d2.getFullPath();

    return p1.equals(p2) || p1.isPrefixOf(p2) || p2.isPrefixOf(p1);
  }

  @Override
  public boolean exists() {
    return delegate.exists();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public boolean exists(Path path) {
    Objects.requireNonNull(path, "Given path must not be null");

    return delegate.exists(ResourceConverter.toEclipsePath(path));
  }

  @Override
  public List<IResource> members() throws IOException {
    org.eclipse.core.resources.IResource[] containedResources;

    try {
      containedResources = delegate.members();

    } catch (CoreException e) {
      throw new IOException(e);
    }

    List<IResource> result = new ArrayList<>(containedResources.length);

    for (org.eclipse.core.resources.IResource containedResource : containedResources) {
      String name = containedResource.getName();

      Path childPath = Paths.get(name);

      if (containedResource.getType() == org.eclipse.core.resources.IResource.FILE) {
        result.add(new EclipseFile(this, childPath));

      } else if (containedResource.getType() == org.eclipse.core.resources.IResource.FOLDER) {
        result.add(new EclipseFolder(this, childPath));

      } else {
        throw new IllegalStateException(
            "Encounter root or project in inner tree - " + containedResource);
      }
    }

    return result;
  }

  @Override
  public saros.filesystem.IFile getFile(String pathString) {
    return getFile(Paths.get(pathString));
  }

  @Override
  public saros.filesystem.IFile getFile(Path path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (PathUtils.isEmpty(path)) {
      throw new IllegalArgumentException("Can not create file handle for an empty path");
    }

    return new EclipseFile(this, path);
  }

  @Override
  public saros.filesystem.IFolder getFolder(String pathString) {
    return getFolder(Paths.get(pathString));
  }

  @Override
  public saros.filesystem.IFolder getFolder(Path path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (PathUtils.isEmpty(path)) {
      throw new IllegalArgumentException("Can not create folder handle for an empty path");
    }

    return new EclipseFolder(this, path);
  }

  /**
   * Returns the file delegate for the given relative path.
   *
   * @param relativePath relative path to the file
   * @return the delegate for the given relative path
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute or empty
   * @see org.eclipse.core.resources.IContainer#getFile(org.eclipse.core.runtime.IPath)
   */
  org.eclipse.core.resources.IFile getFileDelegate(Path relativePath) {
    Objects.requireNonNull(relativePath, "Given path must not be null");

    if (PathUtils.isEmpty(relativePath)) {
      throw new IllegalArgumentException("Given path must not be empty");

    } else if (relativePath.isAbsolute()) {
      throw new IllegalArgumentException("Given path must not be absolute");
    }

    return delegate.getFile(ResourceConverter.toEclipsePath(relativePath));
  }

  /**
   * Returns the folder delegate for the given relative path.
   *
   * @param relativePath relative path to the folder
   * @return the delegate for the given relative path
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute or empty
   * @see org.eclipse.core.resources.IContainer#getFolder(org.eclipse.core.runtime.IPath)
   */
  org.eclipse.core.resources.IFolder getFolderDelegate(Path relativePath) {
    Objects.requireNonNull(relativePath, "Given path must not be null");

    if (PathUtils.isEmpty(relativePath)) {
      throw new IllegalArgumentException("Given path must not be empty");

    } else if (relativePath.isAbsolute()) {
      throw new IllegalArgumentException("Given path must not be absolute");
    }

    return delegate.getFolder(ResourceConverter.toEclipsePath(relativePath));
  }

  /**
   * Returns a resource object representing the given resource delegate.
   *
   * @param resourceDelegate the resource delegate for which to get a resource object
   * @return a resource object representing the given resource delegate or <code>null</code> if no
   *     relative path could be constructed between the reference point delegate and the given
   *     resource delegate
   * @throws NullPointerException if the given delegate is <code>null</code>
   * @throws IllegalArgumentException if the given delegate is the reference point delegate or is
   *     not a file or a folder
   */
  IResource getResource(org.eclipse.core.resources.IResource resourceDelegate) {
    Objects.requireNonNull(resourceDelegate, "Given resource delegate must not be null");

    if (resourceDelegate.equals(delegate)) {
      throw new IllegalArgumentException(
          "Given resource delegate must not be the reference point delegate - " + delegate);
    }

    if (resourceDelegate.getType() == org.eclipse.core.resources.IResource.FILE) {
      return getFile((org.eclipse.core.resources.IFile) resourceDelegate);

    } else if (resourceDelegate.getType() == org.eclipse.core.resources.IResource.FOLDER) {
      return getFolder((org.eclipse.core.resources.IFolder) resourceDelegate);

    } else {
      log.debug(
          "Given resource delegate is not a file or a folder; can't be non-reference-point resource; found type: "
              + delegate.getType()
              + " - "
              + delegate);

      return null;
    }
  }

  /**
   * Returns a file object representing the given file delegate.
   *
   * @param fileDelegate the file delegate for which to get a file object
   * @return a file object representing the given file delegate or <code>null</code> if no relative
   *     path could be constructed between the reference point delegate and the given file delegate
   * @throws NullPointerException if the given delegate is <code>null</code>
   */
  IFile getFile(org.eclipse.core.resources.IFile fileDelegate) {
    Objects.requireNonNull(fileDelegate, "Given file delegate must not be null");

    Path relativePath = getReferencePointRelativePath(fileDelegate);

    if (relativePath == null) {
      return null;
    }

    return new EclipseFile(this, relativePath);
  }

  /**
   * Returns a folder object representing the given folder delegate.
   *
   * @param folderDelegate the folder delegate for which to get a folder object
   * @return a folder object representing the given folder delegate or <code>null</code> if no
   *     relative path could be constructed between the reference point delegate and the given
   *     folder delegate
   * @throws NullPointerException if the given delegate is <code>null</code>
   * @throws IllegalArgumentException if the given delegate is the reference point delegate
   */
  IFolder getFolder(org.eclipse.core.resources.IFolder folderDelegate) {
    Objects.requireNonNull(folderDelegate, "Given folder delegate must not be null");

    if (folderDelegate.equals(delegate)) {
      throw new IllegalArgumentException(
          "Given folder delegate must not be the reference point delegate - " + delegate);
    }

    Path relativePath = getReferencePointRelativePath(folderDelegate);

    if (relativePath == null) {
      return null;
    }

    return new EclipseFolder(this, relativePath);
  }

  /**
   * Returns the path to the given resource relative to this reference point.
   *
   * @param resource the Eclipse resource for which to get the relative path
   * @return a relative path for the given resource or <code>null</code> if there is no relative
   *     path from the reference point to the resource
   * @throws NullPointerException if the given resource is <code>null</code>
   */
  private Path getReferencePointRelativePath(org.eclipse.core.resources.IResource resource) {
    Objects.requireNonNull(resource, "Given resource must not be null");

    org.eclipse.core.runtime.IPath referencePointPath = delegate.getFullPath();
    org.eclipse.core.runtime.IPath resourcePath = resource.getFullPath();

    if (!referencePointPath.isPrefixOf(resourcePath)) {
      return null;
    }

    org.eclipse.core.runtime.IPath relativePath = resourcePath.makeRelativeTo(referencePointPath);

    if (relativePath.equals(resourcePath)) {
      return null;
    }

    return ResourceConverter.toPath(relativePath);
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + " : " + delegate + "]";
  }
}
