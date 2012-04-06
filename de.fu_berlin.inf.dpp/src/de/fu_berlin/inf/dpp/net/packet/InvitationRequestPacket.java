package de.fu_berlin.inf.dpp.net.packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InvitationRequestPacket extends Packet {

    public InvitationRequestPacket() {
        super(PacketType.INVITATION_REQUEST);
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        // TODO
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        // TODO
    }
}
