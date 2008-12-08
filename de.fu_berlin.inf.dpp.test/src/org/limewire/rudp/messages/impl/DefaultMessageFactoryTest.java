package org.limewire.rudp.messages.impl;

import java.nio.ByteBuffer;
import java.util.Random;

import junit.framework.Test;

import org.limewire.rudp.messages.MessageFormatException;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.RUDPMessage.OpCode;
import org.limewire.util.BaseTestCase;

public class DefaultMessageFactoryTest extends BaseTestCase {

    DefaultMessageFactory factory;
    
    
    
    public DefaultMessageFactoryTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        factory = new DefaultMessageFactory();
    }
    
    public static Test suite() {
        return buildTestSuite(DefaultMessageFactoryTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testCreateMessages() throws Exception {
        // empty messages that should not fail
        for (OpCode opcode : OpCode.values()) {
            ByteBuffer data = createMessage(opcode, 0);
            factory.createMessage(data);
        }
        
        // messages that have barely the length to succeed
        for (OpCode opcode : OpCode.values()) {
            ByteBuffer data = createMessage(opcode, 4);
            factory.createMessage(data);
        }
        
        // some single tests
        factory.createMessage(createMessage(OpCode.OP_SYN, 3));
        factory.createMessage(createMessage(OpCode.OP_FIN, 1));
        
        // messages that have the full length
        for (OpCode opcode : OpCode.values()) {
            ByteBuffer data = createMessage(opcode, 12);
            factory.createMessage(data);
        }
        
        // messages that are too long by one
        for (OpCode opcode : OpCode.values()) {
            ByteBuffer data = createMessage(opcode, 13);
            assertCreationFails(factory, data, MessageFormatException.class);
        }
    }
    
    public static void assertCreationFails(RUDPMessageFactory factory, ByteBuffer data, Class exception) {
        try {
            factory.createMessage(data);
            fail("Exception of type " + exception.getName() + " should have been thrown");
        }
        catch (Throwable t) {
            assertEquals(exception, t.getClass());
        }
    }
    
    private static ByteBuffer createMessage(OpCode opcode, int length) {
        ByteBuffer message = ByteBuffer.allocate(23);
        Random random = new Random();
        message.put((byte)random.nextInt());
        int opcodeAndLength = ((opcode.toByte() & 0x0F) << 4) | ((byte)length & 0x0F);
        message.put((byte)opcodeAndLength);
        message.putShort((short)random.nextInt());
        message.position(0);
        assertEquals(23, message.remaining());
        return message;
    }

}
