package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

public class EclipseFolderImpl_V2 extends EclipseAbstractFolderImpl implements IFolder {

  public EclipseFolderImpl_V2(org.eclipse.core.resources.IFolder delegate)
  {
    super(delegate);
  }

  @Override
  public void create(int updateFlags, boolean local) throws IOException {
    try {
      getDelegate().create(updateFlags, local, null);
    } catch (CoreException e) {
      throw new IOException(e);
    } catch (OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void create(boolean force, boolean local) throws IOException {
    try {
      getDelegate().create(force, local, null);
    } catch (CoreException e) {
      throw new IOException(e);
    } catch (OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IFolder IFolder} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IFolder getDelegate() {
    return (org.eclipse.core.resources.IFolder) delegate;
  }
}
