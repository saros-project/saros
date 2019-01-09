package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;
import org.apache.commons.lang3.NotImplementedException;

public abstract class EclipseAbstractFolderImpl extends EclipseContainerImpl implements IFolder_V2 {

  public EclipseAbstractFolderImpl(org.eclipse.core.resources.IContainer delegate) {
    super(delegate);
  }

  @Override
  public void create(int updateFlags, boolean local) throws IOException {
    throw new NotImplementedException(
        "create() is not implemented in " + this.getClass().getName());
  }

  @Override
  public void create(boolean force, boolean local) throws IOException {
    create(0, local);
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
    return new EclipseFileImpl(getDelegate().getFile(toIPath(name)));
  }

  @Override
  public IFile getFile(IPath path) {
    return new EclipseFileImpl(getDelegate().getFile(((EclipsePathImpl) path).getDelegate()));
  }

  @Override
  public IFolder getFolder(String name) {
    return new EclipseFolderImpl(getDelegate().getFolder(toIPath(name)));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new EclipseFolderImpl(getDelegate().getFolder(((EclipsePathImpl) path).getDelegate()));
  }

  private org.eclipse.core.runtime.IPath toIPath(String toPath) {
    EclipsePathFactory factory = new EclipsePathFactory();
    return ResourceAdapterFactory.convertBack(factory.fromString(toPath));
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IProject IProject} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IContainer getDelegate() {
    return (org.eclipse.core.resources.IContainer) delegate;
  }
}
