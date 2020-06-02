package saros.filesystem;

import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/** @deprecated use {@link EclipseFolderImplV2} instead */
@Deprecated
public class EclipseFolderImpl extends EclipseContainerImpl implements IFolder {

  EclipseFolderImpl(org.eclipse.core.resources.IFolder delegate) {
    super(delegate);
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
   * Returns the original {@link org.eclipse.core.resources.IFolder IFolder} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IFolder getDelegate() {
    return (org.eclipse.core.resources.IFolder) delegate;
  }
}
