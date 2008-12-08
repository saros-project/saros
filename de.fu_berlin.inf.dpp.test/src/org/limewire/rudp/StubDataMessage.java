package org.limewire.rudp;

import java.nio.ByteBuffer;

import org.limewire.rudp.messages.DataMessage;

public class StubDataMessage extends StubRUDPMessage implements DataMessage {

    public StubDataMessage(int sequenceNumber) {
        setSequenceNumber(sequenceNumber);
    }

    public StubDataMessage() {
    }

    public ByteBuffer getChunk() {
        return null;
    }

    public ByteBuffer getData1Chunk() {
        return null;
    }

    public ByteBuffer getData2Chunk() {
        return null;
    }

    public byte getDataAt(int i) {
        return 0;
    }

}
