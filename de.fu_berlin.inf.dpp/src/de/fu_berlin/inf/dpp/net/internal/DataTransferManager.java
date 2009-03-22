package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.TransferData;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.IDataReceiver;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.extensions.DataTransferExtension;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.observables.JingleFileTransferManagerObservable;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class is responsible for handling all transfers of binary data
 * 
 * @Component The single instance of this class per application is managed by
 *            PicoContainer
 */
public class DataTransferManager implements ConnectionSessionListener {

    private DataTransferExtension handmadeDataTransferExtension = new DataTransferExtension() {

        /**
         * Receives a data buffer sent by a chat message. The data will be
         * decoded from base64 encoding. Splitted transfer will be buffered
         * until all chunks are received. Then the file will be reconstructed
         * and processed as a whole.
         */
        @Override
        public void chunkReceived(JID fromJID, String sName, String desc,
            int index, int maxIndex, String sData) {

            setTransferMode(fromJID, NetTransferMode.HANDMADE, true);

            TransferDescription transferDescription;
            try {
                transferDescription = TransferDescription.fromBase64(desc);
            } catch (IOException e) {
                log.error(
                    "Error while decoding TransferDescription in ChatTransfer",
                    e);
                return;
            }

            // Is this a transfer with multiple parts?
            if (maxIndex > 1) {

                log.debug("Received chunk " + index + " of " + maxIndex
                    + " of " + transferDescription.toString());

                // check for previous chunks
                IncomingFile ifile = incomingFiles.get(desc);
                if (ifile == null) {
                    /*
                     * this is the first received chunk->create incoming file
                     * object
                     */
                    ifile = new IncomingFile();
                    ifile.receivedChunks++;
                    ifile.chunkCount = maxIndex;
                    ifile.name = sName;
                    for (int i = 0; i < maxIndex; i++) {
                        ifile.messageBuffer.add(null);
                    }
                    ifile.messageBuffer.set(index - 1, sData);
                    incomingFiles.put(sName, ifile);
                    return;
                } else {
                    // this is a following chunk
                    ifile.receivedChunks++;
                    ifile.messageBuffer.set(index - 1, sData);

                    if (ifile.isComplete() == false) {
                        return;
                    } else {

                        // join the buffers to restore the file from chunks
                        sData = "";
                        for (int i = 0; i < maxIndex; i++) {
                            sData += ifile.messageBuffer.get(i);
                        }
                        incomingFiles.remove(ifile.name);
                    }
                }
            }

            byte[] dataOrg = Base64.decodeBase64(sData.getBytes());
            if (dataOrg == null)
                return;

            receiveData(transferDescription, new ByteArrayInputStream(dataOrg));
        }
    };

    protected FileTransferManager fileTransferManager;

    protected ConcurrentLinkedQueue<TransferData> fileTransferQueue;

    protected Map<String, IncomingFile> incomingFiles;

    protected Thread startingJingleThread;

    protected JingleDiscoveryManager jingleDiscovery;

    protected List<IDataReceiver> receivers;

    protected XMPPConnection connection;

    protected XMPPChatTransmitter chatTransmitter;

    protected List<ITransferModeListener> transferModeListeners;

    public DataTransferManager() {
        transferModeListeners = new ArrayList<ITransferModeListener>();
        transferModeListeners.add(trackingTransferModeListener);
    }

    @Inject
    public JingleFileTransferManagerObservable jingleManager;

