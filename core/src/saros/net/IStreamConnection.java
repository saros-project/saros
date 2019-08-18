package saros.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import saros.net.internal.IConnection;

/**
 * A stream connection consists of an input and out stream. It up to the caller to gracefully
 * shutdown the connections.
 */
public interface IStreamConnection extends IConnection {

  /**
   * Gets the input stream to receive data.
   *
   * @return the input stream to receive data
   * @throws IOException if an I/O error occurs
   */
  public InputStream getInputStream() throws IOException;

  /**
   * Gets the output stream to send data.
   *
   * @return the output stream to send data
   * @throws IOException if an I/O error occurs
   */
  public OutputStream getOutputStream() throws IOException;

  /**
   * Returns the read timeout in milliseconds.
   *
   * @return the read timeout
   * @throws IOException if an I/O error occurs
   */
  public int getReadTimeout() throws IOException;

  /**
   * Sets the read timeout in milliseconds.
   *
   * @throws IOException if an I/O error occurs
   */
  public void setReadTimeout(int timeout) throws IOException;
}
