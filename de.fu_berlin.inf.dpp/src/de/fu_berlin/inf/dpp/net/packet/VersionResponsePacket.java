package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VersionResponsePacket extends Packet {

    private String version;
    private byte compatibility;

    public VersionResponsePacket() {
        super(PacketType.VERSION_RESPONSE);
    }

    public VersionResponsePacket(String version) {
        super(PacketType.VERSION_RESPONSE);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeUTF(version);
        dos.write(compatibility);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        version = dis.readUTF();
        compatibility = dis.readByte();
    }

}