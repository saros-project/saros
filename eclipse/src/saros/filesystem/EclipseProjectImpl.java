package saros.filesystem;

import org.eclipse.core.runtime.Path;

public class EclipseProjectImpl extends EclipseContainerImpl implements IProject {

  EclipseProjectImpl(org.eclipse.core.resources.IProject delegate) {
    super(delegate);
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
   * Returns the original {@link org.eclipse.core.resources.IProject IProject} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IProject getDelegate() {
    return (org.eclipse.core.resources.IProject) delegate;
  }
}
