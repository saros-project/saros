package de.fu_berlin.inf.dpp.net.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPByteStream implements ByteStream {

  private final Socket socket;

  public TCPByteStream(final Socket socket) {
    if (socket == null) throw new NullPointerException("socket is null");

    this.socket = socket;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return socket.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return socket.getOutputStream();
  }

  @Override
  public void close() throws IOException {
    socket.close();
  }

  @Override
  public int getReadTimeout() throws IOException {
    return socket.getSoTimeout();
  }

  @Override
  public void setReadTimeout(int timeout) throws IOException {
    socket.setSoTimeout(timeout);
  }
}
