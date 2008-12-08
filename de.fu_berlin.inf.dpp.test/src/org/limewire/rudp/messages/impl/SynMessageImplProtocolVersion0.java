package org.limewire.rudp.messages.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.limewire.rudp.messages.MessageFormatException;
import org.limewire.rudp.messages.SynMessage;

/**
 * Begins a reliable UDP connection by pinging the other host and by
 * communicating the desired identifying connection ID.
 */
public class SynMessageImplProtocolVersion0 extends RUDPMessageImpl implements
	SynMessage {

    private final byte _senderConnectionID;
    private final short _protocolVersionNumber;

    /**
     * Construct a new SynMessage with the specified settings and data
     */
    SynMessageImplProtocolVersion0(byte connectionID) {
	super((byte) 0, OpCode.OP_SYN, 0, connectionID, (short) 0);
	_senderConnectionID = connectionID;
	_protocolVersionNumber = PROTOCOL_VERSION_NUMBER;
    }

    /**
     * Construct a new SynMessage with both my Connection ID and theirs
     */
    SynMessageImplProtocolVersion0(byte connectionID, byte theirConnectionID) {
	super(theirConnectionID, OpCode.OP_SYN, 0, connectionID, (short) 0);
	_senderConnectionID = connectionID;
	_protocolVersionNumber = PROTOCOL_VERSION_NUMBER;
    }

    /**
     * Construct a new SynMessage from the network
     */
    SynMessageImplProtocolVersion0(byte connectionId, long sequenceNumber,
	    ByteBuffer data1, ByteBuffer data2) throws MessageFormatException {
	super(OpCode.OP_SYN, connectionId, sequenceNumber, data1, data2);
	if (data1.remaining() < 3) {
	    throw new MessageFormatException(
		    "Message not long enough, message length "
			    + data1.remaining() + " < 3");
	}
	_senderConnectionID = data1.get();
	data1.order(ByteOrder.BIG_ENDIAN);
	_protocolVersionNumber = data1.getShort();
	data1.rewind();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.limewire.rudp.messages.impl.SynMessage#getSenderConnectionID()
     */
    public byte getSenderConnectionID() {
	return _senderConnectionID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.limewire.rudp.messages.impl.SynMessage#getProtocolVersionNumber()
     */
    public int getProtocolVersionNumber() {
	return _protocolVersionNumber;
    }

    @Override
    public String toString() {
	return "SynMessage DestID:" + getConnectionID() + " SrcID:"
		+ _senderConnectionID + " vNo:" + _protocolVersionNumber;
    }

    public Role getRole() {
	return Role.UNDEFINED;
    }
}
