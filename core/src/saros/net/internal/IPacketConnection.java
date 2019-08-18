package saros.net.internal;

import java.io.IOException;
import saros.net.IReceiver;
import saros.net.ITransmitter;

/**
 * A packet connection is internally used as an abstraction layer to coordinate the transmission of
 * and receiving of packets between the {@link IReceiver} and {@link ITransmitter}.
 */
public interface IPacketConnection extends IConnection {

  /** Sends the given data along with the given description. */
  public void send(TransferDescription description, byte[] data) throws IOException;

  /**
   * Sets the receiver for incoming {@link BinaryXMPPExtension packet extensions}.
   *
   * @param receiver
   */
  public void setBinaryXMPPExtensionReceiver(IBinaryXMPPExtensionReceiver receiver);
}
