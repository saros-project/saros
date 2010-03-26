package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.dnd.TransferData;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.packet.Jingle;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.business.ActivitiesHandler;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.JingleFileTransferManagerObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class is responsible for handling all transfers of binary data
 */
@Component(module = "net")
public class DataTransferManager implements ConnectionSessionListener {

    protected Map<JID, List<TransferDescription>> incomingTransfers = new HashMap<JID, List<TransferDescription>>();

    protected Map<JID, NetTransferMode> incomingTransferModes = Collections
        .synchronizedMap(new HashMap<JID, NetTransferMode>());
    protected Map<JID, NetTransferMode> outgoingTransferModes = Collections
        .synchronizedMap(new HashMap<JID, NetTransferMode>());

    protected TransferModeDispatch transferModeDispatch = new TransferModeDispatch();

    /**
     * TransferModeListener which keeps track of the last type of transfer mode
     * *started* in both incoming and outgoing directions.
     */
    protected ITransferModeListener trackingTransferModeListener = new ITransferModeListener() {

        public void clear() {
            incomingTransferModes.clear();
            outgoingTransferModes.clear();
        }

        public void transferFinished(JID jid, NetTransferMode newMode,
            boolean incoming, long size, long transmissionMillisecs) {
            if (incoming) {
                incomingTransferModes.put(jid, newMode);
            } else {
                outgoingTransferModes.put(jid, newMode);
            }
        }
    };

    protected boolean forceFileTransferByChat;

