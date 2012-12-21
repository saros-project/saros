package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TransferData;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IPacketInterceptor;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.util.StopWatch;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This class is responsible for handling all transfers of binary data. It
 * maintains a map of established connections and tries to reuse them.
 * 
 * @author coezbek
 * @author jurke
 */
@Component(module = "net")
public class DataTransferManager implements IConnectionListener,
    IByteStreamConnectionListener {

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

    /**
     * Maps JIDs to the last known throughput of an incoming IBB reception
     */
    private Map<JID, Double> incomingIBBTransferSpeeds = new HashMap<JID, Double>();

    private TransferModeDispatch transferModeDispatch = new TransferModeDispatch();

    private CopyOnWriteArrayList<IPacketInterceptor> packetInterceptors = new CopyOnWriteArrayList<IPacketInterceptor>();

    private JID currentLocalJID;

    private ConcurrentLinkedQueue<TransferData> fileTransferQueue;

    private Connection connection;

    private XMPPReceiver receiver;

    private IBBTransport ibbTransport;

    private IUPnPService upnpService;

    private Socks5Transport socks5Transport;

    private PreferenceUtils preferenceUtils;

    private ArrayList<ITransport> transports = null;

    private Map<NetTransferMode, Throwable> errors = new LinkedHashMap<NetTransferMode, Throwable>();

    private SarosNet sarosNet;

    /**
     * Collection of {@link JID}s, flagged to prefer IBB transfer mode
     */
    protected Collection<JID> peersForIBB = new ArrayList<JID>();

    public DataTransferManager(SarosNet sarosNet, XMPPReceiver receiver,
        IBBTransport ibbTransport, Socks5Transport socks5Transport,
        @Nullable IUPnPService upnpService,
        @Nullable RosterTracker rosterTracker,
        @Nullable PreferenceUtils preferenceUtils) {

        this.sarosNet = sarosNet;
        this.receiver = receiver;
        this.ibbTransport = ibbTransport;
        this.socks5Transport = socks5Transport;
        this.upnpService = upnpService;
        this.preferenceUtils = preferenceUtils;
        this.initTransports();

        if (rosterTracker != null)
            addRosterListener(rosterTracker);

        sarosNet.addListener(this);

        transferModeDispatch.add(new TransferCompleteListener());
    }

    /**
     * Adds a RosterListener to the tracker to remove connections when peer gets
     * unavailable.
     * 
     * Else IBB connections would remain if no leave package is send.
     * 
     * @param rosterTracker
     */
    protected void addRosterListener(RosterTracker rosterTracker) {
        rosterTracker.addRosterListener(new IRosterListener() {

            public void entriesAdded(Collection<String> addresses) {
                // nothing to do here
            }

            public void entriesDeleted(Collection<String> addresses) {
                // nothing to do here
            }

            public void entriesUpdated(Collection<String> addresses) {
                // nothing to do here
            }

            public void presenceChanged(Presence presence) {

                if (!presence.isAvailable())
                    if (closeConnection(new JID(presence.getFrom())))
                        log.debug(presence.getFrom()
                            + " is not available anymore. Bytestream connection closed.");
            }

            public void rosterChanged(Roster roster) {
                // nothing to do here
            }

        });
    }

    private final class LoggingTransferObject implements IncomingTransferObject {

        private final IncomingTransferObject transferObject;
        private final TransferDescription description;

        private LoggingTransferObject(IncomingTransferObject transferObject) {
            this.transferObject = transferObject;
            this.description = transferObject.getTransferDescription();
        }

        /**
         * Accepts a transfer and returns the incoming data.
         */
        public byte[] accept() throws IOException {
            addIncomingFileTransfer(description);
            try {
                // TODO Put size in TransferDescription, so we can
                // display it here

                log.trace("[" + getTransferMode()
                    + "] Starting incoming data transfer: " + description);

                long startTime = System.nanoTime();

                byte[] content = transferObject.accept();

                long duration = Math.max(0, System.nanoTime() - startTime) / 1000000;

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

        public TransferDescription getTransferDescription() {
            return description;
        }

        public NetTransferMode getTransferMode() {
            return transferObject.getTransferMode();
        }

        public long getTransferredSize() {
            return transferObject.getTransferredSize();
        }

        public long getUncompressedSize() {
            return transferObject.getUncompressedSize();
        }

    }

    protected Map<JID, IByteStreamConnection> connections = Collections
        .synchronizedMap(new HashMap<JID, IByteStreamConnection>());

    /**
     * Adds an incoming transfer.
     * 
     * @param transferObjectDescription
     *            An IncomingTransferObject that has the TransferDescription as
     *            content to provide information of the incoming transfer to
     *            upper layers.
     */
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

        receiver.processIncomingTransferObject(description, transferObjectData);
    }

    /**
     * Dispatch to the used {@link BinaryChannelConnection}.
     * 
     * @throws IOException
     *             If a technical problem occurred.
     */
    public void sendData(TransferDescription transferData, byte[] payload)
        throws IOException {

        // Think about how to synchronize this, that multiple people can connect
        // at the same time.
        log.trace("sending data ... from " + sarosNet.getMyJID() + " to "
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

            StopWatch watch = new StopWatch().start();

            long sizeUncompressed = payload.length;

            if (transferData.compressContent()) {
                payload = Utils.deflate(payload, null);
            }

            connection.send(transferData, payload);

            watch.stop();
            transferModeDispatch.transferFinished(recipient,
                connection.getMode(), false, payload.length, sizeUncompressed,
                watch.getTime());
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

        IByteStreamConnection connection = null;

        ArrayList<ITransport> transportModesToUse = transports;

        // Move IBB to front for peers preferring IBB
        if (peersForIBB.contains(recipient)) {
            int ibbIndex = transports.indexOf(ibbTransport);
            if (ibbIndex != -1)
                transports.remove(ibbIndex);
            transports.add(0, ibbTransport);
        }

        log.debug("Currently used IP addresses for Socks5Proxy: "
            + Arrays.toString(Socks5Proxy.getSocks5Proxy().getLocalAddresses()
                .toArray()));
        for (ITransport transport : transportModesToUse) {
            log.info("Try to establish a bytestream connection to "
                + recipient.getBase() + " from " + sarosNet.getMyJID()
                + " using " + transport.getDefaultNetTransferMode());
            try {
                connection = transport.connect(recipient);
            } catch (IOException e) {
                log.error(Utils.prefix(recipient) + "Failed to connect using "
                    + transport.toString() + ":",
                    e.getCause() == null ? e : e.getCause());
                errors.put(transport.getDefaultNetTransferMode(), e.getCause());
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                log.error(Utils.prefix(recipient) + "Failed to connect using "
                    + transport.toString() + " because of an unknown error:", e);
                errors.put(transport.getDefaultNetTransferMode(), e);
            }
            if (connection != null)
                break;
        }

        if (connection == null) {

            StringBuilder errorMsg = new StringBuilder(
                "Exhausted all transport options: ");

            for (Entry<NetTransferMode, Throwable> entry : errors.entrySet()) {
                errorMsg.append(entry.getKey() + ": "
                    + entry.getValue().getMessage() + ", ");
            }

            errorMsg.delete(errorMsg.length() - 2, errorMsg.length());

            throw new IOException(Utils.prefix(recipient) + errorMsg.toString());
        } else
            connectionChanged(recipient, connection, false);
        return connection;
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
        IByteStreamConnection c = connections.remove(peer);
        if (c == null)
            return false;

        c.close();
        return true;
    }

    public synchronized void connectionChanged(JID peer,
        IByteStreamConnection connection2, boolean incomingRequest) {
        // TODO: remove these lines
        IByteStreamConnection old = connections.get(peer);
        assert (old == null || !old.isConnected());

        log.debug("Bytestream connection changed " + connection2.getMode());
        connections.put(peer, connection2);
        transferModeDispatch.connectionChanged(peer, connection2);

        if (connection2.getMode() == NetTransferMode.IBB && incomingRequest
            && upnpService != null)
            upnpService.checkAndInformAboutUPnP();
    }

    public void connectionClosed(JID peer, IByteStreamConnection connection2) {
        connections.remove(peer);
        transferModeDispatch.connectionChanged(peer, null);
    }

    public NetTransferMode getTransferMode(JID jid) {
        IByteStreamConnection connection = connections.get(jid);
        if (connection == null)
            return NetTransferMode.NONE;
        return connection.getMode();
    }

    // Listens for completed transfers to store speed of incoming IBB transfers
    protected class TransferCompleteListener implements ITransferModeListener {

        // store transfer speed for incoming IBB transfers, user specific
        public void transferFinished(JID jid, NetTransferMode newMode,
            boolean incoming, long sizeTransferred, long sizeUncompressed,
            long transmissionMillisecs) {
            if (newMode == NetTransferMode.IBB && incoming) {

                double ibbSpeed = sizeTransferred
                    / ((double) transmissionMillisecs / 1000);

                setIncomingIBBTransferSpeed(jid, ibbSpeed);
            }
        }

        public void connectionChanged(JID jid, IByteStreamConnection connection) {
            // do nothing
        }

        public void clear() {
            // do nothing
        }
    }

    public static class TransferModeDispatch implements ITransferModeListener {

        protected List<ITransferModeListener> listeners = new ArrayList<ITransferModeListener>();

        public synchronized void add(ITransferModeListener listener) {
            listeners.add(listener);
        }

        public synchronized void remove(ITransferModeListener listener) {
            listeners.remove(listener);
        }

        public synchronized void clear() {
            for (ITransferModeListener listener : listeners) {
                try {
                    listener.clear();
                } catch (RuntimeException e) {
                    log.error("Listener crashed: ", e);
                }
            }
        }

        public synchronized void transferFinished(JID jid,
            NetTransferMode newMode, boolean incoming, long sizeTransferred,
            long sizeUncompressed, long transmissionMillisecs) {

            for (ITransferModeListener listener : listeners) {
                try {
                    listener.transferFinished(jid, newMode, incoming,
                        sizeTransferred, sizeUncompressed,
                        transmissionMillisecs);
                } catch (RuntimeException e) {
                    log.error("Listener crashed: ", e);
                }
            }
        }

        public synchronized void connectionChanged(JID jid,
            IByteStreamConnection connection) {
            for (ITransferModeListener listener : listeners) {
                try {
                    listener.connectionChanged(jid, connection);
                } catch (RuntimeException e) {
                    log.error("Listener crashed: ", e);
                }
            }
        }
    }

    /*
     * On Henning's suggestion, Saros is not the place to implement free
     * transport negotiation because this is actually part of XMP-protocol: the
     * smack API would be the place to implement it and there we should put our
     * effort.
     */
    protected void initTransports() {
        initTransports(isForceFileTransferByChatEnabled());
    }

    protected boolean isForceFileTransferByChatEnabled() {
        if (preferenceUtils != null)
            return preferenceUtils.forceFileTranserByChat();
        else
            return false;
    }

    /**
     * Initializes transport methods respective the set property
     * (PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT only). The last method is
     * the in-band bytestream. see also addPrimaryTransports()
     */
    protected void initTransports(boolean chatOnly) {
        this.transports = new ArrayList<ITransport>();
        if (!chatOnly) {
            addPrimaryTransports();
        }
        if (ibbTransport != null)
            transports.add(ibbTransport);
    }

    /**
     * Method to add all primary transport methods (except chat). The transports
     * are tried in order they are inserted here.
     */
    protected void addPrimaryTransports() {
        if (socks5Transport != null)
            transports.add(0, socks5Transport);
    }

    public boolean connectionIsDisposed() {
        return connection == null;
    }

    /**
     * Sets up the transports for the given XMPPConnection
     */
    protected void prepareConnection(final Connection connection) {
        assert (this.connectionIsDisposed());

        this.initTransports();

        log.debug("Prepare bytestreams for XMPP connection. Used transport order: "
            + Arrays.toString(transports.toArray()));

        this.connection = connection;
        this.fileTransferQueue = new ConcurrentLinkedQueue<TransferData>();
        this.currentLocalJID = new JID(connection.getUser());

        for (ITransport transport : transports) {
            transport.prepareXMPPConnection(connection, this);
        }
    }

    protected void disposeConnection() {

        for (ITransport transport : transports) {
            transport.disposeXMPPConnection();
        }

        fileTransferQueue.clear();

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

        connection = null;
    }

    public void connectionStateChanged(Connection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED)
            prepareConnection(connection);
        else if (!connectionIsDisposed())
            disposeConnection();
    }

    /**
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

    protected void removeIncomingFileTransfer(
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

    protected void addIncomingFileTransfer(
        TransferDescription transferDescription) {

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
     * Returns the last known speed of incoming IBB transfer for the given
     * {@link JID}.
     * 
     * @param jid
     *            The {@link JID} of the peer to get the throughput information
     *            about
     * @return speed in bytes/second, or 0 if not known
     */
    public double getIncomingIBBTransferSpeed(JID jid) {
        Double value = incomingIBBTransferSpeeds.get(jid);

        if (value == null)
            return 0;
        else
            return value.doubleValue();
    }

    /**
     * Sets a speed value for a given {@link JID}
     * 
     * @param jid
     *            {@link JID} of the transfer source peer
     * @param value
     *            transfer speed in bytes/second
     */
    protected void setIncomingIBBTransferSpeed(JID jid, double value) {
        incomingIBBTransferSpeeds.put(jid, value);
    }

    /**
     * Flag the specified peer to prefer IBB during a connect during this Saros
     * session.
     * 
     * @param peer
     *            {@link JID} of the peer to set IBB as preferred transfer mode
     */
    public void setFallbackConnectionMode(JID peer) {
        if (!peersForIBB.contains(peer))
            peersForIBB.add(peer);
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

    public void addPacketInterceptor(IPacketInterceptor interceptor) {
        packetInterceptors.addIfAbsent(interceptor);
    }

    public void removePacketInterceptor(IPacketInterceptor interceptor) {
        packetInterceptors.remove(interceptor);
    }

}
