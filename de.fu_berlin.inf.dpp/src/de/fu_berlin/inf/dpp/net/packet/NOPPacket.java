package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class NOPPacket extends Packet {

    public NOPPacket() {
        super(PacketType.NOP);
    }

    @Override
    public void serialize(DataOutputStream out) {
        //
    }

    @Override
    public void deserialize(DataInputStream in) {
        //
    }
}
