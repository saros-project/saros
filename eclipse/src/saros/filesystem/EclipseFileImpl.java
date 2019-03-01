package saros.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

public class EclipseFileImpl extends EclipseResourceImpl implements IFile {

  EclipseFileImpl(org.eclipse.core.resources.IFile delegate) {
    super(delegate);
  }

  @Override
  public String getCharset() throws IOException {
    try {
      return getDelegate().getCharset();
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public InputStream getContents() throws IOException {
    try {
      return getDelegate().getContents();
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void setContents(InputStream input, boolean force, boolean keepHistory)
      throws IOException {
    try {
      getDelegate().setContents(input, force, keepHistory, null);
    } catch (CoreException e) {
      throw new IOException(e);
    } catch (OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void create(InputStream input, boolean force) throws IOException {
    try {
      getDelegate().create(input, force, null);
    } catch (CoreException e) {
      throw new IOException(e);
    } catch (OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IFile IFile} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IFile getDelegate() {
    return (org.eclipse.core.resources.IFile) delegate;
  }

  @Override
  public long getSize() throws IOException {
    URI uri = getDelegate().getLocationURI();

    if (uri != null) {
      try {
        return EFS.getStore(uri).fetchInfo().getLength();
      } catch (CoreException e) {
        throw new IOException(e);
      }
    }

    return 0;
  }
}
