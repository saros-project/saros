package saros.filesystem;

public class EclipseProjectImpl extends EclipseContainerImpl implements IProject {

  EclipseProjectImpl(org.eclipse.core.resources.IProject delegate) {
    super(delegate);
  }

  @Override
  public IResource findMember(IPath path) {
    org.eclipse.core.resources.IResource resource =
        getDelegate().findMember(((EclipsePathImpl) path).getDelegate());

    if (resource == null) return null;

    return ResourceAdapterFactory.create(resource);
  }

  @Override
  public IFile getFile(String name) {
    return new EclipseFileImpl(getDelegate().getFile(name));
  }

  @Override
  public IFile getFile(IPath path) {
    return new EclipseFileImpl(getDelegate().getFile(((EclipsePathImpl) path).getDelegate()));
  }

  @Override
  public IFolder getFolder(String name) {
    return new EclipseFolderImpl(getDelegate().getFolder(name));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new EclipseFolderImpl(getDelegate().getFolder(((EclipsePathImpl) path).getDelegate()));
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
