package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(version);
        out.write(compatibility);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        version = in.readUTF();
        compatibility = in.readByte();
    }

}