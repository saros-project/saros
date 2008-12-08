package org.limewire.rudp.messages.impl;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import junit.framework.Test;

import org.limewire.rudp.messages.RUDPMessage;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.SynMessage.Role;
import org.limewire.util.Base32;
import org.limewire.util.BaseTestCase;

public class RUDPMessagesImplTest extends BaseTestCase {
    
    public RUDPMessagesImplTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(RUDPMessagesImplTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testMessageFormat() throws Exception {
        // 3, 5, 8, 3
        byte[] ackData = Base32.decode("AMIAABIABAAAGAAAAAAAAAAAABAQCAAAAAAAA");
        // data for dataData
        byte[] bufferData = Base32.decode("VNMMPZX6DDIHKZ6UHU34ZYHHVK7TO2FQVRL755B2WS6SSOSQFSALBLJKHCNSJ2WCEKNPGDEGWIBVNQFKZWYKMBW753ERHQXZPJW6N7LAOJA2EIJQZ3CY3OX6XKZ62CXFAYK5G6Y4222UK43TREZYOGNR7RCDHNPH443Q");
        // 5, 2, bufferData
        byte[] dataData = Base32.decode("AU6AAAVLLDD6N7QY2B2WPVB5G5AQCAC2AAAABTHA46VL6N3IWCWFP7XUHK2L2KJ2KAWIBMFNFI4JWJHKYIRJV4YMQ2ZAGVWAVLG3BJQG37XMSE6C7F5G3ZX5MBZEDIRBGDHMLDN2725LH3IK4UDBLU33DTLLKRLTOOETHBYZWH6EIM5V47TTO");
        // 254, 21, 5
        byte[] finData = Base32.decode("7ZAAAFIFAAAAAAAAAAAAAAAAABAQCAAAAAAAA");
        // 203, 5810, 52
        byte[] keepAliveData = Base32.decode("ZMQAAAAWWIADIAAAAAAAAAAAABAQCAAAAAAAA");
        // 123, 153
        byte[] synData = Base32.decode("TEAAAAD3AAAAAAAAAAAAAAAAABAQCAAAAAAAA");
        
        // protocol version 1, 123, 153, role undefined
        byte[] synData1 = Base32.decode("TEAAAAD3AAAQAAAAAAAAAAAAABAQCAAAAAAAA");
        
        // protocol version 1, 123, 153, role requestor
        byte[] synData2 = Base32.decode("TEAAAAD3AAAQCAAAAAAAAAAAABAQCAAAAAAAA");
        
        // protocol version 1, 123, 153, role acceptor
        byte[] synData3 = Base32.decode("TEAAAAD3AAAQEAAAAAAAAAAAABAQCAAAAAAAA");
        
        RUDPMessageFactory factory = new DefaultMessageFactory();
        
        checkMessage(factory.createAckMessage((byte)3, 5, 8, 3),
                     factory.createMessage(ByteBuffer.wrap(ackData)),
                     ackData);
        
        checkMessage(factory.createDataMessage((byte)5, 2, ByteBuffer.wrap(bufferData)),
                     factory.createMessage(ByteBuffer.wrap(dataData)),
                     dataData);
        
        checkMessage(factory.createFinMessage((byte)254, 21, (byte)5),
                     factory.createMessage(ByteBuffer.wrap(finData)),
                     finData);
        
        checkMessage(factory.createKeepAliveMessage((byte)203, 5810, 52),
                     factory.createMessage(ByteBuffer.wrap(keepAliveData)),
                     keepAliveData);
        
        checkMessage(new SynMessageImplProtocolVersion0((byte)123, (byte)153),
                     new Version0MessageFactory().createMessage(ByteBuffer.wrap(synData)),
                     synData);
        
        checkMessage(factory.createSynMessage((byte)123, (byte)153, Role.UNDEFINED),
                factory.createMessage(ByteBuffer.wrap(synData1)),
                synData1);
        
        checkMessage(factory.createSynMessage((byte)123, (byte)153, Role.REQUESTOR),
                factory.createMessage(ByteBuffer.wrap(synData2)),
                synData2);
        
        checkMessage(factory.createSynMessage((byte)123, (byte)153, Role.ACCEPTOR),
                factory.createMessage(ByteBuffer.wrap(synData3)),
                synData3);
    
    }
    
    private void checkMessage(RUDPMessage a, RUDPMessage b, byte[] data) throws Exception {
        assertEquals(b.getClass(), a.getClass());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        a.write(out);
        byte[] ba = out.toByteArray();
        out.reset();
        b.write(out);
        byte[] bb = out.toByteArray();
        assertEquals(bb.length, ba.length);
        assertEquals(data, bb);
        assertEquals(data, ba);
        assertEquals(bb, ba);
    }
}