    protected IPropertyChangeListener propertyListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(
                PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT)) {
                Object value = event.getNewValue();
                // make sure the cast will work
                if (value instanceof Boolean) {
                    forceFileTransferByChat = ((Boolean) value).booleanValue();

                    // add/remove Jingle XMPP feature
                    ServiceDiscoveryManager sdm = ServiceDiscoveryManager
                        .getInstanceFor(connection);
                    if (forceFileTransferByChat) {
                        sdm.removeFeature(Jingle.NAMESPACE);
                    } else {
                        if (!sdm.includesFeature(Jingle.NAMESPACE))
                            sdm.addFeature(Jingle.NAMESPACE);
                    }

                } else {
                    log.warn("Preference value FORCE_FILETRANSFER_BY_CHAT"
                        + " is supposed to be a boolean, but it unexpectedly"
                        + " changed to a different type!");
                }
            }
        }
    };

    protected IJingleFileTransferListener jingleListener = new JingleTransferListener();

    protected FileTransferManager fileTransferManager;

    protected ConcurrentLinkedQueue<TransferData> fileTransferQueue;

    protected Thread startingJingleThread;

    protected XMPPConnection connection;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    @Inject
    protected JingleFileTransferManagerObservable jingleManager;

    @Inject
    protected XMPPReceiver receiver;

    @Inject
    protected ActivitiesHandler activitiesHandler;

    @Inject
    protected InvitationProcessObservable invitationProcess;

    @Inject
    protected ActivitiesExtensionProvider activitiesProvider;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected DispatchThreadContext dispatchThreadContext;

    @Inject
    protected IncomingTransferObjectExtensionProvider incomingExtProv;

    protected Saros saros;

    protected SessionIDObservable sessionID;

    public DataTransferManager(Saros saros, SessionIDObservable sessionID,
        PreferenceUtils prefUtils) {
        this.sessionID = sessionID;
        this.saros = saros;
        this.forceFileTransferByChat = prefUtils.forceFileTranserByChat();

        /*
         * register a property change listeners to keep forceFileTransferByChat
         * up-to-date
         */
        saros.getPreferenceStore().addPropertyChangeListener(propertyListener);

        transferModeDispatch.add(trackingTransferModeListener);
    }

    public JingleFileTransferManager getJingleManager() {
        try {
            if (startingJingleThread == null)
                return null;

            startingJingleThread.join();
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
            return null;
        }
        return jingleManager.getValue();
    }

    /**
     * JingleTransferListener receives from all JingleFileTransferSessions
     * {@link IncomingTransferObject}
     */
    protected final class JingleTransferListener implements
        IJingleFileTransferListener {

        /**
         * It changes to the Thread context of XMPPChatTransmiter before calling
         * XMPPReceiver's processPacket.
         */
        public void incomingData(final IncomingTransferObject transferObject) {
            addIncomingTransferObject(transferObject);
        }
    }

    /**
     * Adds an incoming transfer.
     * 
     * @param transferObjectDescription
     *            An IncomingTransferObject that has the TransferDescription as
     *            content to provide information of the incoming transfer to
     *            upper layers.
     */
    protected void addIncomingTransferObject(
        final IncomingTransferObject transferObjectDescription) {

        final TransferDescription description = transferObjectDescription
            .getTransferDescription();

        final IncomingTransferObject transferObjectData = new IncomingTransferObject() {

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

                    byte[] content = transferObjectDescription.accept(progress);

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

                    transferModeDispatch.transferFinished(description
                        .getSender(), getTransferMode(), true, content.length,
                        duration);

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
                transferObjectDescription.reject();
            }

            public NetTransferMode getTransferMode() {
                return transferObjectDescription.getTransferMode();
            }
        };

        // ask upper layer to accept
        dispatchThreadContext.executeAsDispatch(new Runnable() {
            public void run() {
                receiver.processIncomingTransferObject(description,
                    transferObjectData);
            }
        });
    }

    protected class XMPPFileTransferListener implements FileTransferListener {

        public void fileTransferRequest(final FileTransferRequest request) {

            final TransferDescription transferDescription;
            try {
                transferDescription = TransferDescription.fromBase64(request
                    .getDescription());
            } catch (IOException e) {
                log.error("Incoming File Transfer via IBB failed: ", e);
                return;
            }

            final IncomingTransferObject transferObject = new IncomingTransferObject() {

                /**
                 * @throws SarosCancellationException
                 *             It will be thrown if the user (locally or
                 *             remotely) has canceled the transfer.
                 * 
                 * @throws IOException
                 *             It will be thrown if the stream negotiation, the
                 *             read from InputStream or the inflation failed
                 *             during I/O operations or Timeout.
                 */
                public byte[] accept(SubMonitor monitor)
                    throws SarosCancellationException, IOException {

                    monitor.beginTask("Receive via IBB", 10000);

                    // TODO how to handle files larger than the max size of a
                    // byte array
                    IncomingFileTransfer accept = request.accept();
                    monitor.worked(100);

                    byte[] content;
                    InputStream in = null;
                    try {
                        if (monitor.isCanceled())
                            throw new LocalCancellationException();
                        in = accept.recieveFile();
                        monitor.worked(100);
                        content = Util.toByteArray(in, request.getFileSize(),
                            monitor.newChild(8000));
                        IOUtils.closeQuietly(in);

                        // File is meant to be empty
                        if (transferDescription.emptyFile) {
                            content = new byte[0];
                        }

                        if (transferDescription.compressInDataTransferManager())
                            content = Util.inflate(content, monitor
                                .newChild(1500));

                    } catch (LocalCancellationException e) {
                        log.info("Local monitor was cancelled.");
                        throw e;
                    } catch (IOException e) {
                        if (monitor.isCanceled())
                            throw new LocalCancellationException();
                        log.error("Incoming File Transfer via IBB failed: ", e);
                        throw e;
                    } catch (XMPPException e) {
                        if (monitor.isCanceled())
                            throw new LocalCancellationException();

                        Throwable tmp = e.getWrappedThrowable();
                        Exception cause = (tmp != null) ? (Exception) tmp : e;

                        if (cause instanceof InterruptedException) {
                            log.warn("Interrupted on IBB stream negotiation.");
                        } else if (e.getCause() instanceof TimeoutException) {
                            log
                                .warn("Timeout while waiting for incoming File ransfer via IBB.");
                        } else if (cause instanceof ExecutionException) {
                            // unwrap
                            Throwable t = cause.getCause();
                            if (t != null)
                                log.warn(t.getMessage(), t);
                        }

                        throw new IOException(
                            "Failed to negotiate Stream via IBB.");
                    }

                    monitor.worked(100);
                    monitor.done();

                    return content;
                }

                public TransferDescription getTransferDescription() {
                    return transferDescription;
                }

                public void reject() throws IOException {
                    request.reject();
                }

                public NetTransferMode getTransferMode() {
                    return NetTransferMode.IBB;
                }

            };
            addIncomingTransferObject(transferObject);
        }
    }

    private static final Logger log = Logger
        .getLogger(DataTransferManager.class);

    public interface Transmitter {

        public String getName();

        /**
         * Should return true if this transmitter can send data to the given
         * JID.
         */
        public boolean isSuitable(JID jid);

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
        public NetTransferMode send(TransferDescription data, byte[] content,
            SubMonitor callback) throws IOException, SarosCancellationException;

    }

    protected Transmitter xmppFileTransfer = new Transmitter() {

        public NetTransferMode send(TransferDescription data, byte[] content,
            SubMonitor progress) throws IOException, LocalCancellationException {

            final long startTime = System.nanoTime();
            log.debug("[IBB] Sending to " + data.getRecipient() + ": "
                + data.toString() + ", size: "
                + Util.formatByte(content.length));

            OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(data.getRecipient().toString());

            progress.setWorkRemaining(110);
            progress.subTask("Negotiating file transfer");

            if (content.length == 0) {
                content = new byte[] { 0 };
                data.setEmptyFile(true);
            }

            // The file path is irrelevant
            transfer.sendStream(new ByteArrayInputStream(content),
                "Filename managed by Description", content.length, data
                    .toBase64());

            progress.worked(10);

            int worked = 0;

            InterruptedException interrupted = null;

            progress.subTask("Sending data");
            while (!transfer.isDone()) {

                if (progress.isCanceled()) {
                    transfer.cancel();
                    throw new LocalCancellationException();
                }
                int newProgress = (int) ((100.0 * transfer.getAmountWritten()) / Math
                    .max(1, content.length));
                log.trace("Progress " + newProgress + "%");

                if (worked < newProgress) {
                    progress.worked(newProgress - worked);
                    worked = newProgress;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    interrupted = e;
                }
            }

            if (interrupted != null) {
                log.error("Code not designed to be interruptable", interrupted);
                Thread.currentThread().interrupt();
            }

            if (transfer.getStatus() == Status.error) {
                throw new IOException("XMPPError in IBB-FileTransfer: "
                    + transfer.getError() + " caused by: "
                    + transfer.getException());
            }

            if (transfer.getStatus() != Status.complete) {
                throw new IOException("Error in IBB-FileTransfer wrong state: "
                    + transfer.getStatus());
            }

            long duration = Math.max(0, System.nanoTime() - startTime) / 1000000;

            log.debug("[IBB] Finished sending to " + data.getRecipient() + ": "
                + data.toString() + ", "
                + Util.throughput(content.length, duration));

            return NetTransferMode.IBB;
        }

        public boolean isSuitable(JID jid) {
            return true;
        }

        public String getName() {
            return "IBB";
        }
    };

    protected Transmitter jingle = new Transmitter() {

        public NetTransferMode send(TransferDescription data, byte[] content,
            SubMonitor progress) throws SarosCancellationException, IOException {
            try {
                JingleFileTransferManager jftm = getJingleManager();
                if (jftm == null)
                    throw new IOException("Jingle is disabled");

                return jftm.send(data, content, progress);
            } catch (JingleSessionException e) {
                throw new CausedIOException(e);
            }
        }

        public boolean isSuitable(JID jid) {

            if (preferenceUtils.forceFileTranserByChat())
                return false;

            if (!discoveryManager.isJingleSupported(jid))
                return false;

            JingleFileTransferManager jftm = getJingleManager();
            if (jftm == null)
                return false;

            JingleConnectionState state = jftm.getState(jid);

            // If null, then we have never tried to connect
            if (state == null)
                return true;

            // Only use Jingle, if not in ERROR
            return state != JingleConnectionState.ERROR;
        }

        public String getName() {
            return "Jingle";
        }
    };

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
        // TODO Buffer correctly when not connected....
        // this.fileTransferQueue.offer(transfer);
        // sendNextFile();

        Transmitter[] transmitters;
        if (forceFileTransferByChat) {
            transmitters = new Transmitter[] { xmppFileTransfer };
        } else {
            transmitters = new Transmitter[] { jingle, xmppFileTransfer };
        }

        progress.beginTask("Sending Data", 100);

        if (transferData.compressInDataTransferManager()) {
            input = Util.deflate(input, progress.newChild(15));
        }

        SubMonitor transferProgress = progress.newChild(85);

        try {
            // Try all transmitters
            for (Transmitter transmitter : transmitters) {
                if (sendData(transmitter, transferData, input, transferProgress)) {
                    // Successfully sent!
                    transferProgress.done();
                    return;
                }
            }
            // No transmitter worked! :-(
            throw new IOException(Util.prefix(transferData.recipient)
                + "Exhausted all options " + Arrays.toString(transmitters)
                + " to send " + transferData);
        } finally {
            progress.done();
        }
    }

    /**
     * Tries to send the given data using the given transmitter returning true
     * if the transfer was successfully completed, false otherwise.
     * 
     * @throws SarosCancellationException
     *             It will be thrown if the local user or remote user has
     *             canceled the transfer.
     */
    protected boolean sendData(Transmitter transmitter,
        TransferDescription transferData, byte[] content, SubMonitor progress)
        throws SarosCancellationException {

        if (!transmitter.isSuitable(transferData.recipient))
            return false;

        try {
            long startTime = System.nanoTime();

            NetTransferMode mode = transmitter.send(transferData, content,
                progress);

            long duration = Math.max(0, System.nanoTime() - startTime) / 1000000;

            transferModeDispatch.transferFinished(transferData.recipient, mode,
                false, content.length, duration);
            return true;
        } catch (SarosCancellationException e) {
            throw e; // Rethrow to circumvent the Exception catch below
        } catch (CausedIOException e) {
            log.error(Util.prefix(transferData.recipient) + "Failed to send "
                + transferData + " with " + transmitter.getName() + ":", e
                .getCause());
        } catch (Exception e) {
            log.error(Util.prefix(transferData.recipient) + "Failed to send "
                + transferData + " with " + transmitter.getName() + ":", e);
            // Try other transport methods
        }
        return false;
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
                listener.clear();
            }
        }

        public synchronized void transferFinished(JID jid,
            NetTransferMode newMode, boolean incoming, long size,
            long transmissionMillisecs) {

            for (ITransferModeListener listener : listeners) {
                listener.transferFinished(jid, newMode, incoming, size,
                    transmissionMillisecs);
            }
        }
    }

    public void awaitJingleManager(JID jid) {

        if (discoveryManager.isJingleSupported(jid)) {
            getJingleManager();
        }
    }

    public void prepareConnection(final XMPPConnection connection) {

        // Create Containers
        this.connection = connection;

        this.fileTransferQueue = new ConcurrentLinkedQueue<TransferData>();
        this.jingleManager.setValue(null);

        this.fileTransferManager = new FileTransferManager(connection);
        this.fileTransferManager
            .addFileTransferListener(new XMPPFileTransferListener());

        if (!preferenceUtils.forceFileTranserByChat()) {
            // Start Jingle Manager asynchronous
            this.startingJingleThread = new Thread(new Runnable() {
                /**
                 * @review runSafe OK
                 */
                public void run() {
                    try {
                        jingleManager
                            .setValue(new JingleFileTransferManager(saros,
                                connection, jingleListener, incomingExtProv));
                        log.debug("Jingle Manager started");
                    } catch (Exception e) {

                        if (saros.isConnected())
                            log.error("Jingle Manager could not be started", e);
                        else
                            log.debug("Jingle Manager could not be started,"
                                + " because Saros was disconnected from"
                                + " XMPP server.");

                        jingleManager.setValue(null);
                    }
                }
            });
            this.startingJingleThread.start();
        }
    }

    public enum NetTransferMode {
        UNKNOWN("???", false), IBB("IBB", false), JINGLETCP("Jingle/TCP", true), JINGLEUDP(
            "Jingle/UDP", true), HANDMADE("Chat", false);

        String name;

        boolean p2p;

        NetTransferMode(String name, boolean p2p) {
            this.name = name;
            this.p2p = p2p;
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
     * @return the last TransferMode used when receiving a file from the given
     *         user or UNKNOWN if no file was received since the last
     *         connection-reset.
     */
    public NetTransferMode getIncomingTransferMode(JID jid) {
        NetTransferMode result = incomingTransferModes.get(jid);
        if (result == null) {
            return NetTransferMode.UNKNOWN;
        } else {
            return result;
        }
    }

    /**
     * @return the last TransferMode that was used to send a file to the given
     *         user or null if no file has been sent since the last
     *         connection-reset.
     */
    public NetTransferMode getOutgoingTransferMode(JID jid) {
        NetTransferMode result = outgoingTransferModes.get(jid);
        if (result == null) {
            return NetTransferMode.UNKNOWN;
        } else {
            return result;
        }
    }

    public void disposeConnection() {
        fileTransferQueue.clear();
        transferModeDispatch.clear();

        connection = null;

        // If Jingle is still starting, wait for it...
        if (startingJingleThread != null) {
            try {
                startingJingleThread.join();
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Terminate all Jingle connections and notify everybody who used this
        // JingleFileTransferManager
        if (jingleManager.getValue() != null) {
            jingleManager.getValue().terminateAllJingleSessions();
            jingleManager.setValue(null);
        }

        fileTransferManager = null;
        startingJingleThread = null;
    }

    public void startConnection() {
        // TODO The data transfer manager does not support caching yet
    }

    public void stopConnection() {
        // TODO The data transfer manager does not support caching yet
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

    public TransferModeDispatch getTransferModeDispatch() {
        return transferModeDispatch;
    }

}
