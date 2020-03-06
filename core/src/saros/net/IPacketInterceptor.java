package saros.net;

import org.jivesoftware.smack.packet.PacketExtension;
import saros.net.internal.BinaryXMPPExtension;
import saros.net.xmpp.JID;

/**
 * An interface for intercepting packages that are about to send or dispatched via the {@link
 * ITransmitter} and {@link IReceiver}
 */
public interface IPacketInterceptor {

  /**
   * This method is called before the {@link IReceiver} is dispatching the packet.
   *
   * @param extension
   * @return <code>true</code> if the packet should be dispatched, <code>false</code> if the packet
   *     should be dropped
   */
  public boolean receivedPacket(BinaryXMPPExtension extension);

  /**
   * This method is called before the {@link ITransmitter} is sending the packet.
   *
   * @param connectId
   * @param recipient
   * @param extension
   * @return <code>true</code> if the packet should be send, <code>false</code> if the packet should
   *     be dropped
   */
  public boolean sendPacket(String connectId, JID recipient, PacketExtension extension);
}
