package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InvitationResponsePacket extends Packet {

    public static final int ACCEPTED = 0;
    public static final int REJECTED = 1;

    private int response;

    public InvitationResponsePacket() {
        super(PacketType.INVITATION_RESPONSE);
    }

    public InvitationResponsePacket(int response) {
        super(PacketType.INVITATION_RESPONSE);
        this.response = response;
    }

    public int getResponse() {
        return response;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(response);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        response = dis.readInt();
    }
}
