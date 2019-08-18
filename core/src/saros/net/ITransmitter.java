package saros.net;

import java.io.IOException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import saros.net.xmpp.JID;

/**
 * Interface for sending packets to remote addresses. In general this interface offers two
 * possibilities for sending packets.
 *
 * <ol>
 *   <li>Either sending packets using the default network environment by calling {@link
 *       #sendPacket(Packet)} or {@link #sendPacketExtension(JID, PacketExtension)}.
 *   <li>Using {@link #send(String, JID, PacketExtension)} using a specific connection that must
 *       first be established by calling {@link IConnectionManager#connect(String, Object)}.
 * </ol>
 *
 * The second option should always be used as default option when sending packets frequently and
 * over a longer time span to an already known address.
 *
 * <p><b>Implementation notes</b>: Implementation should consider to support connection ID's through
 * the {@link IConnectionManager}. If this is not possible the implementation <b>must</b> ensure
 * that connection lost is properly detected, i.e sending packets to a server which may route the
 * packets at a later time without getting an acknowledgement if the packet has been received is a
 * <b>violation</b> of the contract.
 *
 * @see IConnectionManager
 */
/*
 * TODO ensure we use IQ packets so the server must return an error. Afterwards we can change the contract of this interface.
 */
public interface ITransmitter {

  /**
   * @JTourBusStop 4, Architecture Overview, Network Layer - Transmitter:
   *
   * <p>The Network Layer is responsible for communicating with other participants by sending and
   * receiving messages. This Interface is the main entrance point for sending packets. (...)
   */
  /**
   * Sends the specified packet to the server.
   *
   * @param packet the packet to send
   * @throws IOException if an I/O error occurs or no connection is established to a XMPP server
   */
  public void sendPacket(Packet packet) throws IOException;

  /**
   * Sends the given {@link PacketExtension} to the given {@link JID} over the currently established
   * XMPP connection. There is <b>no</b> guarantee that this message (extension) will arrive at the
   * recipients side !
   *
   * @param jid the recipient of the extension
   * @param extension the extension to send
   */
  public void sendPacketExtension(JID jid, PacketExtension extension);

  /** @deprecated use {@link #send(String, JID, PacketExtension)} */
  @Deprecated
  public void send(JID recipient, PacketExtension extension) throws IOException;

  /**
   * Sends the given {@link PacketExtension} to the given {@link JID} using the given connection ID.
   * A connection with the given connection id must already been established.
   *
   * @param connectionID the ID of the connection
   * @param recipient the recipient of the extension
   * @param extension the extension to send
   * @throws IOException if an I/O error occurs
   * @see IConnectionManager#connect(String, Object)
   */
  public void send(String connectionID, JID recipient, PacketExtension extension)
      throws IOException;

  public default void addTransferListener(ITransferListener listener) {
    // NOP
  }

  public default void removeTransferListener(ITransferListener listener) {
    // NOP
  }

  public default void addPacketInterceptor(IPacketInterceptor interceptor) {
    // NOP
  }

  public default void removePacketInterceptor(IPacketInterceptor interceptor) {
    // NOP
  }
}
