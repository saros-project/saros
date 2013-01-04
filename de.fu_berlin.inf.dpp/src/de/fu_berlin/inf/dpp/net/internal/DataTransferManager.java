package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * @author coezbek
 * @author jurke
 */
@Component(module = "net")
public class DataTransferManager implements IConnectionListener {
    private static final Logger log = Logger
        .getLogger(DataTransferManager.class);
    /**
     * Maps JIDs to a list of currently running incoming transfers - receptions
     */
    private Map<JID, List<TransferDescription>> incomingTransfers = new HashMap<JID, List<TransferDescription>>();

    /**
     * Maps JIDs to the number of currently running, outgoing transfers - send
     */
    private Map<JID, Integer> outgoingTransfers = new HashMap<JID, Integer>();

    private TransferModeDispatch transferModeDispatch = new TransferModeDispatch();

    private CopyOnWriteArrayList<IPacketInterceptor> packetInterceptors = new CopyOnWriteArrayList<IPacketInterceptor>();

    private JID currentLocalJID;

    private Connection connection;

    private IReceiver receiver;

    private IUPnPService upnpService;

    private ITransport mainTransport;

    private ITransport fallbackTransport;

    private PreferenceUtils preferenceUtils;

    private List<ITransport> transports = null;

    private Map<JID, IByteStreamConnection> connections = Collections
        .synchronizedMap(new HashMap<JID, IByteStreamConnection>());

    private final Object connectLock = new Object();
    /**
     * Collection of {@link JID}s, flagged to prefer IBB transfer mode
     */
    private Collection<JID> peersForIBB = new ArrayList<JID>();

