package de.fu_berlin.inf.dpp.net.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import com.google.protobuf.ByteString;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.internal.BinaryPacketProto.BinaryPacket;
import de.fu_berlin.inf.dpp.net.internal.BinaryPacketProto.BinaryPacket.PacketType;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * BinaryChannel is a class that encapsulates a bidirectional communication
 * channel between two participants.
 * 
 * The threading requirements of this class are the following:
 * 
 * 1.) sendDirect() is a reentrant method for sending data. Any number of
 * threads can call it in parallel.
 * 
 * 2.) {@link #receiveIncomingTransferObject(SubMonitor)} is the mainloop of
 * this class. No sending/receiving will work if this method is not called
 * repeatedly. BUT it may only be called from a single thread at any point in
 * time!
 * 
 * 3.) Calling {@link BinaryChannelTransferObject#accept(SubMonitor)} must be
 * done from a thread different than
 * {@link #receiveIncomingTransferObject(SubMonitor)}. Otherwise the
 * BinaryChannel will be blocked.
 * 
 * @author sszuecs
 * @author coezbek
 */
public class BinaryChannel {

    /**
     * Known error codes that do not need any special debug or error output
     */
    public static String[] ACCEPTED_ERROR_CODES_ON_CLOSURE = {
        "service-unavailable(503)" /* peer closed stream already (SOCKS5) */,
        "recipient-unavailable(404)" /* peer is offline (IBB) */};

    private static final Logger log = Logger.getLogger(BinaryChannel.class);

    /**
     * Max size of data chunks
     */
    public static final int CHUNKSIZE = 2 * 65535;

    /**
     * Collect the Packets until an entire Object is received. objectid -->
     * [Packet0, Packet1, ..]
     */
    protected Map<Integer, BlockingQueue<BinaryPacket>> incomingPackets;
    {
        // the intermediate Map MUST not be used for anything except as a
        // backing
        // map for the incomingTransfers
        Map<Integer, BlockingQueue<BinaryPacket>> intermediate = AutoHashMap
            .getBlockingQueueHashMap();
        incomingPackets = Collections.synchronizedMap(intermediate);
    }

    protected AutoHashMap<Integer, List<BinaryPacket>> incomingDescriptionPackets = AutoHashMap
        .getListHashMap();

    /**
     * Contains BinaryPackets by objectID sent to us by the remote side in
     * confirmation of our packets. The objectIDs are thus ours (this is also
     * the reason why this is a separate Map than the incomingPackets-Map.
     */
    protected Map<Integer, BlockingQueue<BinaryPacket>> remoteTransfers;
    {
        // the intermediate Map MUST not be used for anything except as a
        // backing
        // map for the remoteTransfers
        Map<Integer, BlockingQueue<BinaryPacket>> intermediate = AutoHashMap
            .getBlockingQueueHashMap();
        remoteTransfers = Collections.synchronizedMap(intermediate);
    }

    // network stuff
    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected BytestreamSession session;

    /**
     * NetTransferMode to identify the transport method of the underlying socket
     * connection.
     */
    protected NetTransferMode transferMode;

    /**
     * Returns the byte Array for the given List of BinaryPacket.
     */
    public static byte[] getData(List<BinaryPacket> list) {
        int size = 0;

        for (BinaryPacket packet : list) {
            size += packet.getData().size();
        }

        byte[] result = new byte[size];
        int offset = 0;
        for (BinaryPacket packet : list) {
            packet.getData().copyTo(result, offset);
            offset += packet.getData().size();
        }
        return result;
    }

    /**
     * Creates an observer BinaryChannel.
     * 
     * @throws IOException
     */
    // TODO: update TestCases, remove Socket variable and this constructor
    @Deprecated
    public BinaryChannel(Socket socket, NetTransferMode transferMode)
        throws IOException {
        this.socket = socket;
        this.transferMode = transferMode;

        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());
    }

    public NetTransferMode getTransferMode() {
        return transferMode;
    }

    public BinaryChannel(BytestreamSession session, NetTransferMode mode)
        throws IOException {
        this.session = session;
        this.session.setReadTimeout(0); // keep connection alive
        this.transferMode = mode;

        outputStream = new BufferedOutputStream(session.getOutputStream());
        inputStream = new BufferedInputStream(session.getInputStream());
    }

    /**
     * Run the BinaryChannels main loop until the next IncomingTransferObject is
     * received.
     * 
     * Without calling this method, the BinaryChannel will not work.
     * 
     * @nonreentrant This method is not reentrant! Use only with a single
     *               thread!
     * 
     * @throws LocalCancellationException
     *             If waiting was canceled using the supplied progress.
     * @throws IOException
     *             If the associated socket broke, while reading or if the
     *             socket has already been disposed.
     * @throws ClassNotFoundException
     *             If the data sent from the other side could not be decoded.
     */
    public IncomingTransferObject receiveIncomingTransferObject(
        SubMonitor progress) throws LocalCancellationException, IOException,
        ClassNotFoundException {

        try {
            while (true) {

                if (progress.isCanceled())
                    throw new LocalCancellationException();

                if (inputStream == null)
                    throw new IOException("Socket already disposed");

                BinaryPacket packet = BinaryPacket
                    .parseDelimitedFrom(inputStream);
                if (packet == null) {
                    if (progress.isCanceled())
                        throw new LocalCancellationException();
                    throw new EOFException("No more packets");
                }

                final int objectid = packet.getObjectid();

                switch (packet.getType()) {
                case TRANSFERDESCRIPTION:
                    incomingDescriptionPackets.get(objectid).add(packet);

                    if (packet.getRemaining() == 0) {
                        List<BinaryPacket> list = incomingDescriptionPackets
                            .remove(objectid);

                        byte[] data = getData(list);

                        final TransferDescription transferDescription = buildTransferDescription(
                            objectid, data, progress);

                        // Side-effect! Create a new BlockingQueue in the
                        // incomingPackets AutoHashMap!
                        BlockingQueue<BinaryPacket> queue = incomingPackets
                            .get(objectid);
                        if (queue.size() > 0) {
                            log.warn("New incoming transfer, but already"
                                + " packets exist with the given ID!"
                                + " Discarding - " + transferDescription);
                            queue.clear();
                        }

                        return new BinaryChannelTransferObject(this,
                            transferDescription, objectid);
                    }
                    break;
                case DATA:
                    // Only keep packets for DATA we are expecting!
                    if (incomingPackets.containsKey(objectid))
                        incomingPackets.get(objectid).add(packet);
                    else {
                        if (log.isDebugEnabled())
                            log.warn("Discarding Packet: " + packet);
                    }
                    break;
                case CANCEL:
                    incomingDescriptionPackets.remove(objectid);
                    if (incomingPackets.containsKey(objectid)) {
                        // Pass the cancelation to the
                        // BinaryChanelTransferObject
                        // if no incomingPackets is found the transfer has
                        // already been finished
                        incomingPackets.get(objectid).add(packet);
                    }
                    break;
                case FINISHED: // fall through
                case REJECT:
                    remoteTransfers.get(objectid).add(packet);
                    break;
                case SHUTDOWN:
                    // TODO Terminate BinaryChannel
                    break;
                default:
                    log.error("Unknown BinaryHeaderType: " + packet.getType());
                }
            }

        } finally {
            progress.done();
        }
    }

    /**
     * It sends a given Packet through the ObjectOutputStream.
     * 
     * @throws IOException
     */
    protected void send(BinaryPacket packet) throws IOException {
        synchronized (outputStream) {
            packet.writeDelimitedTo(outputStream);
            outputStream.flush();
        }
    }

    protected TransferDescription buildTransferDescription(int objectid,
        byte[] description, SubMonitor progress) throws IOException,
        ClassNotFoundException {

        TransferDescription transferDescription = TransferDescription
            .fromByteArray(Utils.inflate(description, progress.newChild(1)));
        transferDescription.objectid = objectid;
        return transferDescription;
    }

    public boolean isConnected() {
        return inputStream != null && outputStream != null;
    }

    /**
     * It sends the given transferDescription and data direct. Supports
     * cancellation by given SubMonitor.
     * 
     * @reentrant This method may be called from multiple threads at once
     * 
     * @blocking
     * 
     * @throws IOException
     *             If there was an error sending (for instance the socket is
     *             closed) or
     * 
     * @throws LocalCancellationException
     *             If the local user canceled and this cancellation was
     *             performed by this method.
     * @throws SarosCancellationException
     */
    public void sendDirect(TransferDescription transferDescription,
        byte[] data, SubMonitor progress) throws IOException,
        SarosCancellationException {

        if (!isConnected())
            throw new IOException("BinaryChannel is closed");

        int countData = Math.max(0, data.length / CHUNKSIZE) + 1;
        progress.beginTask("send direct", countData + 1);

        int objectid = nextObjectId();
        transferDescription.objectid = objectid;

        byte[] descData = null;
        descData = Utils.deflate(transferDescription.toByteArray(),
            progress.newChild(1));
        if (descData == null) {
            log.error(
                "Failed to deflate bytes of a Base64 encoded TransferDescription String:"
                    + transferDescription.toBase64(), new StackTrace());
            throw new IOException();
        }

        try {
            int countDescription = Math.max(0, descData.length / CHUNKSIZE) + 1;
            progress.setWorkRemaining(countData + countDescription);

            // send TRANSFERDESCRIPTION
            sendDirect(PacketType.TRANSFERDESCRIPTION, countDescription,
                objectid, descData, progress);

            if (isRejected(objectid))
                throw new RemoteCancellationException();

            // send DATA
            sendDirect(PacketType.DATA, countData, objectid, data, progress);

            BinaryPacket confirmation = null;
            try {
                while (confirmation == null && isConnected()
                    && !progress.isCanceled()) {
                    confirmation = remoteTransfers.get(objectid).poll(500,
                        TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                log.error("Code not designed to be interrupted", e);
                throw new CausedIOException(
                    "Binary Channel received unexpected exception while waiting for confirmation package",
                    e);
            }
            if (confirmation == null)
                throw new IOException(
                    "Binary Channel was closed while waiting for confirmation package");

            if (confirmation.getType() == PacketType.REJECT)
                throw new RemoteCancellationException();

            assert confirmation.getType() == PacketType.FINISHED;

        } catch (LocalCancellationException e) {

            log.debug("send was canceled:" + transferDescription.objectid);

            send(buildPacket(PacketType.CANCEL, objectid));
            throw e;

        } finally {
            remoteTransfers.remove(objectid);
        }
    }

    public static BinaryPacket buildPacket(PacketType type, int objectid) {
        return buildPacket(type, 0, objectid, ByteString.EMPTY);
    }

    protected boolean isRejected(int objectid) {
        BinaryPacket packet = remoteTransfers.get(objectid).poll();
        if (packet != null) {
            return packet.getType() == PacketType.REJECT;
        }
        return false;
    }

    /**
     * Splits the given data into chunks of CHUNKSIZE to send the BinaryPackets.
     */
    protected void sendDirect(PacketType type, int remaining, int objectid,
        byte[] data, SubMonitor progress) throws SarosCancellationException,
        IOException {

        int offset = 0;
        // splits into chunks with remaining == 0 is the last packet
        int size;
        for (int idx = remaining; idx > 0; idx--) {

            if (isRejected(objectid))
                return; /*
                         * Just return, sendDirect(byte[]) will take care of
                         * throwing an RemoteCancellationException
                         */

            if (progress.isCanceled()) {
                log.info("Sending " + objectid
                    + " was cancelled by the local user");
                throw new LocalCancellationException();
            }

            size = Math.min(data.length - offset, CHUNKSIZE);
            send(buildPacket(type, idx - 1, objectid,
                ByteString.copyFrom(data, offset, size)));
            offset += size;
            progress.worked(1);
        }
    }

    public static BinaryPacket buildPacket(PacketType type, int index,
        int objectID, ByteString data) {
        return BinaryPacket.newBuilder().setType(type).setRemaining(index)
            .setObjectid(objectID).setData(data).setSize(data.size()).build();
    }

    /**
     * The next ID to use when sending {@link BinaryPacket}s.
     * 
     * This object should only be accessed from nextObjectId()
     */
    protected int currentObjectId = 0;

    /**
     * This object is internally used by nextObjectID for synchronizing access
     * to currentObjectId;
     */
    private Object nextObjectIdLock = new Object();

    protected int nextObjectId() {
        synchronized (nextObjectIdLock) {
            if (currentObjectId >= Integer.MAX_VALUE)
                currentObjectId = 0;
            else
                currentObjectId = currentObjectId + 1;
            return currentObjectId;
        }
    }

    /**
     * See {{@link #ACCEPTED_ERROR_CODES_ON_CLOSURE}
     * 
     * @param e
     * @return whether the error should be logged
     */
    protected boolean isAcceptedOnClosure(IOException e) {
        for (String s : ACCEPTED_ERROR_CODES_ON_CLOSURE) {
            if (e.getMessage().contains(s))
                return true;
        }
        return false;
    }

    /**
     * Shutdown the entire connection represented by this BinaryChannel. It
     * closes the Socket, the ObjectInputStream and the ObjectOutputStream.
     */
    public void dispose() {
        try {
            if (session != null)
                session.close();
            if (socket != null && !socket.isClosed())
                socket.close();
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        } catch (IOException e) {
            if (!isAcceptedOnClosure(e))
                log.debug("Close failed cause: " + e.getMessage(), e);
        }
        inputStream = null;
        outputStream = null;
        socket = null;
        session = null;
    }

    /**
     * Sends a reject BinaryPacket for the given TransferDescription.
     */
    public void sendReject(TransferDescription transferDescription)
        throws IOException {
        log.debug("sendReject objectid:" + transferDescription.objectid);
        send(buildPacket(PacketType.REJECT, transferDescription.objectid));
    }

    /**
     * Returns a new BinaryPacket in which the data field has been trimmed to
     * max 30 bytes so that the BinaryPacket can be printed using toString().
     */
    public static BinaryPacket trimForLogging(BinaryPacket binaryPacket) {
        int offset = 0;
        int size = Math.min(30, binaryPacket.getData().size());
        byte[] bytes = binaryPacket.getData().toByteArray();
        ByteString data = ByteString.copyFrom(bytes, offset, size);
        return BinaryPacket.newBuilder(binaryPacket).setData(data).build();
    }
}