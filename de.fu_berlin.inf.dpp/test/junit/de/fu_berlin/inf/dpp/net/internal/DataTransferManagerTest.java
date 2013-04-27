package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceInitializer;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;
import de.fu_berlin.inf.dpp.test.util.TestThread;

public class DataTransferManagerTest {

    private static class Transport implements ITransport {

        private List<ChannelConnection> establishedConnections = new ArrayList<ChannelConnection>();

        private IByteStreamConnectionListener listener;

        private NetTransferMode mode;

        public Transport(NetTransferMode mode) {
            this.mode = mode;
        }

        @Override
        public synchronized IByteStreamConnection connect(JID peer)
            throws IOException, InterruptedException {
            ChannelConnection connection = new ChannelConnection(peer,
                getNetTransferMode(), listener);

            establishedConnections.add(connection);
            return connection;
        }

        public synchronized void announceIncomingRequest(JID peer) {
            ChannelConnection connection = new ChannelConnection(peer,
                getNetTransferMode(), listener);

            establishedConnections.add(connection);
            listener.connectionChanged(peer, connection, true);
        }

        @Override
        public void initialize(Connection connection,
            IByteStreamConnectionListener listener) {
            this.listener = listener;

        }

        @Override
        public void uninitialize() {
            this.listener = null;

        }

        @Override
        public NetTransferMode getNetTransferMode() {
            return mode;
        }

        public synchronized List<ChannelConnection> getEstablishedConnections() {
            return establishedConnections;
        }
    }

    private static class BlockableTransport extends Transport {

        private CountDownLatch acknowledge;

        private CountDownLatch proceed;

        public BlockableTransport(NetTransferMode mode,
            CountDownLatch acknowledge, CountDownLatch proceed) {
            super(mode);
            this.acknowledge = acknowledge;
            this.proceed = proceed;
        }

        @Override
        public IByteStreamConnection connect(JID peer) throws IOException,
            InterruptedException {
            acknowledge.countDown();
            proceed.await();
            return super.connect(peer);
        }
    }

    private static class ChannelConnection implements IByteStreamConnection {

        private JID to;
        private NetTransferMode mode;
        private IByteStreamConnectionListener listener;
        private volatile boolean closed;
        private volatile int sendPackets;

        public ChannelConnection(JID to, NetTransferMode mode,
            IByteStreamConnectionListener listener) {
            this.to = to;
            this.mode = mode;
            this.listener = listener;
        }

        @Override
        public JID getPeer() {
            return to;
        }

        @Override
        public void close() {
            closed = true;
            listener.connectionClosed(to, this);
        }

        @Override
        public boolean isConnected() {
            return !closed;
        }

        @Override
        public void send(TransferDescription data, byte[] content)
            throws IOException {
            sendPackets++;
        }

        @Override
        public NetTransferMode getMode() {
            return mode;
        }

        public int getSendPacketsCount() {
            return sendPackets;
        }
    }

    private SarosNet sarosNetStub;

    private Capture<IConnectionListener> connectionListener = new Capture<IConnectionListener>();

    private Connection connectionMock;
    {
        connectionMock = EasyMock.createMock(Connection.class);
        EasyMock.expect(connectionMock.getUser()).andReturn("local@host")
            .anyTimes();
        EasyMock.replay(connectionMock);
    }

    private SarosNet createSarosNetMock(
        Capture<IConnectionListener> connectionListener) {
        SarosNet net = EasyMock.createMock(SarosNet.class);
        net.addListener(EasyMock.and(EasyMock.isA(IConnectionListener.class),
            EasyMock.capture(connectionListener)));
        EasyMock.expectLastCall().once();
        EasyMock.replay(net);
        return net;
    }

    @Before
    public void setUp() {
        sarosNetStub = createSarosNetMock(connectionListener);
    }

