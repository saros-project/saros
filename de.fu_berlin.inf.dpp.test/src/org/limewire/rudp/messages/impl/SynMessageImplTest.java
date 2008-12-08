package org.limewire.rudp.messages.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.Test;

import org.limewire.rudp.messages.MessageFormatException;
import org.limewire.rudp.messages.RUDPMessage;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.SynMessage;
import org.limewire.rudp.messages.SynMessage.Role;
import org.limewire.util.BaseTestCase;

public class SynMessageImplTest extends BaseTestCase {

    private Version0MessageFactory version0MessageFactory;
    private DefaultMessageFactory version1MessageFactory;

    public SynMessageImplTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(SynMessageImplTest.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        version0MessageFactory = new Version0MessageFactory();
        version1MessageFactory = new DefaultMessageFactory();
    }
    
    public void testVersion0CanParseVersion1() {
        SynMessage synMessage = new SynMessageImpl((byte) 10, Role.ACCEPTOR);
        SynMessage readMessage = writeAndReparse(synMessage, version0MessageFactory);
        assertEquals(1, readMessage.getProtocolVersionNumber());
        assertEquals(10, readMessage.getSenderConnectionID());
        assertEquals(Role.UNDEFINED, readMessage.getRole());
        
        synMessage = new SynMessageImpl((byte)5, (byte)7, Role.REQUESTOR);
        readMessage = writeAndReparse(synMessage, version0MessageFactory);
        assertEquals(1, readMessage.getProtocolVersionNumber());
        assertEquals(5, readMessage.getSenderConnectionID());
        assertEquals(7, readMessage.getConnectionID());
        assertEquals(Role.UNDEFINED, readMessage.getRole());
    }
    
    public void testVersion1CanParseVersion0() {
        SynMessage synMessage = new SynMessageImplProtocolVersion0((byte) 10);
        SynMessage readMessage = writeAndReparse(synMessage, version1MessageFactory);
        assertEquals(0, readMessage.getProtocolVersionNumber());
        assertEquals(10, readMessage.getSenderConnectionID());
        assertEquals(Role.UNDEFINED, readMessage.getRole());
        
        synMessage = new SynMessageImplProtocolVersion0((byte)5, (byte)7);
        readMessage = writeAndReparse(synMessage, version1MessageFactory);
        assertEquals(0, readMessage.getProtocolVersionNumber());
        assertEquals(5, readMessage.getSenderConnectionID());
        assertEquals(7, readMessage.getConnectionID());
        assertEquals(Role.UNDEFINED, readMessage.getRole());
    }
    
    public void testVersion1CanWriteAndParseVersion1() {
        SynMessage synMessage = new SynMessageImpl((byte) 10, Role.ACCEPTOR);
        SynMessage readMessage = writeAndReparse(synMessage, version1MessageFactory);
        assertEquals(1, readMessage.getProtocolVersionNumber());
        assertEquals(10, readMessage.getSenderConnectionID());
        assertEquals(Role.ACCEPTOR, readMessage.getRole());
        
        synMessage = new SynMessageImpl((byte)5, (byte)7, Role.REQUESTOR);
        readMessage = writeAndReparse(synMessage, version1MessageFactory);
        assertEquals(1, readMessage.getProtocolVersionNumber());
        assertEquals(5, readMessage.getSenderConnectionID());
        assertEquals(7, readMessage.getConnectionID());
        assertEquals(Role.REQUESTOR, readMessage.getRole());
    }
    
    public void testRoleCanConnectTo() {
        // undefined role can connect to everything
        for (Role role : Role.values()) {
            assertTrue(Role.UNDEFINED.canConnectTo(role));
            assertTrue(role.canConnectTo(Role.UNDEFINED));
        }
        assertFalse(Role.ACCEPTOR.canConnectTo(Role.ACCEPTOR));
        assertFalse(Role.REQUESTOR.canConnectTo(Role.REQUESTOR));
        assertTrue(Role.ACCEPTOR.canConnectTo(Role.REQUESTOR));
        assertTrue(Role.REQUESTOR.canConnectTo(Role.ACCEPTOR));
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends RUDPMessage> T writeAndReparse(T message, RUDPMessageFactory factory) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.write(out);
            return (T)factory.createMessage(ByteBuffer.wrap(out.toByteArray()));
        } catch (IOException e) {
            // cannot happen
        } catch (MessageFormatException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
