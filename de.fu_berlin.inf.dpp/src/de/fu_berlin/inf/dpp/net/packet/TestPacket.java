package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestPacket extends Packet {

    private byte[] testData;

    public TestPacket() {
        super(PacketType.TEST);
    }

    public TestPacket(byte[] testData) {
        super(PacketType.TEST);
        this.testData = testData;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(testData.length);
        dos.write(testData);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int length = dis.readInt();
        System.err.println(length);
        testData = new byte[length];
        dis.readFully(testData);
    }

    public byte[] getData() {
        return testData;
    }
}
