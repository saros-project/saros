package saros.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import saros.net.IStreamConnection;
import saros.net.stream.ByteStream;
import saros.net.stream.StreamMode;

public class DefaultStreamConnection implements IStreamConnection {

  private static final Logger log = Logger.getLogger(DefaultStreamConnection.class);

  private final ByteStream byteStream;
  private final IConnectionClosedCallback callback;

  private boolean isClosed;

  DefaultStreamConnection(final ByteStream byteStream, final IConnectionClosedCallback callback) {
    this.byteStream = byteStream;
    this.callback = callback;
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
    return byteStream.getId();
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
}
