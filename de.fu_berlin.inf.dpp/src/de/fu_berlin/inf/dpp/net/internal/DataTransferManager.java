package de.fu_berlin.inf.dpp.net.internal;

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

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.SarosContext.Bindings.IBBTransport;
import de.fu_berlin.inf.dpp.SarosContext.Bindings.Socks5Transport;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IPacketInterceptor;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This class is responsible for handling all transfers of binary data. It
 * maintains a map of established connections and tries to reuse them.
 * 
 * @author srossbach
 * @author coezbek
 * @author jurke
 */
@Component(module = "net")
public class DataTransferManager implements IConnectionListener {
    private static final Logger log = Logger
        .getLogger(DataTransferManager.class);

    private final TransferModeDispatch transferModeDispatch = new TransferModeDispatch();

    private CopyOnWriteArrayList<IPacketInterceptor> packetInterceptors = new CopyOnWriteArrayList<IPacketInterceptor>();

    private volatile JID currentLocalJID;

    private Connection connection;

    private final IReceiver receiver;

    private final IUPnPService upnpService;

    private final ITransport mainTransport;

    private final ITransport fallbackTransport;

    private final PreferenceUtils preferenceUtils;

    private final Map<JID, ConnectionHolder> connections = Collections
        .synchronizedMap(new HashMap<JID, ConnectionHolder>());

    private final Lock connectLock = new ReentrantLock();

    private final Set<JID> currentOutgoingConnectionEstablishments = new HashSet<JID>();

    private final List<ITransport> availableTransports = new CopyOnWriteArrayList<ITransport>();

