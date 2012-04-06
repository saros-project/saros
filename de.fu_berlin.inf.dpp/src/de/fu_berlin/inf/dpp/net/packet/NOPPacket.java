package de.fu_berlin.inf.dpp.net.packet;

import java.io.InputStream;
import java.io.OutputStream;

public class NOPPacket extends Packet {

    public NOPPacket() {
        super(PacketType.NOP);
    }

    @Override
    public void serialize(OutputStream out) {
        // NOP
    }

    @Override
    public void deserialize(InputStream in) {
        // NOP
    }
}
