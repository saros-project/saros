package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.packet.Packet;

public interface IPacketListener {

    public void processPacket(Packet packet);
}
