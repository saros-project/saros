package de.fu_berlin.inf.dpp.net.jingle.protocol;

import java.io.Serializable;
import java.nio.ByteBuffer;

import de.fu_berlin.inf.dpp.net.jingle.protocol.BinaryHeader.BinaryHeaderType;

/**
 * @author sszuecs
 */
public class BinaryPacket implements Serializable {

    private static final long serialVersionUID = 3782035022179033322L;

    protected BinaryHeader head;
    protected byte[] body;

    public BinaryPacket(BinaryHeader head, byte[] body) {
        super();
        this.head = head;
        this.body = body;
    }

    public BinaryPacket(BinaryHeaderType type, int count, int size,
        int objectid, byte[] body) {
        super();
        this.head = new BinaryHeader(type, count, size, objectid);
        this.body = body;
    }

    /**
     * Returns the size of the Packet body.
     */
    protected int getSize() {
        return head.size;
    }

    /**
     * Returns the size of the whole Packet.
     */
    protected int getPacketSize() {
        return head.size + BinaryHeader.HEADERSIZE;
    }

    protected int getObjectID() {
        return head.objectid;
    }

    public boolean isReject() {
        return head.isReject();
    }

    public boolean isTransferDescription() {
        return head.isTransferDescription();
    }

    @Override
    public String toString() {
        return head.toString();
    }

    public byte[] toByteArray() {
        ByteBuffer buf = head.toByteBuffer();
        return buf.put(body).array();
    }

    public boolean isCancel() {
        return head.isCancel();
    }

    public BinaryHeaderType getType() {
        return head.getType();
    }

    public static BinaryPacket create(int objectid, BinaryHeaderType type) {
        return new BinaryPacket(BinaryHeader.getHeader(objectid, type),
            new byte[0]);
    }

    public boolean isLast() {
        return head.isLast();
    }
}