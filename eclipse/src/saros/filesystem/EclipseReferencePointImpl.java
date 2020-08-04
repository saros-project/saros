package saros.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;

/** Eclipse implementation of the Saros reference point interface. */
// TODO rename to EclipseReferencePoint
public class EclipseReferencePointImpl implements IReferencePoint {
  private static Logger log = Logger.getLogger(EclipseReferencePointImpl.class);

  /** The delegate represented by the reference point. */
  private final org.eclipse.core.resources.IContainer delegate;

  /**
   * Instantiates an Eclipse reference point object.
   *
   * @param delegate the delegate resource represented by the reference point
   * @throws NullPointerException if the given delegate is <code>null</code>
   * @throws IllegalArgumentException if the given delegate does not exist
   */
  public EclipseReferencePointImpl(org.eclipse.core.resources.IContainer delegate) {
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
  public EclipsePathImpl getReferencePointRelativePath() {
    return new EclipsePathImpl(org.eclipse.core.runtime.Path.EMPTY);
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
  public boolean exists(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    return delegate.exists(((EclipsePathImpl) path).getDelegate());
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

      IPath childPath = ResourceConverter.convertToPath(name);

      if (containedResource.getType() == org.eclipse.core.resources.IResource.FILE) {
        result.add(new EclipseFileImplV2(this, childPath));

      } else if (containedResource.getType() == org.eclipse.core.resources.IResource.FOLDER) {
        result.add(new EclipseFolderImplV2(this, childPath));

      } else {
        throw new IllegalStateException(
            "Encounter root or project in inner tree - " + containedResource);
      }
    }

    return result;
  }

  @Override
  public saros.filesystem.IFile getFile(String pathString) {
    return getFile(ResourceConverter.convertToPath(pathString));
  }

  @Override
  public saros.filesystem.IFile getFile(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (path.segmentCount() == 0) {
      throw new IllegalArgumentException("Can not create file handle for an empty path");
    }

    return new EclipseFileImplV2(this, path);
  }

  @Override
  public saros.filesystem.IFolder getFolder(String pathString) {
    return getFolder(ResourceConverter.convertToPath(pathString));
  }

  @Override
  public saros.filesystem.IFolder getFolder(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (path.segmentCount() == 0) {
      throw new IllegalArgumentException("Can not create folder handle for an empty path");
    }

    return new EclipseFolderImplV2(this, path);
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
  org.eclipse.core.resources.IFile getFileDelegate(IPath relativePath) {
    Objects.requireNonNull(relativePath, "Given path must not be null");

    if (relativePath.segmentCount() == 0) {
      throw new IllegalArgumentException("Given path must not be empty");

    } else if (relativePath.isAbsolute()) {
      throw new IllegalArgumentException("Given path must not be absolute");
    }

    return delegate.getFile(((EclipsePathImpl) relativePath).getDelegate());
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
  org.eclipse.core.resources.IFolder getFolderDelegate(IPath relativePath) {
    Objects.requireNonNull(relativePath, "Given path must not be null");

    if (relativePath.segmentCount() == 0) {
      throw new IllegalArgumentException("Given path must not be empty");

    } else if (relativePath.isAbsolute()) {
      throw new IllegalArgumentException("Given path must not be absolute");
    }

    return delegate.getFolder(((EclipsePathImpl) relativePath).getDelegate());
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

    IPath relativePath = getReferencePointRelativePath(fileDelegate);

    if (relativePath == null) {
      return null;
    }

    return new EclipseFileImplV2(this, relativePath);
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

    IPath relativePath = getReferencePointRelativePath(folderDelegate);

    if (relativePath == null) {
      return null;
    }

    return new EclipseFolderImplV2(this, relativePath);
  }

  /**
   * Returns the path to the given resource relative to this reference point.
   *
   * @param resource the Eclipse resource for which to get the relative path
   * @return a relative path for the given resource or <code>null</code> if there is no relative
   *     path from the reference point to the resource
   * @throws NullPointerException if the given resource is <code>null</code>
   */
  private IPath getReferencePointRelativePath(org.eclipse.core.resources.IResource resource) {
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

    return ResourceConverter.convertToPath(relativePath);
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + " : " + delegate + "]";
  }
}
