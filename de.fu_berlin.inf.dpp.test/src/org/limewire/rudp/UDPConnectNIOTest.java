package org.limewire.rudp;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Random;

import junit.framework.Test;

import org.limewire.nio.AbstractNBSocket;
import org.limewire.nio.NIODispatcher;
import org.limewire.nio.channel.ChannelReadObserver;
import org.limewire.nio.channel.ChannelReader;
import org.limewire.nio.channel.InterestReadableByteChannel;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;
import org.limewire.util.BaseTestCase;
import org.limewire.util.BufferUtils;

/**
 * Tests that NIOSocket delegates events correctly.
 */
public final class UDPConnectNIOTest extends BaseTestCase {
    
    private UDPServiceStub stubService;
    private UDPMultiplexor udpMultiplexor;
    private UDPSelectorProvider udpSelectorProvider;

    private final int PORT_1 = 6346;
    private final int PORT_2 = 6348;

	public UDPConnectNIOTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(UDPConnectNIOTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
    
	@Override
    public void setUp() throws Exception {
        RUDPMessageFactory factory = new DefaultMessageFactory();
        stubService = new UDPServiceStub(factory);
        udpSelectorProvider = new UDPSelectorProvider(new DefaultRUDPContext(
                factory, NIODispatcher.instance().getTransportListener(),
                stubService, new DefaultRUDPSettings()));
        udpMultiplexor = udpSelectorProvider.openSelector();
        stubService.setUDPMultiplexor(udpMultiplexor);
        NIODispatcher.instance().registerSelector(udpMultiplexor, udpSelectorProvider.getUDPSocketChannelClass());
        
        // Add some simulated connections to the UDPServiceStub
        stubService.addReceiver(PORT_1, PORT_2, 10, 0);
        stubService.addReceiver(PORT_2, PORT_1, 10, 0);
    }
    
    @Override
    public void tearDown() throws Exception {
        // Clear out the receiver parameters for the UDPServiceStub
        stubService.clearReceivers();
        NIODispatcher.instance().removeSelector(udpMultiplexor);
    }      
    
    
    private StubConnectObserver setupConnection() throws Exception {
        AbstractNBSocket conn = udpSelectorProvider.openAcceptorSocketChannel().socket();
        StubConnectObserver stub = new StubConnectObserver();
        conn.connect(new InetSocketAddress("127.0.0.1", PORT_2), 5000, stub);
        return stub;
    }
    
    public void testDelayedGetInputStream() throws Exception {
        StubConnectObserver stub = setupConnection();
        
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", PORT_1);
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        socket.setSoTimeout(1000);
        socket.connect(addr);
        
        stub.waitForResponse(5000);
        Socket accepted = stub.getSocket();
        assertNotNull(accepted);

        byte[] rnd = new byte[100];
        new Random().nextBytes(rnd);
        accepted.getOutputStream().write(rnd); // this'll go immediately into the buffer
        
        ICROAdapter icro = new ICROAdapter();
        ByteBuffer read = icro.getReadBuffer();
        assertEquals(0, read.position());
        
        socket.setReadObserver(icro);
        Thread.sleep(500); // let NIODispatcher to its thang.
        
        assertEquals(100, read.position()); // data was transferred to the reader.
        for(int i = 0; i < 100; i++)
            assertEquals(rnd[i], read.get(i));
        
        InputStream stream = socket.getInputStream();
        byte[] readData = new byte[100];
        assertEquals(100, stream.read(readData));
        assertEquals(rnd, readData);
        
        assertEquals(0, read.position()); // moved to the InputStream
        
        new Random().nextBytes(rnd);
        accepted.getOutputStream().write(rnd); // write some more, make sure it goes to stream
        
        Thread.sleep(500);
        assertEquals(0, read.position());
        assertEquals(100, stream.read(readData));
        assertEquals(rnd, readData);
        
        socket.close();
    }
    
    public void testSetReadObserverGoesThroughChains() throws Exception {
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        
        RCROAdapter entry = new RCROAdapter();
        socket.setReadObserver(entry);
        Thread.sleep(1000);
        assertInstanceof(UDPSocketChannel.class, entry.getReadChannel());
        
        RCRAdapter chain1 = new RCRAdapter();
        entry.setReadChannel(chain1);
        socket.setReadObserver(entry);
        Thread.sleep(1000);
        assertInstanceof(UDPSocketChannel.class, chain1.getReadChannel());
        assertSame(chain1, entry.getReadChannel());
        
        RCRAdapter chain2 = new RCRAdapter();
        chain1.setReadChannel(chain2);
        socket.setReadObserver(entry);
        Thread.sleep(1000);
        assertInstanceof(UDPSocketChannel.class, chain2.getReadChannel());        
        assertSame(chain2, chain1.getReadChannel());
        assertSame(chain1, entry.getReadChannel());
    }

    
    public void testBlockingConnect() throws Exception {
        setupConnection();        
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        socket.connect(new InetSocketAddress("127.0.0.1", PORT_1));
        assertTrue(socket.isConnected());
        socket.close();
        Thread.sleep(500);
        assertFalse(socket.isConnected());
    }
    
    public void testBlockingConnectFailing() throws Exception {
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 9999));
            fail("shouldn't have connected");
        } catch(ConnectException iox) {
            assertFalse(socket.isConnected());
        }
    }
    
    public void testBlockingConnectTimesOut() throws Exception {
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 9999), 1000);
            fail("shouldn't have connected");
        } catch(SocketTimeoutException iox) {
            assertEquals("operation timed out (1000)", iox.getMessage());
        }
    }
    
    public void testNonBlockingConnect() throws Exception {
        setupConnection();
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        StubConnectObserver observer = new StubConnectObserver();
        socket.connect(new InetSocketAddress("127.0.0.1", PORT_1), 5000, observer);
        observer.waitForResponse(5500);
        assertTrue(socket.isConnected());
        assertSame(socket, observer.getSocket());
        assertFalse(observer.isShutdown());
        assertNull(observer.getIoException());
        socket.close();
        Thread.sleep(500);
        assertFalse(observer.isShutdown()); // doesn't get both connect & shutdown
        assertFalse(socket.isConnected());
    }
    
    public void testNonBlockingConnectFailing() throws Exception {
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        StubConnectObserver observer = new StubConnectObserver(); 
        

        // UDPConnectionProcessor has a default connect timeout of 20 seconds,
        // so we set our timeout at 40 seconds to ensure it doesn't fail because
        // of the timeout.
        socket.connect(new InetSocketAddress("127.0.0.1", PORT_1), 40000, observer);
        observer.waitForResponse(30000);
        
        assertTrue(observer.isShutdown());
        assertNull(observer.getSocket());
        assertNull(observer.getIoException()); // NIOSocket swallows the IOX.
        assertFalse(socket.isConnected());
    }
    
    public void testNonBlockingConnectTimesOut() throws Exception {
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        StubConnectObserver observer = new StubConnectObserver();
        socket.connect(new InetSocketAddress("127.0.0.1", PORT_1), 1000, observer);
        observer.waitForResponse(1500);
        assertTrue(observer.isShutdown());
        assertNull(observer.getSocket());
        assertNull(observer.getIoException()); // NIOSocket swallows the IOX.
        assertFalse(socket.isConnected());
    }
    
    public void testSoTimeoutUsedForNonBlockingRead() throws Exception {
        StubConnectObserver stub = setupConnection();
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", PORT_1);
        AbstractNBSocket socket = udpSelectorProvider.openSocketChannel().socket();
        socket.setSoTimeout(1000);
        socket.connect(addr);

        stub.waitForResponse(1000);
        Socket accepted = stub.getSocket();
        assertNotNull(accepted);
        accepted.getOutputStream().write(new byte[100]); // this'll go immediately into the buffer
        socket.getInputStream().read(new byte[100]);
        Thread.sleep(2000);
        assertTrue(!socket.isClosed()); // didn't close 'cause we're using stream reading

        accepted.getOutputStream().write(new byte[1]); // give it some data just to make sure it has
        socket.setReadObserver(new ReadTester());
        Thread.sleep(2000);
        assertTrue(socket.isClosed()); // closed because we switched to NB reading w/ timeout set
    }
    
    private static class ReadTester implements ChannelReadObserver {
        
        private InterestReadableByteChannel source;
        private ByteBuffer readData = ByteBuffer.allocate(128 * 1024);
        
        // ChannelReader methods.
        public InterestReadableByteChannel getReadChannel() { return source; }
        public void setReadChannel(InterestReadableByteChannel channel) { source = channel; }
        
        // IOErrorObserver methods.
        public void handleIOException(IOException x) { fail(x); }
        
        // ReadObserver methods.
        public void handleRead() throws IOException {
            source.read(readData);
            assertEquals(0, source.read(readData)); // must have finish on first read.
        }
        
        // Shutdownable methods.
        public void shutdown() {}
        
        public ByteBuffer getRead() { return (ByteBuffer)readData.flip(); }
    }
    
    private static class ICROAdapter implements ChannelReadObserver, InterestReadableByteChannel {
        private InterestReadableByteChannel source;

        private ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        public ByteBuffer getReadBuffer() {
            return buffer;
        }

        public InterestReadableByteChannel getReadChannel() {
            return source;
        }

        public void setReadChannel(InterestReadableByteChannel channel) {
            source = channel;
        }

        public int read(ByteBuffer b) {
            return BufferUtils.transfer(buffer, b);
        }

        public void close() throws IOException {
            source.close();
        }

        public boolean isOpen() {
            return source.isOpen();
        }

        public void interestRead(boolean status) {
            source.interestRead(status);
        }

        public void handleRead() throws IOException {
            while (buffer.hasRemaining() && source.read(buffer) != 0);
        }

        public void handleIOException(IOException iox) {
        }

        public void shutdown() {
        }
    }
    
    private static class RCRAdapter implements ChannelReader, InterestReadableByteChannel {
        protected InterestReadableByteChannel source;
        public InterestReadableByteChannel getReadChannel() { return source; }
        public void setReadChannel(InterestReadableByteChannel channel) { source = channel; }
        public int read(ByteBuffer b) throws IOException { return source.read(b); }
        public void close() throws IOException { source.close(); }
        public boolean isOpen() { return source.isOpen(); }
        public void interestRead(boolean status) { source.interestRead(status); }
    }
    
    private static class RCROAdapter extends RCRAdapter implements ChannelReadObserver {
        public void handleRead() throws IOException { source.read(ByteBuffer.allocate(1)); }
        public void handleIOException(IOException iox) {}
        public void shutdown() {}
    }
       
}