    public JingleFileTransferManager getJingleManager() {
        try {
            if (startingJingleThread == null)
                return null;

            startingJingleThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        return jingleManager.getValue();
    }

    /**
     * A simple struct that is used to manage incoming chunked files via
     * chat-file transfer
     */
    protected static class IncomingFile {
        String name;
        int receivedChunks;
        int chunkCount;
        List<String> messageBuffer;

        IncomingFile() {
            this.messageBuffer = new LinkedList<String>();
        }

        boolean isComplete() {
            return (this.receivedChunks == this.chunkCount);
        }
    }

    private final class JingleTransferListener implements
        IJingleFileTransferListener {

        public void incomingData(TransferDescription data, InputStream input,
            NetTransferMode mode) {

            setTransferMode(data.getSender(), mode, true);
            receiveData(data, input);
        }
    }

    private class IBBTransferListener implements FileTransferListener {

        public void fileTransferRequest(final FileTransferRequest request) {

            Util.runSafeAsync("IBBTransferListener-fileTransferRequest-", log,
                new Runnable() {

                    public void run() {
                        try {
                            TransferDescription data = TransferDescription
                                .fromBase64(request.getDescription());

                            log.debug("Incoming file transfer via IBB: "
                                + data.toString());

                            IncomingFileTransfer accept = request.accept();

                            InputStream in = accept.recieveFile();

                            byte[] content;
                            try {
                                content = IOUtils.toByteArray(in);
                            } finally {
                                IOUtils.closeQuietly(in);
                            }
                            setTransferMode(data.getSender(),
                                NetTransferMode.IBB, true);
                            receiveData(data, new ByteArrayInputStream(content));
                        } catch (Exception e) {
                            log.error(
                                "Incoming File Transfer via IBB failed: ", e);

                            IInvitationProcess process = chatTransmitter
                                .getInvitationProcess(new JID(request
                                    .getRequestor()));
                            if (process != null) {
                                process.cancel(e.getMessage(), false);
                            }
                        }
                    }
                });
        }
    }

    private static final Logger log = Logger
        .getLogger(DataTransferManager.class.getName());

    public interface Transmitter {

        public String getName();

        /**
         * Should return true if this transmitter can send data to the given
         * JID.
         */
        public boolean isSuitable(JID jid);

        /**
         * Send the given data as a blocking operation.
         * 
         * If this call returns the data has been send successfully, otherwise
         * an IOException is thrown with the reason why the transfer failed.
         * 
         * @param data
         *            The data to be sent.
         * @throws IOException
         *             if the send failed
         */
        public NetTransferMode send(TransferDescription data, byte[] content,
            IFileTransferCallback callback) throws IOException;

    }

    /**
     * Sends a data buffer to a recipient using chat messages. The buffer is
     * transmitted Base64 encoded and split into blocks of size MAX_MSG_LENGTH.
     * 
     * This is not IBB (XEP-96)!!
     * 
     */
    protected Transmitter handmade = new Transmitter() {

        public NetTransferMode send(TransferDescription data, byte[] content,
            IFileTransferCallback callback) throws IOException {

            final int maxMsgLen = Saros.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE);

            // Convert byte array to base64 string
            byte[] bytes64 = Base64.encodeBase64(content);

            String data64;
            try {
                data64 = new String(bytes64, "UTF-8");
            } catch (UnsupportedCharsetException e1) {
                data64 = new String(bytes64);
            }

            // send large data sets in several messages
            int tosend = data64.length();
            int pcount = (tosend / maxMsgLen)
                + ((tosend % maxMsgLen == 0) ? 0 : 1);
            int start = 0;
            try {
                for (int i = 1; i <= pcount; i++) {
                    int psize = Math.min(tosend, maxMsgLen);
                    int end = start + psize;

                    PacketExtension extension = DataTransferExtension
                        .getDefault().create("Filename managed by Description",
                            data.toBase64(), i, pcount,
                            data64.substring(start, end));

                    chatTransmitter.sendMessage(data.getRecipient(), extension);

                    start = end;
                    tosend -= psize;

                }
            } catch (Exception e) {
                throw new IOException("Sending failed", e);
            }
            return NetTransferMode.HANDMADE;
        }

        public boolean isSuitable(JID jid) {
            return true;
        }

        public String getName() {
            return "ChatTransfer";
        }
    };

