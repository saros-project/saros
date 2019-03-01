/*
 * DPP - Serious Distributed Pair Programming (c) Freie Universität Berlin -
 * Fachbereich Mathematik und Informatik - 2006 (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package saros.net;

import java.io.IOException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import saros.annotations.Component;
import saros.net.xmpp.JID;

/**
 * A humble interface that is responsible for network functionality. The idea behind this interface
 * is to only encapsulates the least possible amount of functionality - the one that can't be easily
 * tested.
 *
 * @author rdjemili
 */
@Component(module = "net")
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
   * Sends the given {@link PacketExtension} to the given {@link JID} using a direct stream
   * connection. The connection must be already established to the recipient with the given id.
   *
   * @param connectionID the id of the connection
   * @param recipient the recipient of the extension
   * @param extension the extension to send
   * @throws IOException if an I/O error occurs
   */
  public void send(String connectionID, JID recipient, PacketExtension extension)
      throws IOException;
}
