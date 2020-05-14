package saros.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class EclipseContainerImpl extends EclipseResourceImpl implements IContainer {

  EclipseContainerImpl(org.eclipse.core.resources.IContainer delegate) {
    super(delegate);
  }

  @Override
  public boolean exists(IPath relativePath) {
    return getDelegate().exists(((EclipsePathImpl) relativePath).getDelegate());
  }

  @Override
  public IResource[] members() throws IOException {
    org.eclipse.core.resources.IResource[] resources;

    try {
      resources = getDelegate().members();

      List<IResource> result = new ArrayList<IResource>(resources.length);
      ResourceAdapterFactory.convertTo(Arrays.asList(resources), result);

      return result.toArray(new IResource[0]);
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(getPath(pathString));
  }

  @Override
  public IFile getFile(IPath path) {
    return new EclipseFileImpl(getDelegate().getFile(((EclipsePathImpl) path).getDelegate()));
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(getPath(pathString));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new EclipseFolderImpl(getDelegate().getFolder(((EclipsePathImpl) path).getDelegate()));
  }

  private IPath getPath(String pathString) {
    if (pathString == null) throw new NullPointerException("Given string is null");

    Path path = new Path(pathString);

    if (path.isAbsolute())
      throw new IllegalArgumentException("Given string denotes an absolute path: " + pathString);

    return ResourceAdapterFactory.create(path);
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IContainer IContainer} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IContainer getDelegate() {
    return (org.eclipse.core.resources.IContainer) delegate;
  }
}