    protected Transmitter ibb = new Transmitter() {

        public NetTransferMode send(TransferDescription data, byte[] content,
            IFileTransferCallback callback) throws IOException {

            log.debug("[IBB] Sending to " + data.getRecipient() + ": "
                + data.toString());

            OutgoingFileTransfer
                .setResponseTimeout(XMPPChatTransmitter.MAX_TRANSFER_RETRIES * 1000);
            OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(data.getRecipient().toString());

            FileTransferProgressMonitor monitor = new FileTransferProgressMonitor(
                transfer, callback, content.length);
            monitor.start();

            // The file path is irrelevant
            transfer.sendStream(new ByteArrayInputStream(content),
                "Filename managed by Description", content.length, data
                    .toBase64());

            /* wait for complete transfer. */
            while (monitor.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (monitor.getMonitoringException() != null) {
                throw new IOException("RuntimeError in IBB-FileTransfer: ",
                    monitor.getMonitoringException());
            }

            if (transfer.getStatus() == Status.error) {
                throw new IOException("XMPPError in IBB-FileTransfer: "
                    + transfer.getError());
            }

            if (transfer.getStatus() != Status.complete) {
                throw new IOException("Error in IBB-FileTransfer wrong state: "
                    + transfer.getStatus());
            }

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
            IFileTransferCallback callback) throws IOException {
            try {
                JingleFileTransferManager jftm = getJingleManager();
                if (jftm == null)
                    throw new IOException("Jingle is disabled");

                return jftm.send(data, content);
            } catch (JingleSessionException e) {
                throw new IOException(e);
            }
        }

        public boolean isSuitable(JID jid) {

            if (Saros.getFileTransferModeViaChat())
                return false;

            /*
             * TODO This is not a safe way to determine whether the user really
             * supports Jingle at this point in time - He might have left and
             * reconnected and changed his Jingle settings in between
             */
            if (!jingleDiscovery.getCachedJingleSupport(jid))
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

    protected void sendData(TransferDescription transferData, byte[] content,
        IFileTransferCallback callback) throws IOException {

        // TODO Buffer correctly when not connected....
        // this.fileTransferQueue.offer(transfer);
        // sendNextFile();

        for (Transmitter transmitter : new Transmitter[] { jingle, ibb }) {

            if (transmitter.isSuitable(transferData.recipient)) {
                try {
                    NetTransferMode mode = transmitter.send(transferData,
                        content, callback);

                    setTransferMode(transferData.recipient, mode, false);
                    return;
                } catch (Exception e) {
                    log.error("Failed to send file with "
                        + transmitter.getName() + ":", e);
                }
            }
        }

        throw new IOException("Exhausted all options to send the given file.");

    }

    /**
     * Adds the given receiver to the top of the stack of the receivers notified
     * when data arrives.
     * 
     * The receiver should return true if it has consumed the given data.
     * 
     * @param dataReceiver
     */
    public void addDataReceiver(IDataReceiver receiver) {
        receivers.add(0, receiver);
    }

    public void removeDataReceiver(IDataReceiver receiver) {
        receivers.remove(receiver);
    }

    /**
     * Will dispatch the received data to the IDataReceivers registered.
     * 
     * Closes the input stream, before returning.
     */
    protected void receiveData(TransferDescription data, InputStream input) {

        try {
            switch (data.type) {
            case ARCHIVE_TRANSFER:
                for (IDataReceiver receiver : receivers) {
                    boolean consumed = receiver.receivedArchive(data, input);
                    if (consumed)
                        return;
                }
                break;
            case FILELIST_TRANSFER:
                for (IDataReceiver receiver : receivers) {
                    boolean consumed = receiver.receivedFileList(data, input);
                    if (consumed)
                        return;
                }
                break;
            case RESOURCE_TRANSFER:
                for (IDataReceiver receiver : receivers) {
                    boolean consumed = receiver
                        .receivedResource(data.sender, new Path(
                            data.file_project_path), input, data.timestamp);
                    if (consumed)
                        return;
                }

                break;
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public void addTransferModeListener(ITransferModeListener listener) {
        transferModeListeners.add(listener);
    }

    public void removeTransferModeListener(ITransferModeListener listener) {
        transferModeListeners.remove(listener);
    }

    public void setTransferMode(JID from, NetTransferMode mode, boolean incoming) {
        for (ITransferModeListener listener : transferModeListeners) {
            listener.setTransferMode(from, mode, incoming);
        }
    }

    public void awaitJingleManager(JID jid) {

        if (jingleDiscovery.getCachedJingleSupport(jid)) {
            getJingleManager();
        }
    }

    protected IDataReceiver defaultReceiver = new IDataReceiver() {

        public boolean receivedResource(JID from, Path path, InputStream input,
            int time) {

            log.info("Incoming resource from " + from.toString() + ": " + path);

            // TODO CJ: move this to business logic
            IInvitationProcess process = chatTransmitter
                .getInvitationProcess(from);
            if (process != null) {
                process.resourceReceived(from, path, input);
                return true;
            }

            // Otherwise
            TimedActivity timedActivity = new TimedActivity(new FileActivity(
                from.toString(), path, input), time);

            chatTransmitter.receiveActivities(from, Collections
                .singletonList(timedActivity));

            return true;
        }

        public boolean receivedFileList(TransferDescription data,
            InputStream input) {

            IInvitationProcess process = chatTransmitter
                .getInvitationProcess(data.sender);
            if (process == null) {
                log.warn("Received FileList from unknown user ["
                    + data.sender.getBase() + "]");
                return false;
            }

            String fileListAsString;
            try {
                fileListAsString = Util.read(input);
            } catch (IOException e) {
                log.error("Error receiving FileList", e);
                return true;
            }

            FileList fileList = null;

            if (fileListAsString != null) {
                try {
                    fileList = new FileList(fileListAsString);
                } catch (Exception e) {
                    process.cancel("Could not parse your FileList", false);
                    log.error("Could not parse FileList", e);
                }
            }

            process.fileListReceived(data.getSender(), fileList);
            return true;
        }

        public boolean receivedArchive(TransferDescription data,
            InputStream input) {

            log.debug("Incoming archive [" + data.sender.getName() + "]");

            long time = System.currentTimeMillis();

            ZipInputStream zip = new ZipInputStream(input);

            try {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    receivedResource(data.getSender(),
                        new Path(entry.getName()), new FilterInputStream(zip) {
                            @Override
                            public void close() throws IOException {
                                // don't close the ZipInputStream, we close the
                                // entry ourselves...
                            }
                        }, data.timestamp);

                    zip.closeEntry();
                }
                log.debug(String.format("Unpacked archive [%s] in %d s",
                    data.sender.getName(),
                    (System.currentTimeMillis() - time) / 1000));

            } catch (IOException e) {
                log.error("Failed to receive and unpack archive", e);
            } finally {
                IOUtils.closeQuietly(zip);
            }
            return true;
        }
    };

    public void prepare(final XMPPConnection connection) {

        // Create Containers
        this.connection = connection;

        this.fileTransferQueue = new ConcurrentLinkedQueue<TransferData>();
        this.incomingFiles = new HashMap<String, IncomingFile>();
        this.receivers = new LinkedList<IDataReceiver>();
        this.receivers.add(defaultReceiver);
        this.jingleManager.setValue(null);

        this.fileTransferManager = new FileTransferManager(connection);
        this.fileTransferManager
            .addFileTransferListener(new IBBTransferListener());

        OutgoingFileTransfer
            .setResponseTimeout(XMPPChatTransmitter.MAX_TRANSFER_RETRIES * 1000);

        connection.addPacketListener(handmadeDataTransferExtension,
            handmadeDataTransferExtension.getFilter());

        // Create JingleDiscoveryManager
        jingleDiscovery = new JingleDiscoveryManager(connection);

        if (!Saros.getFileTransferModeViaChat()) {
            // Start Jingle Manager asynchronous
            this.startingJingleThread = new Thread(new Runnable() {
                /**
                 * @review runSafe OK
                 */
                public void run() {
                    try {
                        jingleManager.setValue(new JingleFileTransferManager(
                            connection, new JingleTransferListener()));
                        log.debug("Jingle Manager started");
                    } catch (Exception e) {
                        log.error("Jingle Manager could not be started", e);
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

    Map<JID, NetTransferMode> incomingTransferModes = Collections
        .synchronizedMap(new HashMap<JID, NetTransferMode>());
    Map<JID, NetTransferMode> outgoingTransferModes = Collections
        .synchronizedMap(new HashMap<JID, NetTransferMode>());

    ITransferModeListener trackingTransferModeListener = new ITransferModeListener() {

        public void clear() {
            incomingTransferModes.clear();
            outgoingTransferModes.clear();
        }

        public void setTransferMode(JID jid, NetTransferMode newMode,
            boolean incoming) {
            if (incoming) {
                incomingTransferModes.put(jid, newMode);
            } else {
                outgoingTransferModes.put(jid, newMode);
            }
        }
    };

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

    public void dispose() {
        incomingFiles.clear();
        fileTransferQueue.clear();
        receivers.clear();
        for (ITransferModeListener listener : transferModeListeners) {
            listener.clear();
        }

        if (connection != null)
            connection.removePacketListener(handmadeDataTransferExtension);

        connection = null;

        // If Jingle is still starting, wait for it...
        if (startingJingleThread != null) {
            try {
                startingJingleThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Terminate all Jingle connections and notify everybody who used this
        // JingleFileTransferManager
        if (jingleManager.getValue() != null) {
            jingleManager.getValue().terminateAllJingleSessions();
            jingleManager.setValue(null);
        }

        fileTransferManager = null;
        jingleDiscovery = null;
        startingJingleThread = null;
    }

    public void start() {
        // TODO Auto-generated method stub

    }

    public void stop() {
        // TODO Auto-generated method stub

    }

}