    @Test(expected = IOException.class)
    public void testEstablishConnectionWithNoTransports() throws Exception {

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            null, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("foo@bar.com"));
    }

    @Test
    public void testEstablishConnectionWithMainAndFallbackTransport()
        throws Exception {

        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);
        ITransport fallbackTransport = new Transport(NetTransferMode.IBB);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("foo@bar.com"));
        assertEquals(NetTransferMode.SOCKS5_DIRECT,
            dtm.getTransferMode(new JID("foo@bar.com")));

    }

    @Test
    public void testEstablishConnectionWithMainAndFallbackTransportAndUsingFallback()
        throws Exception {

        ITransport mainTransport = EasyMock.createMock(ITransport.class);

        ITransport fallbackTransport = new Transport(NetTransferMode.IBB);

        EasyMock.expect(mainTransport.connect(EasyMock.isA(JID.class)))
            .andThrow(new IOException()).anyTimes();

        EasyMock.expect(mainTransport.getNetTransferMode())
            .andReturn(NetTransferMode.SOCKS5_DIRECT).anyTimes();

        mainTransport.initialize(EasyMock.isA(Connection.class),
            EasyMock.isA(IByteStreamConnectionListener.class));

        EasyMock.expectLastCall().once();

        EasyMock.replay(mainTransport);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("foo@bar.com"));

        EasyMock.verify(mainTransport);

        assertEquals("Wrong transport fallback", NetTransferMode.IBB,
            dtm.getTransferMode(new JID("foo@bar.com")));
    }

    @Test
    public void testForceIBBOnly() throws Exception {

        IPreferenceStore store = new MemoryPreferenceStore();
        PreferenceInitializer.setPreferences(store);
        store.setValue(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, true);

        PreferenceUtils preferenceUtil = new PreferenceUtils(store, null);

        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);
        ITransport fallbackTransport = new Transport(NetTransferMode.IBB);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, fallbackTransport, null, preferenceUtil);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("foo@bar.com"));

        assertEquals("only IBB transport should be used", NetTransferMode.IBB,
            dtm.getTransferMode(new JID("foo@bar.com")));

    }

    @Test
    public void testConnectionCaching() throws Exception {

        Transport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("foo@bar.com"));
        dtm.connect(new JID("foo@bar.de"));
        dtm.connect(new JID("foo@bar.com"));

        assertEquals("connection caching failed", 2, mainTransport
            .getEstablishedConnections().size());

        assertNotSame("connection caching failed", mainTransport
            .getEstablishedConnections().get(0), mainTransport
            .getEstablishedConnections().get(1));
    }

    @Test
    public void testGetTransferMode() throws Exception {
        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("foo@bar.com"));

        assertEquals("wrong transport mode returned",
            NetTransferMode.SOCKS5_DIRECT,
            dtm.getTransferMode(new JID("foo@bar.com")));

        assertEquals("wrong transport mode returned", NetTransferMode.NONE,
            dtm.getTransferMode(new JID("nothing@all")));

    }

    @Test
    public void testForceFallback() throws Exception {
        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);
        ITransport fallbackTransport = new Transport(NetTransferMode.IBB);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.setFallbackConnectionMode(new JID("fallback@emergency"));

        dtm.connect(new JID("foo@bar.com"));
        dtm.connect(new JID("fallback@emergency"));

        assertEquals("fallback mode enabled for the wrong connection",
            NetTransferMode.SOCKS5_DIRECT,
            dtm.getTransferMode(new JID("foo@bar.com")));

        assertEquals("fallback mode change failed", NetTransferMode.IBB,
            dtm.getTransferMode(new JID("fallback@emergency")));
    }

    @Test
    public void testClearForceFallback() throws Exception {
        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);
        ITransport fallbackTransport = new Transport(NetTransferMode.IBB);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.setFallbackConnectionMode(new JID("fallback@emergency"));

        dtm.connect(new JID("fallback@emergency"));

        assertEquals("fallback mode change failed", NetTransferMode.IBB,
            dtm.getTransferMode(new JID("fallback@emergency")));

        dtm.closeConnection(new JID("fallback@emergency"));

        dtm.connect(new JID("fallback@emergency"));

        assertEquals(
            "fallback mode was not cleared after closing the connection",
            NetTransferMode.SOCKS5_DIRECT,
            dtm.getTransferMode(new JID("fallback@emergency")));

        dtm.setFallbackConnectionMode(new JID("fallback@emergency"));

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.NOT_CONNECTED);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("fallback@emergency"));

        assertEquals(
            "fallback mode was not cleared after XMPP connection reset",
            NetTransferMode.SOCKS5_DIRECT,
            dtm.getTransferMode(new JID("fallback@emergency")));
    }

    @Test(timeout = 30000)
    public void testConcurrentConnections() throws Exception {

        final CountDownLatch connectAcknowledge = new CountDownLatch(1);
        final CountDownLatch connectProceed = new CountDownLatch(1);

        BlockableTransport mainTransport = new BlockableTransport(
            NetTransferMode.SOCKS5_DIRECT, connectAcknowledge, connectProceed);

        Transport fallbackTransport = new Transport(NetTransferMode.IBB);

        final DataTransferManager dtm = new DataTransferManager(sarosNetStub,
            null, mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        TestThread connectThread0 = new TestThread(new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                dtm.connect(new JID("foo@bar.com"));
            }
        });

        TestThread connectThread1 = new TestThread(new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                dtm.connect(new JID("foo@bar.com"));
            }
        });

        TestThread connectThread2 = new TestThread(new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                dtm.connect(new JID("foo@bar.example"));
            }
        });

        // connect here so connectThread2 will not block
        // use fallback to not trigger the blocked transport method
        dtm.setFallbackConnectionMode(new JID("foo@bar.example"));
        dtm.connect(new JID("foo@bar.example"));

        connectThread0.start();

        if (!connectAcknowledge.await(10000, TimeUnit.MILLISECONDS)) {
            connectThread0.interrupt();
            fail("transport connect method was not called");
        }

        long currentTime = System.currentTimeMillis();

        connectThread1.start();

        // poll thread status
        while (connectThread1.getState() != Thread.State.BLOCKED
            && (System.currentTimeMillis() - currentTime < 1000))
            Thread.yield();

        if (connectThread1.getState() != Thread.State.BLOCKED) {
            connectProceed.countDown();
            connectThread0.interrupt();
            connectThread1.interrupt();
            fail("second connection request must be blocked");
        }

        // This MUST not be blocked
        connectThread2.start();
        connectThread2.join(10000);

        try {
            connectThread2.verify();
        } finally {
            // release lock so the other 2 thread will not idle forever
            connectProceed.countDown();
        }

        connectThread0.join(10000);
        connectThread1.join(10000);

        connectThread0.verify();
        connectThread1.verify();

        assertEquals(
            "connection caching failed during multiple connection requests", 1,
            mainTransport.getEstablishedConnections().size());

    }

    @Test
    public void connectWithRemoteSideConnectedFirst() throws Exception {
        Transport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        mainTransport.announceIncomingRequest(new JID("fallback@emergency"));
        dtm.connect(new JID("fallback@emergency"));

        assertEquals(
            "established an outgoing connection also the remote side is already connected to the local side",
            1, mainTransport.getEstablishedConnections().size());
    }

    @Test(timeout = 30000)
    public void connectToRemoteSideWhileRemoteIsConnectingToLocalSide()
        throws Exception {

        final CountDownLatch connectAcknowledge = new CountDownLatch(1);
        final CountDownLatch connectProceed = new CountDownLatch(1);

        BlockableTransport mainTransport = new BlockableTransport(
            NetTransferMode.SOCKS5_DIRECT, connectAcknowledge, connectProceed);

        Transport fallbackTransport = new Transport(NetTransferMode.IBB);

        final DataTransferManager dtm = new DataTransferManager(sarosNetStub,
            null, mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        TestThread connectThread0 = new TestThread(new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                dtm.connect(new JID("foo@bar.com"));
            }
        });

        connectThread0.start();

        if (!connectAcknowledge.await(10000, TimeUnit.MILLISECONDS)) {
            connectThread0.interrupt();
            fail("transport connect method was not called");
        }

        fallbackTransport.announceIncomingRequest(new JID("foo@bar.com"));

        connectProceed.countDown();
        connectThread0.join(10000);
        connectThread0.verify();

        TransferDescription description = TransferDescription
            .createCustomTransferDescription();

        description.setRecipient(new JID("foo@bar.com"));

        dtm.sendData(description, new byte[0]);

        assertEquals("wrong connection was chosen", 1, mainTransport
            .getEstablishedConnections().get(0).getSendPacketsCount());

        assertEquals("wrong connection was chosen", 0, fallbackTransport
            .getEstablishedConnections().get(0).getSendPacketsCount());
    }

    @Test
    public void testConnectionClosureOnManualClose() throws Exception {
        Transport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("fallback@emergency"));
        mainTransport.announceIncomingRequest(new JID("fallback@emergency"));

        dtm.closeConnection(new JID("fallback@emergency"));

        assertFalse("outgoing connection was not closed", mainTransport
            .getEstablishedConnections().get(0).isConnected());

        assertFalse("incoming connection was not closed", mainTransport
            .getEstablishedConnections().get(1).isConnected());

        assertEquals(NetTransferMode.NONE,
            dtm.getTransferMode(new JID("fallback@emergency")));
    }

    @Test
    public void testConnectionClosureOnDisconnect() throws Exception {
        Transport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.connect(new JID("fallback@emergency"));
        mainTransport.announceIncomingRequest(new JID("fallback@emergency"));

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.NOT_CONNECTED);

        assertFalse("outgoing connection was not closed", mainTransport
            .getEstablishedConnections().get(0).isConnected());

        assertFalse("incoming connection was not closed", mainTransport
            .getEstablishedConnections().get(1).isConnected());

        assertEquals(NetTransferMode.NONE,
            dtm.getTransferMode(new JID("fallback@emergency")));
    }
}
