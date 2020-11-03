package saros.server.filesystem;

import static saros.filesystem.IResource.Type.FILE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.log4j.Logger;
import saros.filesystem.IFile;

/** Server implementation of the {@link IFile} interface. */
public class ServerFileImpl extends ServerResourceImpl implements IFile {

  private static final Logger log = Logger.getLogger(ServerFileImpl.class);

  private static final String DEFAULT_CHARSET = "UTF-8";

  private String charset;

  /**
   * Creates a ServerFileImpl.
   *
   * @param workspace the containing workspace
   * @param path the file's path relative to the workspace's root
   */
  public ServerFileImpl(ServerWorkspaceImpl workspace, Path path) {
    super(workspace, path);
  }

  @Override
  public Type getType() {
    return FILE;
  }

  @Override
  public String getCharset() {
    // TODO remove once #912 is resolved as this will no longer be necessary
    if (charset == null) {
      return DEFAULT_CHARSET;
    }

    return charset;
  }

  @Override
  public void setCharset(String charset)
      throws IllegalCharsetNameException, UnsupportedCharsetException {

    if (charset == null) {
      return;
    }

    // Check whether charset is valid and supported
    Charset.forName(charset);

    this.charset = charset;
  }

  @Override
  public void delete() throws IOException {
    try {
      Files.delete(getLocation());
    } catch (NoSuchFileException e) {
      log.debug("Could not delete " + getFullPath() + " because it doesn't exist (ignoring)", e);
    }
  }

  @Override
  public void create(InputStream input) throws IOException {
    Path nioPath = getLocation();

    Files.createDirectories(nioPath.getParent());
    Files.createFile(nioPath);
    setContents(input);
  }

  @Override
  public InputStream getContents() throws IOException {
    return Files.newInputStream(getLocation());
  }

  @Override
  public void setContents(InputStream input) throws IOException {

    /*
     * We write the new contents to a temporary file first, then move that
     * file atomically to this file's location. This ensures that
     * setContents() as a whole is atomic: should something go wrong, the
     * file either has the old or new content, but never anything in
     * between. Similarly, the stream returned by getContents() will never
     * observe an inconsistent state while a setContents() is in progress.
     *
     * On Windows, this technique has the additional benefit of not having
     * to open the "real" file, thus not locking it during the write.
     */

    Path tempFilePath = Files.createTempFile(getName(), null);

    if (input != null) {
      Files.copy(input, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    try {
      Files.move(
          tempFilePath,
          getLocation(),
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(tempFilePath, getLocation(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Override
  public long getSize() throws IOException {
    return (long) Files.getAttribute(getLocation(), "basic:size");
  }
}
