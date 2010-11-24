package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * The core interface for sending anything via an XMPP connection.
 * 
 * All other methods of sending just repackage their stuff and then call this
 * function.
 */
@Component(module = "net")
public interface IXMPPTransmitter {

    public void sendMessageToProjectUser(JID recipient, PacketExtension data);

}
