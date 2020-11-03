package saros.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import saros.util.PathUtils;

/** Eclipse implementation of the Saros folder interface. */
public class EclipseFolder extends AbstractEclipseResource implements IFolder {

  /** @see AbstractEclipseResource#AbstractEclipseResource(EclipseReferencePoint, Path) */
  EclipseFolder(EclipseReferencePoint referencePoint, Path relativePath) {
    super(referencePoint, relativePath);
  }

  @Override
  public boolean exists(Path path) {
    Objects.requireNonNull(path, "Given path must not be null");

    return getDelegate().exists(ResourceConverter.toEclipsePath(path));
  }

  @Override
  public List<IResource> members() throws IOException {
    org.eclipse.core.resources.IResource[] containedResources;

    try {
      containedResources = getDelegate().members();

    } catch (CoreException e) {
      throw new IOException(e);
    }

    List<IResource> result = new ArrayList<>(containedResources.length);

    for (org.eclipse.core.resources.IResource containedResource : containedResources) {
      String name = containedResource.getName();

      Path childPath = relativePath.resolve(name);

      if (containedResource.getType() == org.eclipse.core.resources.IResource.FILE) {
        result.add(new EclipseFile(referencePoint, childPath));

      } else if (containedResource.getType() == org.eclipse.core.resources.IResource.FOLDER) {
        result.add(new EclipseFolder(referencePoint, childPath));

      } else {
        throw new IllegalStateException(
            "Should not be able to encounter root or project in inner tree - " + containedResource);
      }
    }

    return result;
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(Paths.get(pathString));
  }

  @Override
  public IFile getFile(Path path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (PathUtils.isEmpty(path)) {
      throw new IllegalArgumentException("Can not create file handle for an empty path");
    }

    Path referencePointRelativeChildPath = relativePath.resolve(path);

    return new EclipseFile(referencePoint, referencePointRelativeChildPath);
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(Paths.get(pathString));
  }

  @Override
  public IFolder getFolder(Path path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (PathUtils.isEmpty(path)) {
      throw new IllegalArgumentException("Can not create folder handle for an empty path");
    }

    Path referencePointRelativeChildPath = relativePath.resolve(path);

    return new EclipseFolder(referencePoint, referencePointRelativeChildPath);
  }

  @Override
  public void create() throws IOException {
    try {
      getDelegate().create(false, true, null);

    } catch (CoreException | OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  /**
   * Returns an {@link org.eclipse.core.resources.IFolder} object representing this folder.
   *
   * @return an {@link org.eclipse.core.resources.IFolder} object representing this folder
   */
  @Override
  public org.eclipse.core.resources.IFolder getDelegate() {
    return referencePoint.getFolderDelegate(relativePath);
  }
}
