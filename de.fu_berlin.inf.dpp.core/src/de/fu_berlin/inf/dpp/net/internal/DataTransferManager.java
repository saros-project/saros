package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.ISarosContextBindings.IBBTransport;
import de.fu_berlin.inf.dpp.ISarosContextBindings.Socks5Transport;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionMode;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.IPacketInterceptor;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransferListener;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;

/**
 * This class is responsible for handling all transfers of binary data. It
 * maintains a map of established connections and tries to reuse them.
 * 
 * @author srossbach
 * @author coezbek
 * @author jurke
 */
// FIXME it is currently not possible to configure the transport modes (result
// of a move to Saros core)
@Component(module = "net")
public class DataTransferManager implements IConnectionListener,
    IConnectionManager {

    private static final Logger log = Logger
        .getLogger(DataTransferManager.class);

    private static final int CHUNKSIZE = 16 * 1024;

    private static final String DEFAULT_CONNECTION_ID = "default";

    private final CopyOnWriteArrayList<IPacketInterceptor> packetInterceptors = new CopyOnWriteArrayList<IPacketInterceptor>();

    private final List<ITransferListener> transferListeners = new CopyOnWriteArrayList<ITransferListener>();

    private volatile JID currentLocalJID;

    private Connection connection;

    private int transportMask = -1;

    private final IReceiver receiver;

    private final ITransport mainTransport;

    private final ITransport fallbackTransport;

    private final Map<String, ConnectionHolder> connections = Collections
        .synchronizedMap(new HashMap<String, ConnectionHolder>());

    private final Lock connectLock = new ReentrantLock();

    private final Set<String> currentOutgoingConnectionEstablishments = new HashSet<String>();

    private final List<ITransport> availableTransports = new CopyOnWriteArrayList<ITransport>();

    private final IByteStreamConnectionListener byteStreamConnectionListener = new IByteStreamConnectionListener() {

        @Override
        public void receive(final BinaryXMPPExtension extension) {

            final TransferDescription description = extension
                .getTransferDescription();

            boolean dispatchPacket = true;

            for (IPacketInterceptor packetInterceptor : packetInterceptors)
                dispatchPacket &= packetInterceptor.receivedPacket(extension);

            if (!dispatchPacket)
                return;

            if (log.isTraceEnabled())
                log.trace("[" + extension.getTransferMode()
                    + "] received incoming transfer object: " + description
                    + ", size: " + extension.getCompressedSize()
                    + ", RX time: " + extension.getTransferDuration() + " ms");

            if (extension.getTransferDescription().compressContent()) {
                byte[] payload = extension.getPayload();
                long compressedPayloadLenght = payload.length;

                try {
                    payload = inflate(payload);
                } catch (IOException e) {
                    log.error("could not decompress transfer object payload", e);
                    return;
                }

                // FIXME change method signature
                extension.setPayload(compressedPayloadLenght, payload);
            }

            notifyDataReceived(extension.getTransferMode(),
                extension.getCompressedSize(), extension.getUncompressedSize(),
                extension.getTransferDuration());

            receiver.processBinaryXMPPExtension(extension);
        }

        @Override
        public void connectionChanged(String connectionID, JID peer,
            IByteStreamConnection connection, boolean incomingRequest) {

            synchronized (connections) {
                log.debug("bytestream connection changed "
                    + connection.getMode() + " [to: " + peer + "|inc: "
                    + incomingRequest + "|id: " + connectionID + "]");

                ConnectionHolder holder = connections.get(toConnectionIDToken(
                    connectionID, peer));
                if (holder == null) {
                    holder = new ConnectionHolder();
                    connections.put(toConnectionIDToken(connectionID, peer),
                        holder);
                }

                if (!incomingRequest) {
                    IByteStreamConnection old = holder.out;
                    assert (old == null || !old.isConnected());
                    holder.out = connection;
                } else {
                    IByteStreamConnection old = holder.in;
                    assert (old == null || !old.isConnected());
                    holder.in = connection;
                }

                connection.initialize();
            }
        }

        @Override
        public void connectionClosed(String connectionID, JID peer,
            IByteStreamConnection connection) {
            closeConnection(connectionID, peer);
        }
    };

    private static class ConnectionHolder {
        private IByteStreamConnection out;
        private IByteStreamConnection in;
    }

    public DataTransferManager(XMPPConnectionService connectionService,
        IReceiver receiver,
        @Nullable @Socks5Transport ITransport mainTransport,
        @Nullable @IBBTransport ITransport fallbackTransport) {

        this.receiver = receiver;
        this.fallbackTransport = fallbackTransport;
        this.mainTransport = mainTransport;
        this.initTransports();

        connectionService.addListener(this);
    }

    @Override
    public void addTransferListener(ITransferListener listener) {
        transferListeners.add(listener);
    }

    @Override
    public void removeTransferListener(ITransferListener listener) {
        transferListeners.remove(listener);
    }

    public void sendData(String connectionID,
        TransferDescription transferDescription, byte[] payload)
        throws IOException {

        JID connectionJID = currentLocalJID;

        if (connectionJID == null)
            throw new IOException("not connected to a XMPP server");

        IByteStreamConnection connection = getCurrentConnection(connectionID,
            transferDescription.getRecipient());

        if (connection == null)
            throw new IOException("not connected to "
                + transferDescription.getRecipient()
                + " [connection identifier=" + connectionID + "]");

        if (log.isTraceEnabled())
            log.trace("sending data " + transferDescription + " from "
                + connectionJID + " to " + transferDescription.getRecipient()
                + "[connection identifier=" + connectionID + "]");

        transferDescription.setSender(connectionJID);
        sendInternal(connection, transferDescription, payload);
    }

    /**
     * @deprecated establishes connections on demand
     * @param transferDescription
     * @param payload
     * @throws IOException
     */
    @Deprecated
    public void sendData(TransferDescription transferDescription, byte[] payload)
        throws IOException {

        JID connectionJID = currentLocalJID;

        if (connectionJID == null)
            throw new IOException("not connected to a XMPP server");

        if (log.isTraceEnabled())
            log.trace("sending data ... from " + connectionJID + " to "
                + transferDescription.getRecipient());

        JID recipient = transferDescription.getRecipient();
        transferDescription.setSender(connectionJID);

        sendInternal(connectInternal(DEFAULT_CONNECTION_ID, recipient),
            transferDescription, payload);
    }

    private void sendInternal(IByteStreamConnection connection,
        TransferDescription transferData, byte[] payload) throws IOException {
        try {

            boolean sendPacket = true;

            for (IPacketInterceptor packetInterceptor : packetInterceptors)
                sendPacket &= packetInterceptor.sendPacket(transferData,
                    payload);

            if (!sendPacket)
                return;

            long sizeUncompressed = payload.length;

            if (transferData.compressContent())
                payload = deflate(payload);

            long transferStartTime = System.currentTimeMillis();
            connection.send(transferData, payload);

            notifyDataSent(connection.getMode(), payload.length,
                sizeUncompressed, System.currentTimeMillis()
                    - transferStartTime);

        } catch (IOException e) {
            log.error(
                transferData.getRecipient() + " failed to send " + transferData
                    + " with " + connection.getMode() + ":" + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public void connect(JID peer) throws IOException {
        connect(DEFAULT_CONNECTION_ID, peer);
    }

    @Override
    public void connect(String connectionID, JID peer) throws IOException {
        if (connectionID == null)
            throw new NullPointerException("connectionID is null");

        if (peer == null)
            throw new NullPointerException("peer is null");

        connectInternal(connectionID, peer);
    }

    /**
     * @deprecated Disconnects {@link IByteStreamConnection} with the specified
     *             peer
     * 
     * @param peer
     *            {@link JID} of the peer to disconnect the
     *            {@link IByteStreamConnection}
     */
    @Override
    @Deprecated
    public boolean closeConnection(JID peer) {
        return closeConnection(DEFAULT_CONNECTION_ID, peer);
    }

    @Override
    public boolean closeConnection(String connectionIdentifier, JID peer) {
        ConnectionHolder holder = connections.remove(toConnectionIDToken(
            connectionIdentifier, peer));

        if (holder == null)
            return false;

        if (holder.out != null)
            holder.out.close();

        if (holder.in != null)
            holder.in.close();

        return holder.out != null || holder.in != null;

    }

    /**
     * {@inheritDoc} The transports will be used on the next successful
     * connection to a XMPP server and will not affect the transports that are
     * currently used.
     * 
     */
    @Override
    public synchronized void setTransport(int transportMask) {
        this.transportMask = transportMask;
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public ConnectionMode getTransferMode(JID jid) {
        return getTransferMode(null, jid);
    }

    @Override
    public ConnectionMode getTransferMode(String connectionID, JID jid) {
        IByteStreamConnection connection = getCurrentConnection(connectionID,
            jid);
        return connection == null ? ConnectionMode.NONE : connection.getMode();
    }

    private IByteStreamConnection connectInternal(String connectionID, JID peer)
        throws IOException {

        IByteStreamConnection connection = null;

        String connectionIDToken = toConnectionIDToken(connectionID, peer);

        synchronized (currentOutgoingConnectionEstablishments) {
            if (!currentOutgoingConnectionEstablishments
                .contains(connectionIDToken)) {
                connection = getCurrentConnection(connectionID, peer);

                if (connection == null)
                    currentOutgoingConnectionEstablishments
                        .add(connectionIDToken);
            }

            if (connection != null) {
                if (log.isTraceEnabled())
                    log.trace("Reuse bytestream connection "
                        + connection.getMode());

                return connection;
            }
        }

        connectLock.lock();

        try {

            connection = getCurrentConnection(connectionID, peer);

            if (connection != null)
                return connection;

            JID connectionJID = currentLocalJID;

            if (connectionJID == null)
                throw new IOException("not connected to a XMPP server");

            ArrayList<ITransport> transportModesToUse = new ArrayList<ITransport>(
                availableTransports);

            log.debug("currently used IP addresses for Socks5Proxy: "
                + Arrays.toString(Socks5Proxy.getSocks5Proxy()
                    .getLocalAddresses().toArray()));

            for (ITransport transport : transportModesToUse) {
                log.info("establishing connection to " + peer.getBase()
                    + " from " + connectionJID + " using " + transport);
                try {
                    connection = transport.connect(connectionID, peer);
                    break;
                } catch (IOException e) {
                    log.error(peer + " failed to connect using " + transport
                        + ": " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    IOException io = new InterruptedIOException(
                        "connecting cancelled: " + e.getMessage());
                    io.initCause(e);
                    throw io;
                } catch (Exception e) {
                    log.error(peer + " failed to connect using " + transport
                        + " because of an unknown error: " + e.getMessage(), e);
                }
            }

            if (connection != null) {
                byteStreamConnectionListener.connectionChanged(connectionID,
                    peer, connection, false);

                return connection;
            }

            throw new IOException("could not connect to: " + peer);
        } finally {
            synchronized (currentOutgoingConnectionEstablishments) {
                currentOutgoingConnectionEstablishments
                    .remove(connectionIDToken);
            }
            connectLock.unlock();
        }
    }

    private void initTransports() {
        boolean useIBB;
        boolean useSocks5;

        synchronized (this) {
            useIBB = (transportMask & IBB_TRANSPORT) != 0;
            useSocks5 = (transportMask & SOCKS5_TRANSPORT) != 0;
        }

        availableTransports.clear();

        if (useSocks5 && mainTransport != null)
            availableTransports.add(mainTransport);

        if (useIBB && fallbackTransport != null)
            availableTransports.add(fallbackTransport);

        log.debug("used transport order for the current XMPP connection: "
            + Arrays.toString(availableTransports.toArray()));

    }

    /**
     * Sets up the transports for the given XMPPConnection
     */
    private void prepareConnection(final Connection connection) {
        assert (this.connection == null);

        initTransports();

        this.connection = connection;
        this.currentLocalJID = new JID(connection.getUser());

        for (ITransport transport : availableTransports) {
            transport.initialize(connection, byteStreamConnectionListener);
        }
    }

    private void disposeConnection() {

        currentLocalJID = null;

        boolean acquired = false;

        try {
            acquired = connectLock.tryLock(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            acquired = false;
        }

        try {
            for (ITransport transport : availableTransports)
                transport.uninitialize();
        } finally {
            if (acquired)
                connectLock.unlock();
        }

        List<ConnectionHolder> currentConnections;

        synchronized (connections) {
            currentConnections = new ArrayList<ConnectionHolder>();

            for (ConnectionHolder holder : connections.values()) {
                ConnectionHolder current = new ConnectionHolder();
                current.out = holder.out;
                current.in = holder.in;
                currentConnections.add(current);
            }
        }

        /*
         * Just close one side as this will trigger closeConnection via the
         * listener which will close the other side too
         */

        for (ConnectionHolder holder : currentConnections) {
            IByteStreamConnection connection;

            if (holder.out != null)
                connection = holder.out;
            else
                connection = holder.in;

            assert (connection != null);

            log.trace("closing " + connection.getMode() + " connection");

            try {
                connection.close();
            } catch (Exception e) {
                log.error("error closing " + connection.getMode()
                    + " connection ", e);
            }
        }

        if (connections.size() > 0)
            log.warn("new connections were established during connection shutdown: "
                + connections.toString());

        connections.clear();

        connection = null;
    }

    @Override
    public void connectionStateChanged(Connection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED)
            prepareConnection(connection);
        else if (this.connection != null)
            disposeConnection();
    }

    // TODO: move to ITransmitter
    public void addPacketInterceptor(IPacketInterceptor interceptor) {
        packetInterceptors.addIfAbsent(interceptor);
    }

    // TODO: move to IReceiver
    public void removePacketInterceptor(IPacketInterceptor interceptor) {
        packetInterceptors.remove(interceptor);
    }

    /**
     * Left over and <b>MUST</b> only used by the STF
     * 
     * @param extension
     * @deprecated
     */
    @Deprecated
    public void addIncomingTransferObject(BinaryXMPPExtension extension) {
        byteStreamConnectionListener.receive(extension);
    }

    /**
     * Returns the current connection for the remote side. If the local side is
     * connected to the remote side as well as the remote side is connected to
     * the local side the local to remote connection will be returned.
     * 
     * @param connectionID
     *            identifier for the connection to retrieve or <code>null</code>
     *            to retrieve the default one
     * @param jid
     *            JID of the remote side
     * @return the connection to the remote side or <code>null</code> if no
     *         connection exists
     */
    private IByteStreamConnection getCurrentConnection(String connectionID,
        JID jid) {
        synchronized (connections) {
            ConnectionHolder holder = connections.get(toConnectionIDToken(
                connectionID, jid));

            if (holder == null)
                return null;

            if (holder.out != null)
                return holder.out;

            return holder.in;
        }
    }

    private static String toConnectionIDToken(String connectionIdentifier,
        JID jid) {

        if (connectionIdentifier == null)
            connectionIdentifier = DEFAULT_CONNECTION_ID;

        return connectionIdentifier.concat(":").concat(jid.toString());
    }

    private void notifyDataSent(final ConnectionMode mode,
        final long sizeCompressed, final long sizeUncompressed,
        final long duration) {

        for (final ITransferListener listener : transferListeners) {
            try {
                listener.sent(mode, sizeCompressed, sizeUncompressed, duration);
            } catch (RuntimeException e) {
                log.error("invoking sent() on listener: " + listener
                    + " failed", e);
            }
        }
    }

    private void notifyDataReceived(final ConnectionMode mode,
        final long sizeCompressed, final long sizeUncompressed,
        final long duration) {

        for (final ITransferListener listener : transferListeners) {
            try {
                listener.received(mode, sizeCompressed, sizeUncompressed,
                    duration);
            } catch (RuntimeException e) {
                log.error("invoking received() on listener: " + listener
                    + " failed", e);
            }
        }
    }

    private static byte[] deflate(byte[] input) {

        Deflater compressor = new Deflater(Deflater.DEFLATED);
        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[CHUNKSIZE];

        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        return bos.toByteArray();
    }

    private static byte[] inflate(byte[] input) throws IOException {

        ByteArrayOutputStream bos;
        Inflater decompressor = new Inflater();

        decompressor.setInput(input, 0, input.length);
        bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[CHUNKSIZE];

        try {
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            return bos.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException("failed to inflate data", e);
        }
    }
}
