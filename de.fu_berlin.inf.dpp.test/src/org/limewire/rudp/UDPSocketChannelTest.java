package org.limewire.rudp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Random;

import junit.framework.Test;

import org.limewire.rudp.messages.SynMessage.Role;
import org.limewire.util.BaseTestCase;


public class UDPSocketChannelTest extends BaseTestCase {
    public UDPSocketChannelTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(UDPSocketChannelTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testRead() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(8, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        ByteBuffer buffer = ByteBuffer.allocate(1000);        
        assertEquals(0, channel.read(buffer));

        Random rnd = new Random();
        
        // Normal read.
        byte[] data = new byte[400];
        rnd.nextBytes(data);
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data, data.length));
        assertEquals(400, channel.read(buffer));
        assertEquals(data, buffer.array(), 0, 400);
        
        assertFalse(stub.isSentKeepAlive());
    }
    
    public void testReadDataOverBufferLength() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(8, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        ByteBuffer buffer = ByteBuffer.allocate(1000);        
        assertEquals(0, channel.read(buffer));

        Random rnd = new Random();       
        byte[] data = new byte[1500];
        rnd.nextBytes(data);
        buffer.clear();
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data, data.length));
        assertEquals(1000, channel.read(buffer));
        assertEquals(data, 0, 1000, buffer.array());
        
        buffer.clear();
        assertEquals(500, channel.read(buffer));
        assertEquals(data, 1000, 500, buffer.array(), 0, 500);
        
        assertFalse(stub.isSentKeepAlive());
    }
    
    public void testReadDataInRightOrder() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(8, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        ByteBuffer buffer = ByteBuffer.allocate(1000);        
        assertEquals(0, channel.read(buffer));

        Random rnd = new Random();       
        byte[] data = new byte[103];
        byte[] data2 = new byte[105];
        rnd.nextBytes(data);
        rnd.nextBytes(data2);
        buffer.clear();
        window.addData(MessageHelper.createDataMessage((byte)0, 2, data2, data2.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data, data.length));
        assertEquals(208, channel.read(buffer));
        assertEquals(data, buffer.array(), 0, 103);
        assertEquals(data2, buffer.array(), 103, 105);
        
        assertFalse(stub.isSentKeepAlive());
    }
    
    public void testReadEOSWhenClosed() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(8, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        ByteBuffer buffer = ByteBuffer.allocate(1000);        
        assertEquals(0, channel.read(buffer));

        Random rnd = new Random();       
        byte[] data = new byte[101];
        rnd.nextBytes(data);
        buffer.clear();
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data, data.length));
        stub.close();
        assertEquals(101, channel.read(buffer));
        assertEquals(data, buffer.array(), 0, 101);
        
        assertEquals(-1, channel.read(buffer));
        
        channel.close();
        try {
            channel.read(buffer);
            fail("shouldn't have read!");
        } catch(ClosedChannelException expected) {}

        assertFalse(stub.isSentKeepAlive());
    }
    
    public void testReadSuccessiveMessages() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(8, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        ByteBuffer buffer = ByteBuffer.allocate(1000);        
        assertEquals(0, channel.read(buffer));

        Random rnd = new Random();       
        byte[] data = new byte[300];
        rnd.nextBytes(data);
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data, data.length));
        assertEquals(300, channel.read(buffer));
        assertEquals(data, buffer.array(), 0, 300);
        
        data = new byte[200];
        rnd.nextBytes(data);
        window.addData(MessageHelper.createDataMessage((byte)0, 2, data, data.length));
        assertEquals(200, channel.read(buffer));
        assertEquals(data, buffer.array(), 300, 200);    
        
        assertFalse(stub.isSentKeepAlive());
    }
    
    public void testReadTriggersKeepAlive() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(4, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        ByteBuffer buffer = ByteBuffer.allocate(1000);        
        assertEquals(0, channel.read(buffer));

        Random rnd = new Random();
        byte[] data1 = new byte[101];
        byte[] data2 = new byte[102];
        byte[] data3 = new byte[103];
        byte[] data4 = new byte[104];
        rnd.nextBytes(data1);
        rnd.nextBytes(data2);
        rnd.nextBytes(data3);
        rnd.nextBytes(data4);
        
        assertEquals(4, window.getWindowSpace());
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data1, data1.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 2, data2, data2.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 3, data3, data3.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 4, data4, data4.length));
        assertEquals(0, window.getWindowSpace());
        
        assertFalse(stub.isSentKeepAlive());
        assertEquals(410, channel.read(buffer));
        assertTrue(stub.isSentKeepAlive());
        
        assertEquals(data1, buffer.array(), 0, 101);
        assertEquals(data2, buffer.array(), 101, 102);
        assertEquals(data3, buffer.array(), 203, 103);
        assertEquals(data4, buffer.array(), 306, 104);
    }
    
    public void testReadNoSpaceDoesntTriggerKeepAlive() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(4, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        ByteBuffer buffer = ByteBuffer.allocate(0);        
        assertEquals(0, channel.read(buffer));
        byte[] data1 = new byte[101];
        byte[] data2 = new byte[102];
        byte[] data3 = new byte[103];
        byte[] data4 = new byte[104];
        
        assertEquals(4, window.getWindowSpace());
        window.addData(MessageHelper.createDataMessage((byte)0, 1, data1, data1.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 2, data2, data2.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 3, data3, data3.length));
        window.addData(MessageHelper.createDataMessage((byte)0, 4, data4, data4.length));
        assertEquals(0, window.getWindowSpace());
        
        assertFalse(stub.isSentKeepAlive());
        assertEquals(0, channel.read(buffer));
        assertFalse(stub.isSentKeepAlive());
    }
    
    public void testWrite() throws Exception {
        StubProcessor stub = new StubProcessor();
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        byte[] data = new byte[100];
        Random rnd = new Random();
        rnd.nextBytes(data);
        
        assertEquals(0, channel.getNumberOfPendingChunks());
        ByteBuffer wrapped = ByteBuffer.wrap(data);
        assertEquals(100, channel.write(wrapped));
        assertFalse(wrapped.hasRemaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        ByteBuffer written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(100, written.limit());
        assertEquals(data, written.array(), 0, 100);
        
        assertEquals(null, channel.getNextChunk());
        assertEquals(0, channel.getNumberOfPendingChunks());
    }
    
    public void testWritesCoalesced() throws Exception {
        StubProcessor stub = new StubProcessor();
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        byte[] data = new byte[100];
        Random rnd = new Random();
        rnd.nextBytes(data);
        
        assertEquals(0, channel.getNumberOfPendingChunks());
        ByteBuffer wrapped = ByteBuffer.wrap(data);
        assertEquals(100, channel.write(wrapped));
        assertFalse(wrapped.hasRemaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        byte[] data2 = new byte[300];
        rnd.nextBytes(data2);
        wrapped = ByteBuffer.wrap(data2);
        assertEquals(300, channel.write(wrapped));
        assertFalse(wrapped.hasRemaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        ByteBuffer written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(400, written.limit());
        assertEquals(data, written.array(), 0, 100);
        assertEquals(data2, written.array(), 100, 300);
        
        assertEquals(null, channel.getNextChunk());
        assertEquals(0, channel.getNumberOfPendingChunks());
    }
    
    public void testWriteStopsAtChunkLimit() throws Exception {
        StubProcessor stub = new StubProcessor();
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        assertEquals(0, stub.getChunkLimit());
        
        byte[] data = new byte[1000];
        Random rnd = new Random();
        rnd.nextBytes(data);
        assertEquals(0, channel.getNumberOfPendingChunks());
        
        ByteBuffer wrapped = ByteBuffer.wrap(data);
        assertEquals(512, channel.write(wrapped));
        assertTrue(wrapped.hasRemaining());
        assertEquals(488, wrapped.remaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        ByteBuffer written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(512, written.limit());
        assertEquals(512, written.capacity());
        assertEquals(data, 0, 512, written.array());
        
        assertEquals(null, channel.getNextChunk());
        assertEquals(0, channel.getNumberOfPendingChunks());
        
        assertEquals(488, channel.write(wrapped));
        assertFalse(wrapped.hasRemaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(488, written.limit());
        assertEquals(512, written.capacity());
        assertEquals(data, 512, 488, written.array(), 0, 488);
    }
    
    public void testWriteUpToChunkLimit() throws Exception {
        StubProcessor stub = new StubProcessor();
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        stub.setChunkLimit(7); // writes up to limit + 1
        
        byte[] data = new byte[512 * 8 + 400];
        Random rnd = new Random();
        rnd.nextBytes(data);
        assertEquals(0, channel.getNumberOfPendingChunks());
        
        ByteBuffer wrapped = ByteBuffer.wrap(data);
        assertEquals(512 * 8, channel.write(wrapped));
        assertTrue(wrapped.hasRemaining());
        assertEquals(400, wrapped.remaining());
        assertEquals(8, channel.getNumberOfPendingChunks());
        
        for(int i = 0; i < 8; i++) {
            ByteBuffer written = channel.getNextChunk();
            assertEquals(0, written.position());
            assertEquals(512, written.limit());
            assertEquals(512, written.capacity());
            assertEquals(data, i * 512, 512, written.array());
        }
        
        assertEquals(null, channel.getNextChunk());
        assertEquals(0, channel.getNumberOfPendingChunks());
        
        assertEquals(400, channel.write(wrapped));
        assertFalse(wrapped.hasRemaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        ByteBuffer written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(400, written.limit());
        assertEquals(512, written.capacity());
        assertEquals(data, 512 * 8, 400, written.array(), 0, 400);
    }
    
    public void testWriteWakesupEvents() throws Exception {
        StubProcessor stub = new StubProcessor();
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        byte[] data = new byte[100];
        Random rnd = new Random();
        rnd.nextBytes(data);
        
        assertFalse(stub.isWokeupWriteEvent());
        assertFalse(stub.isWokeupWriteEventForced());
        assertEquals(0, channel.getNumberOfPendingChunks());
        ByteBuffer wrapped = ByteBuffer.wrap(data);
        assertEquals(100, channel.write(wrapped));
        assertTrue(stub.isWokeupWriteEvent());
        assertTrue(stub.isWokeupWriteEventForced());
        
        assertFalse(wrapped.hasRemaining());
        assertEquals(1, channel.getNumberOfPendingChunks());
        stub.clear();
        
        byte[] data2 = new byte[100];
        rnd.nextBytes(data2);
        wrapped = ByteBuffer.wrap(data2);
        assertEquals(100, channel.write(wrapped));
        assertFalse(stub.isWokeupWriteEvent());
        assertFalse(stub.isWokeupWriteEventForced());        
        assertEquals(1, channel.getNumberOfPendingChunks());
        
        ByteBuffer written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(200, written.limit());
        assertEquals(data, written.array(), 0, 100);
        assertEquals(data2, written.array(), 100, 100);
        
        assertEquals(null, channel.getNextChunk());
        assertEquals(0, channel.getNumberOfPendingChunks());
        stub.clear();
        
        data = new byte[100];
        rnd.nextBytes(data);
        wrapped = ByteBuffer.wrap(data);
        assertEquals(100, channel.write(wrapped));
        assertTrue(stub.isWokeupWriteEvent());
        assertFalse(stub.isWokeupWriteEventForced());        
        assertEquals(1, channel.getNumberOfPendingChunks());
        written = channel.getNextChunk();
        assertEquals(0, written.position());
        assertEquals(100, written.limit());
        assertEquals(data, written.array(), 0, 100);      
    }
    
    public void testWriteClosed() throws Exception {
        StubProcessor stub = new StubProcessor();
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        ByteBuffer buffer = ByteBuffer.allocate(0);
        
        assertEquals(0, channel.write(buffer));
        stub.close();
        
        try {
            channel.write(buffer);
            fail("should have failed");
        } catch(ClosedChannelException expected) {}
        
        stub.setClosed(false);
        assertEquals(0, channel.write(buffer));
        
        channel.close();
        try {
            channel.write(buffer);
            fail("should have failed");
        } catch(ClosedChannelException expected) {}
    }

    public void testHasBufferedOutput() throws Exception {
        StubProcessor stub = new StubProcessor();
        DataWindow window = new DataWindow(8, 1);
        stub.setReadWindow(window);
        
        UDPSocketChannel channel = new UDPSocketChannel(stub, Role.UNDEFINED);
        
        assertFalse(channel.hasBufferedOutput());
        channel.write(ByteBuffer.allocate(1000));
        assertTrue(channel.hasBufferedOutput());
        channel.handleWrite();
        assertTrue(channel.hasBufferedOutput());
        while (channel.getNextChunk() != null);
        assertFalse(channel.hasBufferedOutput());
        channel.write(ByteBuffer.allocate(0));
        assertFalse(channel.hasBufferedOutput());
    }
    
    private static class StubProcessor extends UDPConnectionProcessor {
        private boolean closed;
        private boolean connected;
        private InetSocketAddress addr;
        private DataWindow readWindow;
        private boolean preparedOpenConnection;
        private boolean prepareOpenConnectionRetValue;
        private boolean sentKeepAlive;
        private boolean wokeupWriteEvent;
        private boolean wokeupWriteEventWasForced;
        private int chunkLimit;
        private boolean connecting;
        
        void clear() {
            closed = false;
            connected = false;
            addr = null;
            readWindow = null;
            preparedOpenConnection = false;
            prepareOpenConnectionRetValue = false;
            sentKeepAlive = false;
            wokeupWriteEvent = false;
            wokeupWriteEventWasForced = false;
            chunkLimit = 0;
            connecting = false;
        }
        
        StubProcessor() {
            super(null, new DefaultRUDPContext(), Role.UNDEFINED);
        }

        @Override
        protected void close() throws IOException {
            closed = true;
        }

        @Override
        protected void connect(InetSocketAddress addr) throws IOException {
            connected = true;
            this.addr = addr;
        }

        @Override
        protected DataWindow getReadWindow() {
            return readWindow;
        }

        @Override
        protected InetSocketAddress getSocketAddress() {
            return addr;
        }

        @Override
        protected boolean isClosed() {
            return closed;
        }

        @Override
        protected boolean isConnected() {
            return connected;
        }

        @Override
        protected boolean isConnecting() {
            return connecting;
        }

        @Override
        protected boolean prepareOpenConnection() throws IOException {
            preparedOpenConnection = true;
            return prepareOpenConnectionRetValue;
        }

        @Override
        protected void sendKeepAlive() {
            sentKeepAlive = true;
        }

        @Override
        protected void wakeupWriteEvent(boolean force) {
            wokeupWriteEvent = true;
            wokeupWriteEventWasForced = force;
        }

        @Override
        protected int getChunkLimit() {
            return chunkLimit;
        }

        public boolean isPreparedOpenConnection() {
            return preparedOpenConnection;
        }

        public boolean isSentKeepAlive() {
            return sentKeepAlive;
        }

        public boolean isWokeupWriteEvent() {
            return wokeupWriteEvent;
        }

        public boolean isWokeupWriteEventForced() {
            return wokeupWriteEventWasForced;
        }

        public void setAddr(InetSocketAddress addr) {
            this.addr = addr;
        }

        public void setChunkLimit(int chunkLimit) {
            this.chunkLimit = chunkLimit;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public void setPrepareOpenConnectionRetValue(boolean prepareOpenConnectionRetValue) {
            this.prepareOpenConnectionRetValue = prepareOpenConnectionRetValue;
        }

        public void setReadWindow(DataWindow readWindow) {
            this.readWindow = readWindow;
        }
        
        public void setConnecting(boolean connecting) {
            this.connecting = connecting;
        }
    
    }

}
