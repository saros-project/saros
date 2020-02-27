package saros.net.stream;

import java.io.IOException;
import org.jivesoftware.smack.Connection;
import saros.net.xmpp.JID;

// TODO rewrite IStreamService interface

public class TCPTransport implements IStreamService {

  @Override
  public ByteStream connect(String connectionID, JID peer)
      throws IOException, InterruptedException {

    throw new RuntimeException("NYI");
  }

  @Override
  public void initialize(Connection connection, IStreamServiceListener listener) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void uninitialize() {
    throw new RuntimeException("NYI");
  }

  @Override
  public String toString() {
    return "TCP-Transport";
  }
}