    private final IByteStreamConnectionListener byteStreamConnectionListener = new IByteStreamConnectionListener() {

        /**
         * Adds an incoming transfer.
         * 
         * @param transferObjectDescription
         *            An IncomingTransferObject that has the TransferDescription
         *            as content to provide information of the incoming transfer
         *            to upper layers.
         */
        @Override
        public void addIncomingTransferObject(
            final IncomingTransferObject transferObjectDescription) {

            final TransferDescription description = transferObjectDescription
                .getTransferDescription();

            final IncomingTransferObject transferObjectData = new LoggingTransferObject(
                transferObjectDescription);

            boolean dispatchPacket = true;

            for (IPacketInterceptor packetInterceptor : packetInterceptors)
                dispatchPacket &= packetInterceptor
                    .receivedPacket(transferObjectDescription);

            if (!dispatchPacket)
                return;

            receiver.processIncomingTransferObject(description,
                transferObjectData);
        }

        @Override
        public synchronized void connectionChanged(JID peer,
            IByteStreamConnection connection, boolean incomingRequest) {
            // TODO: remove these lines
            IByteStreamConnection old = connections.get(peer);
            assert (old == null || !old.isConnected());

            log.debug("Bytestream connection changed " + connection.getMode());
            connections.put(peer, connection);
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

    private final class LoggingTransferObject implements IncomingTransferObject {

        private final IncomingTransferObject transferObject;
        private final TransferDescription description;

        private LoggingTransferObject(IncomingTransferObject transferObject) {
            this.transferObject = transferObject;
            this.description = transferObject.getTransferDescription();
        }

        @Override
        public byte[] accept() throws IOException {
            addIncomingFileTransfer(description);
            try {
                // TODO Put size in TransferDescription, so we can
                // display it here

                log.trace("[" + getTransferMode()
                    + "] Starting incoming data transfer: " + description);

                long startTime = System.currentTimeMillis();

                byte[] content = transferObject.accept();

                long duration = System.currentTimeMillis() - startTime;

                log.trace("[" + getTransferMode()
                    + "] Finished incoming data transfer: " + description
                    + ", Throughput: "
                    + Utils.throughput(getTransferredSize(), duration));

                transferModeDispatch.transferFinished(description.getSender(),
                    getTransferMode(), true,
                    transferObject.getTransferredSize(),
                    transferObject.getUncompressedSize(), duration);

                return content;

            } finally {
                removeIncomingFileTransfer(description);
            }
        }

        @Override
        public TransferDescription getTransferDescription() {
            return description;
        }

        @Override
        public NetTransferMode getTransferMode() {
            return transferObject.getTransferMode();
        }

        @Override
        public long getTransferredSize() {
            return transferObject.getTransferredSize();
        }

        @Override
        public long getUncompressedSize() {
            return transferObject.getUncompressedSize();
        }
    }

    /**
     * Dispatch to the used {@link BinaryChannelConnection}.
     * 
     * @throws IOException
     *             If a technical problem occurred.
     */
    public void sendData(TransferDescription transferData, byte[] payload)
        throws IOException {

        log.trace("sending data ... from " + currentLocalJID + " to "
            + transferData.getRecipient());

        JID recipient = transferData.getRecipient();
        transferData.setSender(currentLocalJID);

        IByteStreamConnection connection = getConnection(recipient);

        synchronized (outgoingTransfers) {
            Integer currentSendingOperations = outgoingTransfers.get(recipient);

            currentSendingOperations = currentSendingOperations == null ? 0
                : currentSendingOperations;

            outgoingTransfers.put(recipient, currentSendingOperations + 1);
        }

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
            log.error(Utils.prefix(transferData.getRecipient())
                + "Failed to send " + transferData + " with "
                + connection.getMode().toString() + ":" + e.getMessage() + ":",
                e.getCause());
            throw e;
        } finally {
            synchronized (outgoingTransfers) {
                Integer count = outgoingTransfers.get(recipient);
                if (count != null)
                    outgoingTransfers.put(recipient, --count);
            }
        }
    }

    /**
     * 
     * @return a connection to the recipient. If no connection is established
     *         yet, a new one will be created.
     * @throws IOException
     *             if establishing a new connection failed
     * @throws InterruptedIOException
     *             if establishing a new connection was interrupted
     */
    public IByteStreamConnection getConnection(JID recipient)
        throws IOException {

        IByteStreamConnection connection = connections.get(recipient);

        if (connection != null) {
            log.trace("Reuse bytestream connection " + connection.getMode());
            return connection;
        }

        try {
            return connect(recipient);
        } catch (InterruptedException e) {
            IOException io = new InterruptedIOException(
                "connecting cancelled: " + e.getMessage());
            io.initCause(e);
            throw io;
        }
    }

    public IByteStreamConnection connect(JID recipient) throws IOException,
        InterruptedException {

        synchronized (connectLock) {
            IByteStreamConnection connection = connections.get(recipient);

            if (connection != null)
                return connection;

            ArrayList<ITransport> transportModesToUse = new ArrayList<ITransport>(
                transports);

            // Move IBB to front for peers preferring IBB
            synchronized (peersForIBB) {
                if (fallbackTransport != null
                    && peersForIBB.contains(recipient)) {
                    int ibbIndex = transportModesToUse
                        .indexOf(fallbackTransport);
                    if (ibbIndex != -1)
                        transportModesToUse.remove(ibbIndex);
                    transportModesToUse.add(0, fallbackTransport);
                }
            }

            log.debug("Currently used IP addresses for Socks5Proxy: "
                + Arrays.toString(Socks5Proxy.getSocks5Proxy()
                    .getLocalAddresses().toArray()));

            for (ITransport transport : transportModesToUse) {
                log.info("Try to establish a bytestream connection to "
                    + recipient.getBase() + " from " + currentLocalJID
                    + " using " + transport.getDefaultNetTransferMode());
                try {
                    connection = transport.connect(recipient);
                    break;
                } catch (IOException e) {
                    log.error(Utils.prefix(recipient)
                        + "Failed to connect using " + transport.toString()
                        + ":", e.getCause() == null ? e : e.getCause());
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    log.error(Utils.prefix(recipient)
                        + "Failed to connect using " + transport.toString()
                        + " because of an unknown error:", e);
                }
            }

            if (connection != null) {
                byteStreamConnectionListener.connectionChanged(recipient,
                    connection, false);

                return connection;
            }

            throw new IOException("could not connect to: "
                + Utils.prefix(recipient));
        }
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
        peersForIBB.remove(peer);
        IByteStreamConnection c = connections.remove(peer);

        if (c == null)
            return false;

        c.close();
        return true;
    }

    public NetTransferMode getTransferMode(JID jid) {
        IByteStreamConnection connection = connections.get(jid);

        return connection == null ? NetTransferMode.NONE : connection.getMode();
    }

    private void initTransports() {
        boolean forceIBBOnly = false;

        if (preferenceUtils != null)
            forceIBBOnly = preferenceUtils.forceFileTranserByChat();

        transports = new CopyOnWriteArrayList<ITransport>();

        if (!forceIBBOnly && mainTransport != null)
            transports.add(0, mainTransport);

        if (fallbackTransport != null)
            transports.add(fallbackTransport);
    }

    /**
     * Sets up the transports for the given XMPPConnection
     */
    private void prepareConnection(final Connection connection) {
        assert (this.connection == null);

        initTransports();

        log.debug("Prepare bytestreams for XMPP connection. Used transport order: "
            + Arrays.toString(transports.toArray()));

        this.connection = connection;
        this.currentLocalJID = new JID(connection.getUser());

        for (ITransport transport : transports) {
            transport.prepareXMPPConnection(connection,
                byteStreamConnectionListener);
        }
    }

    private void disposeConnection() {

        for (ITransport transport : transports)
            transport.disposeXMPPConnection();

        List<IByteStreamConnection> openConnections;
        synchronized (connections) {
            openConnections = new ArrayList<IByteStreamConnection>(
                connections.values());
        }
        for (IByteStreamConnection connection : openConnections) {
            if (connection != null) {
                // TODO switch to trace
                log.debug("Close " + connection.getMode() + " connection");
                try {
                    connection.close();
                } catch (RuntimeException e) {
                    log.error("Error while closing " + connection.getMode()
                        + " connection ", e);
                }
            }
        }

        if (connections.size() > 0)
            log.warn("Connections object shoud be empty at this points: "
                + connections.toString());

        connections.clear();
        transferModeDispatch.clear();
        peersForIBB.clear();

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

    /*
     * ------------------------------------------------------------------------
     * Support for monitoring ongoing transfers
     * ------------------------------------------------------------------------
     */

    /**
     * Returns just whether there is currently a file transfer being received
     * from the given user.
     */
    public boolean isReceiving(JID from) {
        return getIncomingTransfers(from).size() > 0;
    }

    /**
     * Returns a live copy of the file-transfers currently being received
     */
    public List<TransferDescription> getIncomingTransfers(JID from) {
        synchronized (incomingTransfers) {

            List<TransferDescription> transfers = incomingTransfers.get(from);
            if (transfers == null) {
                transfers = new ArrayList<TransferDescription>();
                incomingTransfers.put(from, transfers);
            }

            return transfers;
        }
    }

    private void removeIncomingFileTransfer(
        TransferDescription transferDescription) {

        synchronized (incomingTransfers) {

            JID from = transferDescription.getSender();

            List<TransferDescription> transfers = getIncomingTransfers(from);
            if (!transfers.remove(transferDescription)) {
                log.warn("Removing incoming transfer description that was never added!:"
                    + transferDescription);
            }
        }
    }

    private void addIncomingFileTransfer(TransferDescription transferDescription) {

        synchronized (incomingTransfers) {
            JID from = transferDescription.getSender();
            List<TransferDescription> transfers = getIncomingTransfers(from);
            transfers.add(transferDescription);
        }
    }

    /**
     * Returns whether there is currently a file being send to the given
     * recipient.
     */
    public boolean isSending(JID recipient) {
        Integer transferCount;
        synchronized (outgoingTransfers) {
            transferCount = outgoingTransfers.get(recipient);
        }
        return (transferCount != null && transferCount > 0);
    }

    /**
     * Flag the specified peer to prefer IBB during a connect during this Saros
     * session.
     * 
     * @param peer
     *            {@link JID} of the peer to set IBB as preferred transfer mode
     */
    public void setFallbackConnectionMode(JID peer) {
        synchronized (peersForIBB) {
            if (!peersForIBB.contains(peer))
                peersForIBB.add(peer);
        }
    }

    /**
     * Disconnects all idle IBB connections to peers. Use to enforce attempting
     * a Socks5 connection on next data transfer.
     * 
     * @return false if not all IBBs could be closed because of ongoing
     *         transfers
     */
    public synchronized boolean disconnectInBandBytestreams() {
        log.info("Closing all IBBs on request");
        List<IByteStreamConnection> openConnections;

        openConnections = new ArrayList<IByteStreamConnection>(
            connections.values());

        boolean isTransfering = false;
        for (IByteStreamConnection connection : openConnections) {
            if (connection != null
                && connection.getMode() == NetTransferMode.IBB) {

                // If this connection is currently in use, don't disconnect
                if (isReceiving(connection.getPeer())
                    || isSending(connection.getPeer())) {
                    isTransfering = true;
                    continue;
                }

                try {
                    connection.close();
                    log.info("Closing IBB connection to "
                        + connection.getPeer().getBareJID());
                } catch (RuntimeException e) {
                    log.error("Error while closing " + connection.getMode()
                        + " connection ", e);
                }
            }
        }
        return isTransfering == false;
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
}
