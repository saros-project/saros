package saros.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import saros.net.IStreamConnection;
import saros.net.stream.ByteStream;
import saros.net.stream.StreamMode;

public class StreamConnection implements IStreamConnection {

  private static final Logger log = Logger.getLogger(StreamConnection.class);

  private final ByteStream byteStream;
  private final IConnectionClosedCallback callback;
  private final String id;

  private boolean isClosed;

  StreamConnection(
      final String id, final ByteStream byteStream, final IConnectionClosedCallback callback) {
    this.byteStream = byteStream;
    this.callback = callback;
    this.id = id;
  }

  @Override
  public Object getLocalAddress() {
    return byteStream.getLocalAddress();
  }

  @Override
  public Object getRemoteAddress() {
    return byteStream.getRemoteAddress();
  }

  @Override
  public StreamMode getMode() {
    return byteStream.getMode();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void close() {
    synchronized (this) {
      if (isClosed) return;

      isClosed = true;
    }

    try {
      byteStream.close();
    } catch (IOException e) {
      log.error("failed to close connection: " + this, e);
    } finally {
      if (callback != null) callback.connectionClosed(this);
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return byteStream.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return byteStream.getOutputStream();
  }

  @Override
  public int getReadTimeout() throws IOException {
    return byteStream.getReadTimeout();
  }

  @Override
  public void setReadTimeout(int timeout) throws IOException {
    byteStream.setReadTimeout(timeout);
  }

  @Override
  protected void finalize() {
    close();
  }

  @Override
  public String toString() {
    return "StreamConnection [id="
        + getId()
        + ", mode="
        + getMode()
        + ", localAddress="
        + getLocalAddress()
        + ", remoteAddress="
        + getRemoteAddress()
        + "]";
  }
}
