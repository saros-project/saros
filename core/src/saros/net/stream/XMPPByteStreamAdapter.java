package saros.net.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import saros.net.xmpp.JID;

public class XMPPByteStreamAdapter implements ByteStream {

  private final BytestreamSession delegate;
  private final JID local;
  private final JID remote;
  private final String id;
  private final StreamMode mode;

  public XMPPByteStreamAdapter(
      final JID local,
      final JID remote,
      final BytestreamSession session,
      final String id,
      final StreamMode mode) {
    if (session == null) throw new NullPointerException("session is null");

    delegate = session;
    this.local = local;
    this.remote = remote;
    this.id = id;
    this.mode = mode;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return delegate.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return delegate.getOutputStream();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public int getReadTimeout() throws IOException {
    return delegate.getReadTimeout();
  }

  @Override
  public void setReadTimeout(int timeout) throws IOException {
    delegate.setReadTimeout(timeout);
  }

  @Override
  public Object getLocalAddress() {
    return local;
  }

  @Override
  public Object getRemoteAddress() {
    return remote;
  }

  @Override
  public StreamMode getMode() {
    return mode == null ? StreamMode.NONE : mode;
  }

  @Override
  public String getId() {
    return id;
  }
}
