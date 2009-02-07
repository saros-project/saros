package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.JID;

public interface IXMPPTransmitter {

    public void sendMessage(JID recipient, PacketExtension data);

}
