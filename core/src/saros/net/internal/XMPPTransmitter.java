/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package saros.net.internal;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import saros.annotations.Component;
import saros.net.ConnectionState;
import saros.net.ITransmitter;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;

/**
 * ITransmitter implementation using XMPP, IBB streams and Socks5 streams for sending packet
 * extensions and packets.
 */
@Component(module = "net")
public class XMPPTransmitter implements ITransmitter, IConnectionListener {

  private static final Logger log = Logger.getLogger(XMPPTransmitter.class);

  /** size in bytes that a packet extension must exceed to be compressed */
  private static final int PACKET_EXTENSION_COMPRESS_THRESHOLD =
      Integer.getInteger("saros.net.transmitter.PACKET_EXTENSION_COMPRESS_THRESHOLD", 32);

  private final DataTransferManager dataManager;

  private Connection connection;

  public XMPPTransmitter(DataTransferManager dataManager, XMPPConnectionService connectionService) {
    connectionService.addListener(this);
    this.dataManager = dataManager;
  }

  @Override
  public void send(JID recipient, PacketExtension extension) throws IOException {
    send(null, recipient, extension);
  }

  @Override
  public void send(String connectionID, JID recipient, PacketExtension extension)
      throws IOException {
    /*
     * The TransferDescription can be created out of the session, the name
     * and namespace of the packet extension and standard values and thus
     * transparent to users of this method.
     */
    TransferDescription transferDescription =
        TransferDescription.newDescription()
            .setRecipient(recipient)
            // .setSender(set by DataTransferManager)
            .setElementName(extension.getElementName())
            .setNamespace(extension.getNamespace());

    byte[] data = extension.toXML().getBytes("UTF-8");

    if (data.length > PACKET_EXTENSION_COMPRESS_THRESHOLD)
      transferDescription.setCompressContent(true);

    // recipient is included in the transfer description
    if (connectionID == null) dataManager.sendData(transferDescription, data);
    else dataManager.sendData(connectionID, transferDescription, data);
  }

  @Override
  public void sendPacketExtension(JID recipient, PacketExtension extension) {
    Message message = new Message();
    message.addExtension(extension);
    message.setTo(recipient.toString());

    assert recipient.toString().equals(message.getTo());

    try {
      sendPacket(message);
    } catch (IOException e) {
      log.error("could not send message to " + recipient, e);
    }
  }

  @Override
  public synchronized void sendPacket(Packet packet) throws IOException {

    if (isConnectionInvalid()) throw new IOException("not connected to a XMPP server");

    try {
      connection.sendPacket(packet);
    } catch (Exception e) {
      throw new IOException("could not send packet " + packet + " : " + e.getMessage(), e);
    }
  }

  /**
   * Determines if the connection can be used. Helper method for error handling.
   *
   * @return false if the connection can be used, true otherwise.
   */
  private synchronized boolean isConnectionInvalid() {
    return connection == null || !connection.isConnected();
  }

  @Override
  public synchronized void connectionStateChanged(Connection connection, ConnectionState state) {

    switch (state) {
      case CONNECTING:
        this.connection = connection;
        break;
      case ERROR:
      case NOT_CONNECTED:
        this.connection = null;
        break;
      default:
        break; // NOP
    }
  }
}
