package saros.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/** @deprecated use {@link EclipseFileImplV2} instead */
@Deprecated
public class EclipseFileImpl extends EclipseResourceImpl implements IFile {

  private static final Logger log = Logger.getLogger(EclipseFileImpl.class);

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
  public void setCharset(String charset) throws IOException {
    if (charset == null) {
      return;
    }

    // Check whether charset is valid and supported
    Charset.forName(charset);

    try {
      updateFileEncoding(charset);

    } catch (CoreException | OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  /**
   * Updates encoding of a file. An effort is made to use the inherited encoding if available.
   *
   * <p>Does nothing if the file does not exist.
   *
   * @param encoding the encoding that should be used
   * @throws CoreException if setting the encoding failed
   * @throws OperationCanceledException if the setting of the charset was canceled
   * @see org.eclipse.core.resources.IFile#setCharset(String, IProgressMonitor)
   */
  private void updateFileEncoding(final String encoding)
      throws CoreException, OperationCanceledException {

    final org.eclipse.core.resources.IFile file = getDelegate();

    if (!file.exists()) return;

    String projectEncoding = file.getProject().getDefaultCharset();
    String fileEncoding = file.getCharset();

    if (encoding.equals(fileEncoding)) {
      log.debug("encoding does not need to be changed for file: " + file);

      return;
    }

    // use inherited encoding if possible
    if (encoding.equals(projectEncoding)) {
      log.debug(
          "changing encoding for file "
              + file
              + " to use default project encoding: "
              + projectEncoding);

      file.setCharset(null, new NullProgressMonitor());

      return;
    }

    log.debug("changing encoding for file " + file + " to encoding: " + encoding);

    file.setCharset(encoding, new NullProgressMonitor());
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
  public void setContents(InputStream input) throws IOException {
    try {
      getDelegate().setContents(input, false, true, null);

    } catch (CoreException | OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void create(InputStream input) throws IOException {
    try {
      getDelegate().create(input, false, null);
    } catch (CoreException | OperationCanceledException e) {
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
