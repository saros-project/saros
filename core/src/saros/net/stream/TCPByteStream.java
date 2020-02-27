package saros.net.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPByteStream implements ByteStream {

  private final Socket socket;
  private final String id;

  public TCPByteStream(final Socket socket, final String id) {
    if (socket == null) throw new NullPointerException("socket is null");

    this.socket = socket;
    this.id = id;
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

  @Override
  public Object getLocalAddress() {
    return socket.getLocalAddress();
  }

  @Override
  public Object getRemoteAddress() {
    return socket.getInetAddress();
  }

  @Override
  public StreamMode getMode() {
    return StreamMode.TCP;
  }

  @Override
  public String getId() {
    return id;
  }
}
