package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.IOException;

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

public class DataTransferManagerTest {

    private static class Transport implements ITransport {

        private IByteStreamConnectionListener listener;

        private NetTransferMode mode;

        public Transport(NetTransferMode mode) {
            this.mode = mode;
        }

        @Override
        public IByteStreamConnection connect(JID peer) throws IOException,
            InterruptedException {
            return new ChannelConnection(peer, getDefaultNetTransferMode(),
                listener);
        }

        @Override
        public void prepareXMPPConnection(Connection connection,
            IByteStreamConnectionListener listener) {
            this.listener = listener;

        }

        @Override
        public void disposeXMPPConnection() {
            this.listener = null;

        }

        @Override
        public NetTransferMode getDefaultNetTransferMode() {
            return mode;
        }

    }

    private static class ChannelConnection implements IByteStreamConnection {

        private JID to;
        private NetTransferMode mode;
        private IByteStreamConnectionListener listener;
        private boolean closed;

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
            // TODO Auto-generated method stub

        }

        @Override
        public NetTransferMode getMode() {
            return mode;
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

        dtm.getConnection(new JID("foo@bar.com"));
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

        IByteStreamConnection connection = dtm.getConnection(new JID(
            "foo@bar.com"));

        assertEquals(NetTransferMode.SOCKS5_DIRECT, connection.getMode());

    }

    @Test
    public void testEstablishConnectionWithMainAndFallbackTransportAndUsingFallback()
        throws Exception {

        ITransport mainTransport = EasyMock.createMock(ITransport.class);

        ITransport fallbackTransport = new Transport(NetTransferMode.IBB);

        EasyMock.expect(mainTransport.connect(EasyMock.isA(JID.class)))
            .andThrow(new IOException()).anyTimes();

        EasyMock.expect(mainTransport.getDefaultNetTransferMode())
            .andReturn(NetTransferMode.SOCKS5_DIRECT).anyTimes();

        mainTransport.prepareXMPPConnection(EasyMock.isA(Connection.class),
            EasyMock.isA(IByteStreamConnectionListener.class));

        EasyMock.expectLastCall().once();

        EasyMock.replay(mainTransport);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, fallbackTransport, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        IByteStreamConnection connection = dtm.getConnection(new JID(
            "foo@bar.com"));

        EasyMock.verify(mainTransport);

        assertEquals("Wrong transport fallback", NetTransferMode.IBB,
            connection.getMode());
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

        IByteStreamConnection connection = dtm.getConnection(new JID(
            "foo@bar.com"));

        assertEquals(NetTransferMode.IBB, connection.getMode());
    }

    @Test
    public void testConnectionCaching() throws Exception {

        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        IByteStreamConnection connection0 = dtm.getConnection(new JID(
            "foo@bar.com"));
        IByteStreamConnection connection1 = dtm.getConnection(new JID(
            "foo@bar.de"));
        IByteStreamConnection connection2 = dtm.getConnection(new JID(
            "foo@bar.com"));

        assertSame("connection caching failed", connection0, connection2);
        assertNotSame("connection caching failed", connection1, connection2);
    }

    @Test
    public void testGetTransferMode() throws Exception {
        ITransport mainTransport = new Transport(NetTransferMode.SOCKS5_DIRECT);

        DataTransferManager dtm = new DataTransferManager(sarosNetStub, null,
            mainTransport, null, null, null);

        connectionListener.getValue().connectionStateChanged(connectionMock,
            ConnectionState.CONNECTED);

        dtm.getConnection(new JID("foo@bar.com"));

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

        assertEquals("fallback mode enabled for the wrong connection",
            NetTransferMode.SOCKS5_DIRECT,
            dtm.getConnection(new JID("foo@bar.com")).getMode());

        assertEquals("fallback mode change failed", NetTransferMode.IBB, dtm
            .getConnection(new JID("fallback@emergency")).getMode());
    }
}
