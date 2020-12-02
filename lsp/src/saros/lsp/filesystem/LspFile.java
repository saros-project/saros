package saros.lsp.filesystem;

import static saros.filesystem.IResource.Type.FILE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.log4j.Logger;
import saros.filesystem.IFile;
import saros.filesystem.IPath;

/**
 * Saros language server implementation of {@link IFile}.
 *
 * @implNote Based on the server implementation
 */
public class LspFile extends LspResource implements IFile {

  private final Logger LOG = Logger.getLogger(LspFile.class);

  private static final String DEFAULT_CHARSET = "UTF-8";

  private String charset;

  /**
   * Creates a ServerFileImpl.
   *
   * @param workspace the containing workspace
   * @param path the file's path relative to the workspace's root
   */
  public LspFile(IWorkspacePath workspace, IPath path) {
    super(workspace, path);
  }

  /**
   * Sets the character encoding to use when decoding this file to text. Passing <code>null</code>
   * means the default encoding of the surrounding container should be used (which is the default
   * behavior).
   *
   * @param charset the file's character encoding
   */
  @Override
  public void setCharset(String charset) {
    this.charset = charset;
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
  public void delete() throws IOException {
    try {
      Files.delete(toNioPath());
    } catch (NoSuchFileException e) {
      LOG.debug("Could not delete " + getLocation() + " because it doesn't exist (ignoring)", e);
    }
  }

  @Override
  public void create(InputStream input) throws IOException {
    Path nioPath = toNioPath();

    Files.createDirectories(nioPath.getParent());
    Files.createFile(nioPath);
    setContents(input);
  }

  @Override
  public InputStream getContents() throws IOException {
    return Files.newInputStream(toNioPath());
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
          toNioPath(),
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(tempFilePath, toNioPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Override
  public long getSize() throws IOException {
    return (long) Files.getAttribute(toNioPath(), "basic:size");
  }
}
