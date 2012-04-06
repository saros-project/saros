package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VersionRequestPacket extends Packet {

    private String version;

    public VersionRequestPacket() {
        super(PacketType.VERSION_REQUEST);
    }

    public VersionRequestPacket(String version) {
        super(PacketType.VERSION_REQUEST);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeUTF(version);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        version = dis.readUTF();
    }

}
