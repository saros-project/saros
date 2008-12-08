package org.limewire.rudp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import junit.framework.Test;

import org.limewire.nio.observer.TransportListener;
import org.limewire.rudp.messages.RUDPMessage;
import org.limewire.rudp.messages.SynMessage;
import org.limewire.rudp.messages.SynMessage.Role;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;
import org.limewire.util.BaseTestCase;

public class UDPMultiplexorTest extends BaseTestCase {


    public UDPMultiplexorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(UDPMultiplexorTest.class);
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private static StubListener listener = new StubListener();
    private static RUDPContext context = new DefaultRUDPContext(listener);
    private static UDPSelectorProvider provider = new UDPSelectorProvider(context);
    
    
    public void testRegister() throws Exception {
        Selector selector = provider.openSelector();
        assertInstanceof(UDPMultiplexor.class, selector);
        
        SocketChannel channels[] = new SocketChannel[256];
        SelectionKey  keys[] = new SelectionKey[256];
        for(int i = 0; i < channels.length; i++) {
            channels[i] = provider.openSocketChannel();
            assertFalse(channels[i].isRegistered());
            keys[i] = channels[i].register(selector, 1);
            assertTrue(channels[i].isRegistered());
            assertSame(keys[i], channels[i].keyFor(selector));
            assertEquals(1, keys[i].interestOps());
            assertSame(selector, keys[i].selector());
        }

        Set allKeys = selector.keys();
        assertEquals(256, allKeys.size());
        for(int i = 0; i < keys.length; i++)
            assertContains(allKeys, keys[i]);
        
        // All could register except the last (they were all full)
        
        int n = selector.selectNow();
        assertEquals(1, n);
        Set selected = selector.selectedKeys();
        assertEquals(1, selected.size());
        SelectionKey key = (SelectionKey)selected.iterator().next();
        assertFalse(key.isValid());
        try {
            assertEquals(0, key.readyOps());
            fail("should have failed");
        } catch(CancelledKeyException expected) {}
        
        assertSame(keys[255], key);
 
        assertNotContains(selector.keys(), key);
    }
    
    public void testClosedChannelsRemoved() throws Exception {
        Selector selector = provider.openSelector();
        assertInstanceof(UDPMultiplexor.class, selector);
        
        SocketChannel channel = provider.openSocketChannel();
        SelectionKey key = channel.register(selector, 0);
        assertSame(key, channel.keyFor(selector));
        Set keys = selector.keys();
        assertEquals(1, keys.size());
        assertContains(keys, key);
        assertTrue(key.isValid());
        
        channel.close();
        assertFalse(key.isValid());
        assertSame(key, channel.keyFor(selector));
        // Selector still has it because no select has been performed.
        keys = selector.keys();
        assertEquals(1, keys.size());
        assertContains(keys, key);
        
        // Now do a select and it'll return remove the channel
        int n = selector.selectNow();
        assertEquals(0, n);
        
        assertEquals(0, selector.keys().size());
    }
    
    public void testSelectedKeys()  throws Exception {
        Selector selector = provider.openSelector();
        assertInstanceof(UDPMultiplexor.class, selector);
        
        StubUDPSocketChannel channel = new StubUDPSocketChannel();
        SelectionKey key = channel.register(selector, 0);
        
        assertEquals(0, selector.selectNow());
        
        key.interestOps(SelectionKey.OP_CONNECT);
        assertEquals(0, selector.selectNow());
        
        channel.setReadyOps(SelectionKey.OP_CONNECT);
        assertEquals(1, selector.selectNow());
        Set selectedKeys = selector.selectedKeys();
        assertEquals(1, selectedKeys.size());
        assertContains(selectedKeys, key);
        assertEquals(SelectionKey.OP_CONNECT, key.readyOps());
        
        key.interestOps(0);
        assertEquals(0, selector.selectNow());
        
        key.interestOps(SelectionKey.OP_READ);
        channel.setReadyOps(SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
        assertEquals(0, selector.selectNow());
        channel.setReadyOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        assertEquals(1, selector.selectNow());
        selectedKeys = selector.selectedKeys();
        assertEquals(1, selectedKeys.size());
        assertContains(selectedKeys, key);
        assertEquals(SelectionKey.OP_READ, key.readyOps());
        
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        assertEquals(1, selector.selectNow());
        selectedKeys = selector.selectedKeys();
        assertEquals(1, selectedKeys.size());
        assertContains(selectedKeys, key);
        assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.readyOps());        
    }
    
    /** 
     * Tests that if a channel becomes ready for 
     * some event, a provided listener is notified 
     **/
    public void testTransportEventGenerated() throws Exception {
    	Selector selector = provider.openSelector();
        assertInstanceof(UDPMultiplexor.class, selector);
        
        StubUDPSocketChannel channel = new StubUDPSocketChannel();
        channel.addr = new InetSocketAddress(InetAddress.getLocalHost(),1);
        SelectionKey key = channel.register(selector, 0);
        
        assertEquals(0, selector.selectNow());
        
        key.interestOps(SelectionKey.OP_CONNECT);
        SynMessage syn = new DefaultMessageFactory().createSynMessage((byte)1, Role.UNDEFINED);
        UDPMultiplexor plexor = (UDPMultiplexor) selector;
        
        StubProcessor processor = (StubProcessor)channel.getProcessor();
        assertNull(processor.msg);
        
        listener.notified = false;
        // send a message, do not change readiness.  No notification 
        plexor.routeMessage(syn, channel.addr);
        assertSame(syn,processor.msg);
        assertFalse(listener.notified);
        
        // send a message, change readiness.  Should be notified
        channel.setReadyOps(1);
        plexor.routeMessage(syn, channel.addr);
        assertTrue(listener.notified);
    }
    
    
    private static class StubUDPSocketChannel extends UDPSocketChannel {
        private StubProcessor stubProcessor = new StubProcessor(this);
        InetSocketAddress addr;
        StubUDPSocketChannel() {
            super((SelectorProvider)null, context, Role.UNDEFINED);
        }
        
        @Override
        UDPConnectionProcessor getProcessor() {
            return stubProcessor;
        }
        
        void setReadyOps(int readyOps) {
            stubProcessor.setReadyOps(readyOps);
        }

		@Override
		public InetSocketAddress getRemoteSocketAddress() {
			return addr;
		}

		@Override
		public boolean isConnectionPending() {
			return true;
		}
        
        
    }
    
    private static class StubProcessor extends UDPConnectionProcessor {
        private int readyOps;

        RUDPMessage msg;
        StubProcessor(UDPSocketChannel channel) {
            super(channel, context, Role.UNDEFINED);
        }
        
        @Override
        protected int readyOps() {
            return readyOps;
        }
        
        protected void setReadyOps(int readyOps) {
            this.readyOps = readyOps;
        }

        @Override
        protected void handleMessage(RUDPMessage msg) {
			this.msg = msg;
		}
        
    }
    
    private static class StubListener implements TransportListener {
    	boolean notified;

		public void eventPending() {
			notified = true;
		}
    }
}
