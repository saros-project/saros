package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(version);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        version = in.readUTF();
    }

}
