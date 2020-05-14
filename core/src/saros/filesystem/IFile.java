package saros.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;

/**
 * Represents a handle for a file in the (virtual) file system.
 *
 * <p>The referenced file do not necessarily have to exist in the local filesystem.
 */
public interface IFile extends IResource {

  /**
   * Returns the charset for the file.
   *
   * @return the charset for the file
   * @throws IOException if the charset could not be read
   */
  String getCharset() throws IOException;

  /**
   * Sets the given character set for this file.
   *
   * <p>Does nothing if the passed character set is <code>null</code>.
   *
   * @param charset the character set to set
   * @throws IOException if the character set could not be set
   * @throws IllegalCharsetNameException if the given character set name is not valid
   * @throws UnsupportedEncodingException if the given character set is not supported by the local
   *     JVM
   */
  void setCharset(String charset) throws IOException;

  /**
   * Returns an input stream containing the content of the file.
   *
   * @return an input stream containing the content of the file
   * @throws IOException if the file does not exist or its contents could not be read
   */
  InputStream getContents() throws IOException;

  /**
   * Writes the content of the given input stream into the file. Any existing file content is
   * overwritten.
   *
   * <p>Passing <code>null</code> as the input stream will result in the current content being
   * dropped without any replacement, resulting in an empty file.
   *
   * @param input an input stream to write into the file or <code>null</code> if the current content
   *     should just be dropped
   * @throws IOException if file does not exist or the content could not be written to the file
   */
  void setContents(InputStream input) throws IOException;

  /**
   * Creates the this file with the given content.
   *
   * <p>Passing <code>null</code> as the input stream will result in the file being created empty.
   *
   * @param input an input stream to write into the file or <code>null</code> if the file should be
   *     created without any content
   * @throws IOException if the file already exists, could not be created, or the content of the
   *     newly created file could not be set
   */
  void create(InputStream input) throws IOException;

  /**
   * Returns the size of the file in bytes.
   *
   * @return the size of the file in bytes
   * @throws IOException if the file does not exist or its contents could not be read
   */
  long getSize() throws IOException;

  default Type getType() {
    return Type.FILE;
  }
}
