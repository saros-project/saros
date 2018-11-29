package de.fu_berlin.inf.dpp.net.stream;

import de.fu_berlin.inf.dpp.net.internal.BinaryChannelConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import org.jivesoftware.smack.Connection;

// TODO rewrite IStreamService interface

public class TCPTransport implements IStreamService {

  private volatile IByteStreamConnectionListener currentListener;

  @Override
  public IByteStreamConnection connect(String connectionID, JID peer)
      throws IOException, InterruptedException {

    if (true) throw new RuntimeException("NYI");

    // TODO this should be configurable;

    final Socket socket = new Socket(Proxy.NO_PROXY);

    final InetSocketAddress address = new InetSocketAddress("localhost", 4711);

    socket.connect(address, 30000);
    socket.setTcpNoDelay(true);

    final IByteStreamConnectionListener listener = currentListener;

    if (listener == null) {
      socket.close();
      throw new IOException(this + " transport is not initialized");
    }

    final IByteStreamConnection connection =
        new BinaryChannelConnection(
            null, peer, connectionID, new TCPByteStream(socket), StreamMode.TCP, listener);

    return connection;
  }

  @Override
  public void initialize(Connection connection, IByteStreamConnectionListener listener) {

    currentListener = listener;
  }

  @Override
  public void uninitialize() {
    currentListener = null;
  }

  @Override
  public String toString() {
    return "TCP-Transport";
  }
}
