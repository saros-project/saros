/**
 * 
 */
package org.limewire.rudp.messages.impl;

import java.nio.ByteBuffer;

import org.limewire.rudp.messages.MessageFormatException;
import org.limewire.rudp.messages.SynMessage;

public class Version0MessageFactory extends DefaultMessageFactory {
    @Override
    protected SynMessage createSynMessage(byte connectionID, long sequenceNumber,
            ByteBuffer data1, ByteBuffer data2) throws MessageFormatException {
        return new SynMessageImplProtocolVersion0(connectionID, sequenceNumber, data1, data2);
    }
}