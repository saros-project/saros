package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.dnd.TransferData;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.util.StopWatch;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.log.LoggingUtils;

/**
 * This class is responsible for handling all transfers of binary data. It
 * maintains a map of established connections and tries to reuse them.
 * 
 * @author coezbek
 * @author jurke
 */
@Component(module = "net")
public class DataTransferManager implements IConnectionListener,
    IBytestreamConnectionListener {

    protected Map<JID, List<TransferDescription>> incomingTransfers = new HashMap<JID, List<TransferDescription>>();

    protected Map<JID, Double> incomingIBBTransferSpeeds = new HashMap<JID, Double>();

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

    protected Map<NetTransferMode, Throwable> errors = new LinkedHashMap<NetTransferMode, Throwable>();

    protected SessionIDObservable sessionID = null;

    public DataTransferManager(Saros saros, SessionIDObservable sessionID,
        PreferenceUtils preferenceUtils, RosterTracker rosterTracker) {
        this.sessionID = sessionID;
        this.saros = saros;
        this.preferenceUtils = preferenceUtils;
        this.initTransports();
        addRosterListener(rosterTracker);
        saros.addListener(this);
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
                    for (JID jid : connections.keySet()) {
                        if (jid.toString().equals(presence.getFrom())) {
                            IBytestreamConnection c = connections.remove(jid);
                            log.debug(jid.getBase()
                                + " is not available anymore. Bytestream connection closed.");
                            c.close();
                        }
                    }
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
        public byte[] accept(final SubMonitor progress)
            throws SarosCancellationException, IOException {
            addIncomingFileTransfer(description);
            try {
                // TODO Put size in TransferDescription, so we can
                // display it here

                LoggingUtils.log(
                    log,
                    "[" + getTransferMode().toString()
                        + "] Starting incoming data transfer: "
                        + description.toString(), description.logToDebug);

                long startTime = System.nanoTime();

                byte[] content = transferObject.accept(progress);

                long duration = Math.max(0, System.nanoTime() - startTime) / 1000000;

                LoggingUtils.log(
                    log,
                    "[" + getTransferMode().toString()
                        + "] Finished incoming data transfer: "
                        + description.toString() + ", Throughput: "
                        + Utils.throughput(getTransferredSize(), duration),
                    description.logToDebug);

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

        /**
         * Rejects the incoming transfer data.
         */
        public void reject() throws IOException {
            transferObject.reject();
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
     *             It will be thrown if the local or buddy has canceled the
     *             transfer.
     * @throws IOException
     *             If a technical problem occurred.
     */
    public void sendData(TransferDescription transferData, byte[] input,
        SubMonitor progress) throws IOException, SarosCancellationException {

        // Think about how to synchronize this, that multiple people can connect
        // at the same time.
        LoggingUtils.log(log, "sending data ... ", transferData.logToDebug);

        JID recipient = transferData.recipient;

        IBytestreamConnection connection = getConnection(recipient,
            progress.newChild(1));

        try {
            StopWatch watch = new StopWatch().start();

            long sizeUncompressed = input.length;

            if (transferData.compressInDataTransferManager()) {
                input = Utils.deflate(input, progress.newChild(15));
            }

            connection.send(transferData, input, progress);

            watch.stop();

            transferModeDispatch.transferFinished(recipient,
                connection.getMode(), false, input.length, sizeUncompressed,
                watch.getTime());

        } catch (SarosCancellationException e) {
            throw e; // Rethrow to circumvent the Exception catch below
        } catch (IOException e) {
            e.printStackTrace();
            log.error(Utils.prefix(transferData.recipient) + "Failed to send "
                + transferData + " with " + connection.getMode().toString()
                + ":" + e.getMessage() + ":", e.getCause());
            throw e;
        }
    }

    /**
     * 
     * @return a connection to the recipient. If no connection is established
     *         yet, a new one will be created.
     * @throws IOException
     *             if establishing a new connection failed
     * @throws SarosCancellationException
     *             if establishing a new connection was interrupted
     */
    public IBytestreamConnection getConnection(JID recipient,
        SubMonitor progress) throws IOException, SarosCancellationException {

        IBytestreamConnection connection = connections.get(recipient);

        if (connection != null) {
            log.trace("Reuse bytestream connection " + connection.getMode());
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
            return NetTransferMode.NONE;
        return connection.getMode();
    }

    // Listens for completed transfers to store speed of incoming IBB transfers
    protected class TransferCompleteListener implements ITransferModeListener {
        protected DataTransferManager dataTransferManager;

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

        public void connectionChanged(JID jid, IBytestreamConnection connection) {
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

    /*
     * On Henning's suggestion, Saros is not the place to implement free
     * transport negotiation because this is actually part of XMP-protocol: the
     * smack API would be the place to implement it and there we should put our
     * effort.
     */
    protected void initTransports() {
        initTransports(preferenceUtils.forceFileTranserByChat());
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
        NONE("", "", false), UNKNOWN("???", "???", false), //
        IBB("IBB", "XEP 47 In-Band Bytestream", false), //
        JINGLETCP("Jingle/TCP", "XEP 166 Jingle (TCP)", true), //
        JINGLEUDP("Jingle/UDP", "XEP 166 Jingle (UDP)", true), //
        HANDMADE("Chat", "Chat", false), //
        SOCKS5("SOCKS5", "XEP 65 SOCKS5", true), //
        SOCKS5_MEDIATED("SOCKS5 (mediated)", "XEP 65 SOCKS5", false), //
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

    /**
     * Sets up the transports for the given XMPPConnection
     */
    protected void prepareConnection(final XMPPConnection connection) {
        assert (this.connectionIsDisposed());

        this.initTransports();

        log.debug("Prepare bytestreams for XMPP connection. Used transport order: "
            + Arrays.toString(transports.toArray()));

        this.connection = connection;
        this.fileTransferQueue = new ConcurrentLinkedQueue<TransferData>();

        for (ITransport transport : transports) {
            transport.prepareXMPPConnection(connection, this);
        }
    }

    protected void disposeConnection() {

        for (ITransport transport : transports) {
            transport.disposeXMPPConnection();
        }

        fileTransferQueue.clear();

        List<IBytestreamConnection> openConnections;
        synchronized (connections) {
            openConnections = new ArrayList<IBytestreamConnection>(
                connections.values());
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

    public void connectionStateChanged(XMPPConnection connection,
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

            JID from = transferDescription.sender;

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
            JID from = transferDescription.sender;
            List<TransferDescription> transfers = getIncomingTransfers(from);
            transfers.add(transferDescription);
        }
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

}