    private final IByteStreamConnectionListener byteStreamConnectionListener = new IByteStreamConnectionListener() {

        /**
         * Adds an incoming transfer.
         * 
         * @param transferObject
         *            An IncomingTransferObject that has the TransferDescription
         *            as content to provide information of the incoming transfer
         *            to upper layers.
         */
        @Override
        public void addIncomingTransferObject(
            final IncomingTransferObject transferObject) {

            final TransferDescription description = transferObject
                .getTransferDescription();

            boolean dispatchPacket = true;

            for (IPacketInterceptor packetInterceptor : packetInterceptors)
                dispatchPacket &= packetInterceptor
                    .receivedPacket(transferObject);

            if (!dispatchPacket)
                return;

            log.trace("["
                + transferObject.getTransferMode()
                + "] received incoming transfer object: "
                + description
                + ", throughput: "
                + Utils.throughput(transferObject.getCompressedSize(),
                    transferObject.getTransferDuration()));

            transferModeDispatch.transferFinished(description.getSender(),
                transferObject.getTransferMode(), true,
                transferObject.getCompressedSize(),
                transferObject.getUncompressedSize(),
                transferObject.getTransferDuration());

            receiver.processTransferObject(transferObject);
        }

        @Override
        public void connectionChanged(JID peer,
            IByteStreamConnection connection, boolean incomingRequest) {

            synchronized (connections) {
                log.debug("bytestream connection changed "
                    + connection.getMode() + " [to: " + peer + "|inc: "
                    + incomingRequest + "]");

                ConnectionHolder holder = connections.get(peer);
                if (holder == null) {
                    holder = new ConnectionHolder();
                    connections.put(peer, holder);
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
            }

            transferModeDispatch.connectionChanged(peer, connection);

            if (connection.getMode() == NetTransferMode.IBB && incomingRequest
                && upnpService != null)
                upnpService.checkAndInformAboutUPnP();
        }

        @Override
        public void connectionClosed(JID peer, IByteStreamConnection connection) {
            closeConnection(peer);
            transferModeDispatch.connectionChanged(peer, null);
        }
    };

    private static class ConnectionHolder {
        private IByteStreamConnection out;
        private IByteStreamConnection in;
    }

    public DataTransferManager(SarosNet sarosNet, IReceiver receiver,
        @Nullable @Socks5Transport ITransport mainTransport,
        @Nullable @IBBTransport ITransport fallbackTransport,
        @Nullable IUPnPService upnpService,
        @Nullable PreferenceUtils preferenceUtils) {

        this.receiver = receiver;
        this.fallbackTransport = fallbackTransport;
        this.mainTransport = mainTransport;
        this.upnpService = upnpService;
        this.preferenceUtils = preferenceUtils;
        this.initTransports();

        sarosNet.addListener(this);
    }

    /**
     * Dispatch to the used {@link BinaryChannelConnection}.
     * 
     * @throws IOException
     *             If a technical problem occurred.
     */
    public void sendData(TransferDescription transferData, byte[] payload)
        throws IOException {

        JID connectionJID = currentLocalJID;

        if (connectionJID == null)
            throw new IOException("not connected to a XMPP server");

        log.trace("sending data ... from " + connectionJID + " to "
            + transferData.getRecipient());

        JID recipient = transferData.getRecipient();
        transferData.setSender(connectionJID);

        IByteStreamConnection connection = connectInternal(recipient);

        try {

            boolean sendPacket = true;

            for (IPacketInterceptor packetInterceptor : packetInterceptors)
                sendPacket &= packetInterceptor.sendPacket(transferData,
                    payload);

            if (!sendPacket)
                return;

            long sizeUncompressed = payload.length;

            if (transferData.compressContent())
                payload = Utils.deflate(payload, null);

            long transferStartTime = System.currentTimeMillis();
            connection.send(transferData, payload);

            transferModeDispatch.transferFinished(recipient,
                connection.getMode(), false, payload.length, sizeUncompressed,
                System.currentTimeMillis() - transferStartTime);
        } catch (IOException e) {
            log.error(
                Utils.prefix(transferData.getRecipient()) + "failed to send "
                    + transferData + " with " + connection.getMode() + ":"
                    + e.getMessage(), e);
            throw e;
        }
    }

    public void connect(JID recipient) throws IOException {
        connectInternal(recipient);
    }

    public TransferModeDispatch getTransferModeDispatch() {
        return transferModeDispatch;
    }

    /**
     * Disconnects {@link IByteStreamConnection} with the specified peer
     * 
     * @param peer
     *            {@link JID} of the peer to disconnect the
     *            {@link IByteStreamConnection}
     */
    public boolean closeConnection(JID peer) {
        ConnectionHolder holder = connections.remove(peer);

        if (holder == null)
            return false;

        if (holder.out != null)
            holder.out.close();

        if (holder.in != null)
            holder.in.close();

        return holder.out != null || holder.in != null;
    }

    public NetTransferMode getTransferMode(JID jid) {
        IByteStreamConnection connection = getCurrentConnection(jid);
        return connection == null ? NetTransferMode.NONE : connection.getMode();
    }

    private IByteStreamConnection connectInternal(JID recipient)
        throws IOException {

        IByteStreamConnection connection = null;

        synchronized (currentOutgoingConnectionEstablishments) {
            if (!currentOutgoingConnectionEstablishments.contains(recipient)) {
                connection = getCurrentConnection(recipient);

                if (connection == null)
                    currentOutgoingConnectionEstablishments.add(recipient);
            }

            if (connection != null) {
                log.trace("Reuse bytestream connection " + connection.getMode());
                return connection;
            }
        }

        connectLock.lock();

        try {

            connection = getCurrentConnection(recipient);

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
                log.info("establishing connection to " + recipient.getBase()
                    + " from " + connectionJID + " using "
                    + transport.getNetTransferMode());
                try {
                    connection = transport.connect(recipient);
                    break;
                } catch (IOException e) {
                    log.error(Utils.prefix(recipient)
                        + " failed to connect using " + transport.toString()
                        + ": " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    IOException io = new InterruptedIOException(
                        "connecting cancelled: " + e.getMessage());
                    io.initCause(e);
                    throw io;
                } catch (Exception e) {
                    log.error(Utils.prefix(recipient)
                        + " failed to connect using " + transport.toString()
                        + " because of an unknown error: " + e.getMessage(), e);
                }
            }

            if (connection != null) {
                byteStreamConnectionListener.connectionChanged(recipient,
                    connection, false);

                return connection;
            }

            throw new IOException("could not connect to: "
                + Utils.prefix(recipient));
        } finally {
            synchronized (currentOutgoingConnectionEstablishments) {
                currentOutgoingConnectionEstablishments.remove(recipient);
            }
            connectLock.unlock();
        }
    }

    private void initTransports() {
        boolean forceIBBOnly = false;

        if (preferenceUtils != null)
            forceIBBOnly = preferenceUtils.forceFileTranserByChat();

        availableTransports.clear();

        if (!forceIBBOnly && mainTransport != null)
            availableTransports.add(0, mainTransport);

        if (fallbackTransport != null)
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
        transferModeDispatch.clear();

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

    /**
     * Disconnects all idle IBB connections to peers. Use to enforce attempting
     * a Socks5 connection on next data transfer.
     * 
     * @return false if not all IBBs could be closed because of ongoing
     *         transfers
     * @deprecated
     */
    @Deprecated
    public synchronized boolean disconnectInBandBytestreams() {

        /*
         * Stefan Rossbach: although a nice feature, it will not hurt anybody if
         * UPNP port mapping activation will affect only the next Saros
         * sessions.
         * 
         * Currently disabled as it is possible to close a connection that may
         * have a pending send.
         */

        return false;
        // log.info("Closing all IBBs on request");
        // List<IByteStreamConnection> openConnections;
        //
        // openConnections = new ArrayList<IByteStreamConnection>(
        // connections.values());
        //
        // boolean isTransfering = false;
        // for (IByteStreamConnection connection : openConnections) {
        // if (connection != null
        // && connection.getMode() == NetTransferMode.IBB) {
        //
        // // If this connection is currently in use, don't disconnect
        // if (isReceiving(connection.getPeer())
        // || isSending(connection.getPeer())) {
        // isTransfering = true;
        // continue;
        // }
        //
        // try {
        // connection.close();
        // log.info("Closing IBB connection to "
        // + connection.getPeer().getBareJID());
        // } catch (RuntimeException e) {
        // log.error("Error while closing " + connection.getMode()
        // + " connection ", e);
        // }
        // }
        // }
        // return isTransfering == false;
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
     * @deprecated
     * @param incomingTransferObject
     */
    @Deprecated
    public void addIncomingTransferObject(
        IncomingTransferObject incomingTransferObject) {
        byteStreamConnectionListener
            .addIncomingTransferObject(incomingTransferObject);
    }

    /**
     * Returns the current connection for the remote side. If the local side is
     * connected to the remote side as well as the remote side is connected to
     * the local side the local to remote connection will be returned.
     * 
     * @param jid
     *            JID of the remote side
     * @return the connection to the remote side or <code>null</code> if no
     *         connection exists
     */
    private IByteStreamConnection getCurrentConnection(JID jid) {
        synchronized (connections) {
            ConnectionHolder holder = connections.get(jid);

            if (holder == null)
                return null;

            if (holder.out != null)
                return holder.out;

            return holder.in;
        }
    }
}
