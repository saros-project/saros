package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.CoreException;

public abstract class EclipseContainerImpl extends EclipseResourceImpl implements IContainer {

  EclipseContainerImpl(org.eclipse.core.resources.IResource delegate) {
    super(delegate);
  }

  @Override
  public boolean exists(IPath path) {
    return getDelegate().exists(((EclipsePathImpl) path).getDelegate());
  }

  @Override
  public IResource[] members() throws IOException {
    return members(org.eclipse.core.resources.IResource.NONE);
  }

  @Override
  public IResource[] members(int memberFlags) throws IOException {
    org.eclipse.core.resources.IResource[] resources;

    try {
      resources = getDelegate().members(memberFlags);

      List<IResource> result = new ArrayList<IResource>(resources.length);
      ResourceAdapterFactory.convertTo(Arrays.asList(resources), result);

      return result.toArray(new IResource[0]);
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public String getDefaultCharset() throws IOException {
    try {
      return getDelegate().getDefaultCharset();
    } catch (CoreException e) {
      throw new IOException(e);
    }
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
