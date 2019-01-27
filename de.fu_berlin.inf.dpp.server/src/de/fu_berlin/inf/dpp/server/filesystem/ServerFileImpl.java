package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.log4j.Logger;

/*Server implementation of the {@link IFile} interface. */
public class ServerFileImpl extends ServerResourceImpl implements IFile {

  private static final Logger LOG = Logger.getLogger(ServerFileImpl.class);

  private String charset;

  /**
   * Creates a ServerFileImpl.
   *
   * @param referencePointsPath the root source's path
   * @param referencePointRelativePath the resource's path relative to root source
   */
  public ServerFileImpl(IPath referencePointsPath, IPath referencePointRelativePath) {
    super(referencePointsPath, referencePointRelativePath);
  }

  /**
   * Sets the character encoding to use when decoding this file to text. Passing <code>null</code>
   * means the default encoding of the surrounding container should be used (which is the default
   * behavior).
   *
   * @param charset the file's character encoding
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  @Override
  public int getType() {
    return FILE;
  }

  @Override
  public String getCharset() throws IOException {
    return charset != null ? charset : getParent().getDefaultCharset();
  }

  @Override
  public void delete(int updateFlags) throws IOException {
    try {
      Files.delete(toNioPath());
    } catch (NoSuchFileException e) {
      LOG.debug("Could not delete " + getFullPath() + " because it doesn't exist (ignoring)", e);
    }
  }

  @Override
  public void move(IPath destination, boolean force) throws IOException {

    IPath destinationRoot =
        destination.isAbsolute()
            ? referencePointsPath.removeLastSegments(1)
            : getLocation().removeLastSegments(1);

    IPath absoluteDestination = destinationRoot.append(destination);
    Path nioDestination = ((ServerPathImpl) absoluteDestination).getDelegate();

    Files.move(toNioPath(), nioDestination);
  }

  @Override
  public void create(InputStream input, boolean force) throws IOException {
    Path nioPath = toNioPath();

    Files.createDirectories(nioPath.getParent());
    Files.createFile(nioPath);
    setContents(input, force, false);
  }

  @Override
  public InputStream getContents() throws IOException {
    return Files.newInputStream(toNioPath());
  }

  @Override
  public void setContents(InputStream input, boolean force, boolean keepHistory)
      throws IOException {

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
