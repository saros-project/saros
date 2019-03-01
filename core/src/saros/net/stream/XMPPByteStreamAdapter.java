package de.fu_berlin.inf.dpp.net.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

public class XMPPByteStreamAdapter implements ByteStream {

  private final BytestreamSession delegate;

  public XMPPByteStreamAdapter(final BytestreamSession session) {
    if (session == null) throw new NullPointerException("session is null");

    delegate = session;
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
}
