package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.dnd.TransferData;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.util.StoppWatch;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class is responsible for handling all transfers of binary data. It
 * maintains a map of established connections and tries to reuse them.
 * 
 * @author coezbek
 * @author jurke
 */
@Component(module = "net")
public class DataTransferManager implements ConnectionSessionListener,
    IBytestreamConnectionListener {

    protected Map<JID, List<TransferDescription>> incomingTransfers = new HashMap<JID, List<TransferDescription>>();

    protected TransferModeDispatch transferModeDispatch = new TransferModeDispatch();

    protected ConcurrentLinkedQueue<TransferData> fileTransferQueue;

    protected XMPPConnection connection;

    @Inject
    protected XMPPReceiver receiver;

    @Inject
    protected DispatchThreadContext dispatchThreadContext;

    @Inject
    protected IncomingTransferObjectExtensionProvider incomingExtProv;

    protected Saros saros;

    protected PreferenceUtils preferenceUtils;

    protected ArrayList<ITransport> transports = null;
    protected SessionIDObservable sessionID = null;

    public DataTransferManager(Saros saros, SessionIDObservable sessionID,
        PreferenceUtils preferenceUtils) {
        this.sessionID = sessionID;
        this.saros = saros;
        this.preferenceUtils = preferenceUtils;
        this.initTransports();
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
        public byte[] accept(final SubMonitor progress)
            throws SarosCancellationException, IOException {
            addIncomingFileTransfer(description);
            try {
                // TODO Put size in TransferDescription, so we can
                // display it here
                if (description.type == FileTransferType.ACTIVITY_TRANSFER
                    || description.type == FileTransferType.STREAM_DATA
                    || description.type == FileTransferType.STREAM_META) {
                    if (log.isTraceEnabled()) {
                        log.trace("[" + getTransferMode().toString()
                            + "] Starting incoming data transfer: "
                            + description.toString());
                    }
                } else {
                    log.debug("[" + getTransferMode().toString()
                        + "] Starting incoming data transfer: "
                        + description.toString());
                }

                long startTime = System.nanoTime();

                byte[] content = transferObject.accept(progress);

                long duration = Math.max(0, System.nanoTime() - startTime) / 1000000;

                if (description.type == FileTransferType.ACTIVITY_TRANSFER
                    || description.type == FileTransferType.STREAM_DATA
                    || description.type == FileTransferType.STREAM_META) {
                    if (log.isTraceEnabled()) {
                        log.trace("[" + getTransferMode().toString()
                            + "] Finished incoming data transfer: "
                            + description.toString() + ", size: "
                            + Util.throughput(content.length, duration));
                    }
                } else {
                    log.debug("[" + getTransferMode().toString()
                        + "] Finished incoming data transfer: "
                        + description.toString() + ", size: "
                        + Util.throughput(content.length, duration));
                }

                transferModeDispatch.transferFinished(description.getSender(),
                    getTransferMode(), true, content.length, duration);

                return content;

            } finally {
                removeIncomingFileTransfer(description);
            }
        }

        public TransferDescription getTransferDescription() {
            return description;
        }

        /**
         * Rejects the incoming transfer data.
         */
        public void reject() throws IOException {
            transferObject.reject();
        }

        public NetTransferMode getTransferMode() {
            return transferObject.getTransferMode();
        }
    }

    private static final Logger log = Logger
        .getLogger(DataTransferManager.class);

    protected Map<JID, IBytestreamConnection> connections = Collections
        .synchronizedMap(new HashMap<JID, IBytestreamConnection>());

    /**
     * This interface is used to define various transport methods (probably only
     * XEP 65 SOCKS5, XEP 47 in-band bytestream and XEP 16x Jingle
     */
    public interface ITransport {

        /**
         * Try to connect to the given user.
         * 
         * @throws IOException
         */
        public IBytestreamConnection connect(JID peer, SubMonitor progress)
            throws IOException, InterruptedException;

        public void prepareXMPPConnection(XMPPConnection connection,
            IBytestreamConnectionListener listener);

        public void disposeXMPPConnection();

        public String toString();

        public NetTransferMode getDefaultNetTransferMode();
    }

    /**
     * A IConnection is responsible for sending data to a particular user
     */
    public interface IBytestreamConnection {

        public JID getPeer();

        public void close();

        public boolean isConnected();

        /**
         * If this call returns the data has been send successfully, otherwise
         * an IOException is thrown with the reason why the transfer failed.
         * 
         * @param data
         *            The data to be sent.
         * @throws IOException
         *             if the send failed
         * @throws SarosCancellationException
         *             It will be thrown if the user (locally or remotely) has
         *             canceled the transfer.
         * @blocking Send the given data as a blocking operation.
         */
        public void send(TransferDescription data, byte[] content,
            SubMonitor callback) throws IOException, SarosCancellationException;

        public NetTransferMode getMode();
    }

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

        // ask upper layer to accept
        dispatchThreadContext.executeAsDispatch(new Runnable() {
            public void run() {
                receiver.processIncomingTransferObject(description,
                    transferObjectData);
            }
        });
    }

    /**
     * Dispatch to Transmitter.
     * 
     * @throws SarosCancellationException
     *             It will be thrown if the local or remote user has canceled
     *             the transfer.
     * @throws IOException
     *             If a technical problem occurred.
     */
    public void sendData(TransferDescription transferData, byte[] input,
        SubMonitor progress) throws IOException, SarosCancellationException {

        // Think about how to synchronize this, that multiple people can connect
        // at the same time.
        log.debug("sending data ... ");

        JID recipient = transferData.recipient;

        IBytestreamConnection connection = getConnection(recipient, progress
            .newChild(1));

        try {
            StoppWatch watch = new StoppWatch().start();

            if (transferData.compressInDataTransferManager()) {
                input = Util.deflate(input, progress.newChild(15));
            }

            connection.send(transferData, input, progress);

            watch.stop();

            transferModeDispatch.transferFinished(recipient, connection
                .getMode(), false, input.length, watch.getTime());
        } catch (SarosCancellationException e) {
            throw e; // Rethrow to circumvrent the Exception catch below
        } catch (IOException e) {
            log.error(Util.prefix(transferData.recipient) + "Failed to send "
                + transferData + " with " + connection.getMode().toString()
                + ":", e.getCause());
            throw e;
        }
    }

    public IBytestreamConnection getConnection(JID recipient,
        SubMonitor progress) throws IOException, SarosCancellationException {

        IBytestreamConnection connection = connections.get(recipient);

        if (connection != null) {
            log.debug("Reuse bytestream connection " + connection.getMode());
            return connection;
        }

        try {

            return connect(recipient, progress);
        } catch (InterruptedException e) {
            throw new SarosCancellationException("Connecting cancelled");
        }
    }

    public IBytestreamConnection connect(JID recipient, SubMonitor progress)
        throws IOException, InterruptedException {

        IBytestreamConnection connection = null;
        for (ITransport transport : transports) {
            log.info("Try to establish a bytestream connection to "
                + recipient.getBase() + " using "
                + transport.getDefaultNetTransferMode());
            try {
                connection = transport.connect(recipient, progress);
            } catch (IOException e) {
                log.error(Util.prefix(recipient) + "Failed to connect using "
                    + transport.toString() + ":", e.getCause() == null ? e : e
                    .getCause());
            }
            if (connection != null)
                break;
        }

        if (connection == null)
            throw new IOException(Util.prefix(recipient)
                + "Exhausted all transport options: "
                + Arrays.toString(transports.toArray()));
        else
            connectionChanged(recipient, connection);
        return connection;
    }

    public TransferModeDispatch getTransferModeDispatch() {
        return transferModeDispatch;
    }

    public synchronized void connectionChanged(JID peer,
        IBytestreamConnection connection2) {
        // TODO: remove these lines
        IBytestreamConnection old = connections.get(peer);
        assert (old == null || !old.isConnected());

        log.debug("Bytestream connection changed " + connection2.getMode());
        connections.put(peer, connection2);
        transferModeDispatch.connectionChanged(peer, connection2);
    }

    public void connectionClosed(JID peer, IBytestreamConnection connection2) {
        connections.remove(peer);
        transferModeDispatch.connectionChanged(peer, null);
    }

    public NetTransferMode getTransferMode(JID jid) {
        IBytestreamConnection connection = connections.get(jid);
        if (connection == null)
            return null;
        return connection.getMode();
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
            NetTransferMode newMode, boolean incoming, long size,
            long transmissionMillisecs) {

            for (ITransferModeListener listener : listeners) {
                try {
                    listener.transferFinished(jid, newMode, incoming, size,
                        transmissionMillisecs);
                } catch (RuntimeException e) {
                    log.error("Listener crashed: ", e);
                }
            }
        }

        public synchronized void connectionChanged(JID jid,
            IBytestreamConnection connection) {
            for (ITransferModeListener listener : listeners) {
                try {
                    listener.connectionChanged(jid, connection);
                } catch (RuntimeException e) {
                    log.error("Listener crashed: ", e);
                }
            }
        }
    }

    public void awaitJingleManager(JID jid) {

        // if (discoveryManager.isJingleSupported(jid)) {
        // getJingleManager();
        // }
    }

    /**
     * Sets up the transports for the given XMPPConnection
     */
    public void prepareConnection(final XMPPConnection connection) {
        assert (this.connectionIsDisposed());

        this.updateFileTransferByChatOnly();

        log
            .debug("Prepare bytestreams for XMPP connection. Used transport order: "
                + Arrays.toString(transports.toArray()));

        this.connection = connection;
        this.fileTransferQueue = new ConcurrentLinkedQueue<TransferData>();

        for (ITransport transport : transports) {
            transport.prepareXMPPConnection(connection, this);
        }
    }

    /*
     * On Henning's suggestion, Saros is not the place to implement free
     * transport negotiation because this is actually part of XMP-protocol: the
     * smack API would be the place to implement it and there we should put our
     * effort.
     */
    protected void updateFileTransferByChatOnly() {
        if (preferenceUtils.forceFileTranserByChat()) {
            if (transports.size() == 1)
                return;
            else {
                initTransports();
            }
        } else if (transports.size() == 1) {
            addPrimaryTransports();

        }
    }

    /**
     * Initializes transport methods respective the set property
     * (PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT only). The last method is
     * the in-band bytestream. see also addPrimaryTransports()
     */
    protected void initTransports() {
        this.transports = new ArrayList<ITransport>();
        if (!preferenceUtils.forceFileTranserByChat()) {
            addPrimaryTransports();
        }
        transports.add(IBBTransport.getTransport());
    }

    /**
     * Method to add all primary transport methods (except chat). The transports
     * are tried in order they are inserted here.
     */
    protected void addPrimaryTransports() {
        transports.add(0, Socks5Transport.getTransport());
    }

    public boolean connectionIsDisposed() {
        return connection == null;
    }

    public enum NetTransferMode {
        UNKNOWN("???", "???", false), //
        IBB("IBB", "XEP 47 In-Band Bytestream", false), //
        JINGLETCP("Jingle/TCP", "XEP 166 Jingle (TCP)", true), //
        JINGLEUDP("Jingle/UDP", "XEP 166 Jingle (UDP)", true), // 
        HANDMADE("Chat", "Chat", false), //
        SOCKS5("SOCKS5", "XEP 65 SOCKS5", true), //
        SOCKS5_MEDIATED("SOCKS5 (mediated)", "XEP 65 SOCKS5", true), //
        SOCKS5_DIRECT("SOCKS5 (direct)", "XEP 65 SOCKS5", true);//

        private String name;
        private String xep;
        private boolean p2p;

        NetTransferMode(String name, String xep, boolean p2p) {
            this.name = name;
            this.xep = xep;
            this.p2p = p2p;
        }

        public String getXEP() {
            return xep;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isP2P() {
            return p2p;
        }
    }

    public enum JingleConnectionState {
        /**
         * A jingle connection starts in this state. It is not ready for sending
         * yet. Trying to send will block.
         */
        INIT,
        /**
         * The jingle connection is ready for sending.
         */
        ESTABLISHED,
        /**
         * The jingle connection has been closed correctly. Trying to send will
         * reopen the connection.
         */
        CLOSED,
        /**
         * The jingle connection has been closed on error. Trying to send will
         * always return immediately.
         */
        ERROR
    }

    public void disposeConnection() {

        for (ITransport transport : transports) {
            transport.disposeXMPPConnection();
        }

        fileTransferQueue.clear();

        List<IBytestreamConnection> openConnections;
        synchronized (connections) {
            openConnections = new ArrayList<IBytestreamConnection>(connections
                .values());
        }
        for (IBytestreamConnection connection : openConnections) {
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

    public void startConnection() {
        // TODO The data transfer manager does not support caching yet
    }

    public void stopConnection() {
        // TODO The data transfer manager does not support caching yet
        // log.warn("Error state stops connection without effect");
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

            JID from = transferDescription.sender;

            List<TransferDescription> transfers = getIncomingTransfers(from);
            if (!transfers.remove(transferDescription)) {
                log
                    .warn("Removing incoming transfer description that was never added!:"
                        + transferDescription);
            }
        }
    }

    protected void addIncomingFileTransfer(
        TransferDescription transferDescription) {

        synchronized (incomingTransfers) {
            JID from = transferDescription.sender;
            List<TransferDescription> transfers = getIncomingTransfers(from);
            transfers.add(transferDescription);
        }
    }

}
