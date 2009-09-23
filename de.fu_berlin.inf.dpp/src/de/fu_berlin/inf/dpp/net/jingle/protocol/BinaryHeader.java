package de.fu_berlin.inf.dpp.net.jingle.protocol;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author sszuecs
 */
public class BinaryHeader implements Serializable {

    private static final long serialVersionUID = -3339204413664356412L;

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(BinaryHeader.class);

    /* The fixed size of the binary protocol header */
    public final static int HEADERSIZE = Byte.SIZE + 3 * Integer.SIZE;

    public static enum BinaryHeaderType {

        // Reject an incoming transfer
        REJECT(0x01),
        // Cancel an outgoing transfer
        CANCEL(0x02),
        // Confirm the arrival of an incoming transfer
        FINISHED(0x03),
        // Control
        SHUTDOWN(0x04),
        // Binary packet containing a transfer description
        TRANSFERDESCRIPTION(0x10),
        // Binary packet containing data
        DATA(0x11);

        byte type;

        private BinaryHeaderType(int type) {
            this.type = (byte) type;
        }

        byte toBinaryRepresentation() {
            return type;
        }

        static HashMap<Byte, BinaryHeaderType> typeMap = new HashMap<Byte, BinaryHeaderType>();

        static {
            for (BinaryHeaderType type : values()) {
                typeMap.put(type.type, type);
            }
        }

        public static BinaryHeaderType toTYPE(byte type) {
            return typeMap.get(type);
        }

    }

    /* header attributes */
    protected byte type;

    protected int remaining;

    protected int size; // size of the data without Header!

    protected int objectid;

    /**
     * 
     * @param type
     *            The BinaryProtocol.TYPE is the type of the Packet body have.
     * @param remaining
     *            The remaining of Packets required for the whole Object.
     * @param size
     *            The size of the body.
     * @param objectid
     *            The identifier of an Object.
     */
    public BinaryHeader(BinaryHeaderType type, int remaining, int size,
        int objectid) {
        super();
        this.type = type.type;
        this.remaining = remaining;
        this.size = size;
        this.objectid = objectid;
    }

    @Override
    public String toString() {
        return "binaryPacket(type:" + BinaryHeaderType.toTYPE(type)
            + ", remaining:" + remaining + ", size:" + size + ", objectid:"
            + objectid;
    }

    public byte[] toByte() {
        return toByteBuffer().array();
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(HEADERSIZE);
        buf.put(type);
        buf.putInt(remaining);
        buf.putInt(size);
        buf.putInt(objectid);

        return buf;
    }

    public boolean isReject() {
        return BinaryHeaderType.REJECT.type == type;
    }

    public boolean isTransferDescription() {
        return BinaryHeaderType.TRANSFERDESCRIPTION.type == type;
    }

    public boolean isLast() {
        return remaining == 1;
    }

    public boolean isCancel() {
        return BinaryHeaderType.CANCEL.type == type;
    }

    public BinaryHeaderType getType() {
        return BinaryHeaderType.toTYPE(type);
    }

    public static BinaryHeader getHeader(int objectid, BinaryHeaderType type) {
        return new BinaryHeader(type, 1, 0, objectid);
    }

}