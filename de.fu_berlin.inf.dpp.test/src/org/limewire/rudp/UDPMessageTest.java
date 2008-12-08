package org.limewire.rudp;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import junit.framework.Test;

import org.limewire.rudp.messages.DataMessage;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;
import org.limewire.util.BaseTestCase;


/**
 * Unit tests for UDPConnectionMessages
 */
public class UDPMessageTest extends BaseTestCase {
        
	public UDPMessageTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(UDPMessageTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
    public void testDataMessage() throws Exception {

        // Construct a data message
        byte        connectionID   = 1;
        byte[]      data           = new byte[512];
        int         datalength     = data.length;

        SequenceNumberExtender extender = new SequenceNumberExtender(); 

        // Try a range of sequence numbers
        for( long i = 0; i <= 0x1ffff; i++) {
            writeAndReadADataMessage(connectionID, i, data, datalength, 
              extender);
        }
    }

    public void testBoundaryConditions() throws Exception {

        // Construct a data message
        byte[]      data           = new byte[512];
        int         datalength     = data.length;

        SequenceNumberExtender extender;

        // Test boundary conditions
        extender = new SequenceNumberExtender(999999999l); 
        writeAndReadADataMessage((byte)0xff, 999999999l, data, datalength, 
          extender);

        // Test 1 byte
        data = new byte[1];
        datalength = 1;
        extender = new SequenceNumberExtender(999999999l); 
        writeAndReadADataMessage((byte)0xff, 999999999l, data, datalength, 
          extender);

    }

	public void testDataSizes() throws Exception {

        // Construct a data message
        byte[]      data;
        int         datalength;

        SequenceNumberExtender extender = new SequenceNumberExtender(); 

        // Test data sizes and content
        DataMessage dmRead;
        data = new byte[0x1000];
        datalength = data.length;
        for (int i = 0; i < datalength; i++) {
            data[i] = (byte)(i % 255);
        }
        dmRead = writeAndReadADataMessage((byte)1, 1, data, datalength, 
          extender);
        for (int i = 0; i < datalength; i++) {
            assertEquals(dmRead.getDataAt(i), (byte)(i % 255));
        }
    
        // Test 2K
        data = new byte[0x2000];
        datalength = data.length;
        for (int i = 0; i < datalength; i++) {
            data[i] = (byte)(i % 255);
        }
        dmRead = writeAndReadADataMessage((byte)1, 1, data, datalength, 
          extender);
        for (int i = 0; i < datalength; i++) {
            assertEquals(dmRead.getDataAt(i), (byte)(i % 255));
        }

        // Test 4K
        data = new byte[0x4000];
        datalength = data.length;
        for (int i = 0; i < datalength; i++) {
            data[i] = (byte)(i % 255);
        }
        dmRead = writeAndReadADataMessage((byte)1, 1, data, datalength, 
          extender);
        for (int i = 0; i < datalength; i++) {
            assertEquals(dmRead.getDataAt(i), (byte)(i % 255));
        }

        // Test 8K
        data = new byte[0x8000];
        datalength = data.length;
        for (int i = 0; i < datalength; i++) {
            data[i] = (byte)(i % 255);
        }
        dmRead = writeAndReadADataMessage((byte)1, 1, data, datalength, 
          extender);
        for (int i = 0; i < datalength; i++) {
            assertEquals(dmRead.getDataAt(i), (byte)(i % 255));
        }
    
    }


    private DataMessage writeAndReadADataMessage(
      byte                   connectionID,
      long                   sequenceNumber,
      byte[]                 data,
      int                    datalength,
      SequenceNumberExtender extender) throws Exception {
        RUDPMessageFactory factory = new DefaultMessageFactory();

        DataMessage dmWrite;
        DataMessage dmRead;
        dmWrite = MessageHelper.createDataMessage(connectionID, sequenceNumber, data, datalength);

        // Write the message out
        ByteArrayOutputStream sout = new ByteArrayOutputStream();
        dmWrite.write(sout);

        // Read the message in
        ByteBuffer in = ByteBuffer.allocate(sout.size());
        in.put(sout.toByteArray());
        in.flip();
        dmRead = (DataMessage)factory.createMessage(in);

        // Extend the msgs sequenceNumber to 8 bytes based on past state
        dmRead.extendSequenceNumber(
          extender.extendSequenceNumber( dmRead.getSequenceNumber()) ); 

        assertEquals(connectionID, dmRead.getConnectionID());
        assertEquals(sequenceNumber, dmRead.getSequenceNumber());
        assertEquals(datalength, dmRead.getDataLength());

        return dmRead;
    }
	
}        
