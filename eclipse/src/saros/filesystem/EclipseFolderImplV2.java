package saros.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/** Eclipse implementation of the Saros folder interface. */
// TODO rename to EclipseFolder
public class EclipseFolderImplV2 extends EclipseResourceImplV2 implements IFolder {

  /** @see EclipseResourceImplV2#EclipseResourceImplV2(EclipseReferencePointImpl, IPath) */
  EclipseFolderImplV2(EclipseReferencePointImpl referencePoint, IPath relativePath) {
    super(referencePoint, relativePath);
  }

  @Override
  public boolean exists(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    return getDelegate().exists(((EclipsePathImpl) path).getDelegate());
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

      IPath childPath = relativePath.append(name);

      if (containedResource.getType() == org.eclipse.core.resources.IResource.FILE) {
        result.add(new EclipseFileImplV2(referencePoint, childPath));

      } else if (containedResource.getType() == org.eclipse.core.resources.IResource.FOLDER) {
        result.add(new EclipseFolderImplV2(referencePoint, childPath));

      } else {
        throw new IllegalStateException(
            "Should not be able to encounter root or project in inner tree - " + containedResource);
      }
    }

    return result;
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(ResourceConverter.convertToPath(pathString));
  }

  @Override
  public IFile getFile(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (path.segmentCount() == 0) {
      throw new IllegalArgumentException("Can not create file handle for an empty path");
    }

    IPath referencePointRelativeChildPath = relativePath.append(path);

    return new EclipseFileImplV2(referencePoint, referencePointRelativeChildPath);
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(ResourceConverter.convertToPath(pathString));
  }

  @Override
  public IFolder getFolder(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    if (path.segmentCount() == 0) {
      throw new IllegalArgumentException("Can not create folder handle for an empty path");
    }

    IPath referencePointRelativeChildPath = relativePath.append(path);

    return new EclipseFolderImplV2(referencePoint, referencePointRelativeChildPath);
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
