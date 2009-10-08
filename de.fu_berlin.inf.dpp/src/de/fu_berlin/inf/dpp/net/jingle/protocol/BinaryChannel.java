package de.fu_berlin.inf.dpp.net.jingle.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.protocol.BinaryHeader.BinaryHeaderType;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * BinaryChannel is a class that encapsulates a bidirectional communication
 * channel between two participants.
 * 
 * @author sszuecs
 * 
 */
public class BinaryChannel {

    protected final class BinaryChannelTransferObject implements
        IncomingTransferObject {

        protected final TransferDescription transferDescription;

        protected final int objectid;

        protected AtomicBoolean acceptedOrRejected = new AtomicBoolean(false);

        protected BinaryChannelTransferObject(
            TransferDescription transferDescription, int objectid) {

            this.transferDescription = transferDescription;
            this.objectid = objectid;
        }

        public byte[] accept(SubMonitor progress)
            throws SarosCancellationException, IOException {

            try {

                if (!acceptedOrRejected.compareAndSet(false, true))
                    throw new IllegalStateException(
                        "This IncomingTransferObject has already"
                            + " been accepted or rejected");

                BlockingQueue<BinaryPacket> myPackets = incomingPackets
                    .get(objectid);

                boolean first = true;

                LinkedList<BinaryPacket> resultList = new LinkedList<BinaryPacket>();

                while (true) {
                    if (progress.isCanceled()) {
                        reject();
                        throw new LocalCancellationException();
                    }

                    BinaryPacket packet;
                    try {
                        packet = myPackets.take();
                    } catch (InterruptedException e) {
                        log.error("Code not designed to be interrupted");
                        Thread.currentThread().interrupt();
                        return null;
                    }

                    if (packet.isCancel()) {
                        assert packet.getObjectID() == objectid;

                        throw new RemoteCancellationException();
                    }

                    if (first) {
                        progress.beginTask("Receiving", packet.head.remaining
                            + (transferDescription
                                .compressInDataTransferManager() ? 1 : 0));
                        first = false;
                    }

                    resultList.add(packet);
                    progress.worked(1);
                    if (packet.isLast())
                        break;
                }

                send(BinaryPacket.create(objectid, BinaryHeaderType.FINISHED));
                byte[] data = getData(resultList);

                if (transferDescription.compressInDataTransferManager())
                    data = Util.inflate(data, progress.newChild(1));

                return data;
            } finally {
                incomingPackets.remove(objectid);
            }
        }

        public Packet getPacket() {
            Packet packet = new Message();
            packet.setPacketID(Packet.ID_NOT_AVAILABLE);
            packet.addExtension(packetProvider.create(this));
            return packet;
        }

        public TransferDescription getTransferDescription() {
            return transferDescription;
        }

        public void reject() throws IOException {
            if (!acceptedOrRejected.compareAndSet(false, true))
                throw new IllegalStateException(
                    "This IncomingTransferObject has already"
                        + " been accepted or rejected");

            send(BinaryPacket.create(objectid, BinaryHeaderType.REJECT));
        }

        public NetTransferMode getTransferMode() {
            return transferMode;
        }
    }

    private static final Logger log = Logger.getLogger(BinaryChannel.class);

    /**
     * Max size of data chunks
     */
    public static final int CHUNKSIZE = 65535;

    /**
     * bound for resetting the objectOutputStream
     * 
     * This is private because it is used exclusively by
     * {@link #send(BinaryPacket)}
     */
    private static final int RESETBOUND = 128;

    /**
     * count the objects send through the objectOutputStream
     * 
     * This is private because it is used exclusively by
     * {@link #send(BinaryPacket)}
     */
    private int resetCounter = 0;

    /**
     * Collect the Packets until an entire Object is received. objectid -->
     * [Packet0, Packet1, ..]
     */
    protected AutoHashMap<Integer, BlockingQueue<BinaryPacket>> incomingPackets = AutoHashMap
        .getBlockingQueueHashMap();

    protected AutoHashMap<Integer, List<BinaryPacket>> incomingDescriptionPackets = AutoHashMap
        .getListHashMap();

    protected Map<Integer, BlockingQueue<BinaryPacket>> intermediate = AutoHashMap
        .getBlockingQueueHashMap();
    protected Map<Integer, BlockingQueue<BinaryPacket>> remoteTransfers = Collections
        .synchronizedMap(intermediate);

    // network stuff
    protected Socket socket;
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;

    /**
     * The next ID to use when sending {@link BinaryPacket}s
     */
    protected int currentObjectId = 0;

    protected XStreamExtensionProvider<IncomingTransferObject> packetProvider;

    /**
     * NetTransferMode to identify the transport method of the underlying socket
     * connection.
     */
    protected NetTransferMode transferMode;

    /**
     * Returns the byte Array for the given List of BinaryPacket.
     */
    public static byte[] getData(List<BinaryPacket> list) {
        ByteBuffer buf = ByteBuffer.allocate(list.size() * CHUNKSIZE);
        for (BinaryPacket packet : list)
            buf.put(packet.body);
        return buf.array();
    }

    /**
     * Creates an observer BinaryChannel.
     * 
     * @throws IOException
     */
    public BinaryChannel(Socket socket,
        XStreamExtensionProvider<IncomingTransferObject> packetProvider,
        NetTransferMode transferMode) throws IOException {
        this.socket = socket;
        this.packetProvider = packetProvider;
        this.transferMode = transferMode;

        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.flush();
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public IncomingTransferObject receiveIncomingTransferObject(
        SubMonitor progress) throws SarosCancellationException, IOException,
        ClassNotFoundException {

        try {
            while (true) {

                if (progress.isCanceled())
                    throw new LocalCancellationException();

                BinaryPacket packet = receiveInternal();
                if (packet == null)
                    continue;

                if (log.isTraceEnabled())
                    log.trace("Incoming packet: " + packet);

                final int objectid = packet.getObjectID();

                switch (packet.getType()) {
                case TRANSFERDESCRIPTION:
                    incomingDescriptionPackets.get(objectid).add(packet);

                    if (packet.head.isLast()) {
                        List<BinaryPacket> list = incomingDescriptionPackets
                            .remove(objectid);

                        byte[] data = getData(list);

                        final TransferDescription transferDescription = buildTransferDescription(
                            objectid, data, progress);

                        return new BinaryChannelTransferObject(
                            transferDescription, objectid);
                    }
                    break;
                case DATA:
                    incomingPackets.get(objectid).add(packet);
                    break;
                case CANCEL:
                    incomingDescriptionPackets.remove(objectid);
                    incomingPackets.get(objectid).add(packet);
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
    protected synchronized void send(BinaryPacket packet) throws IOException {

        if (log.isTraceEnabled())
            log.trace("send packet " + packet.head.toString());

        objectOutputStream.writeUnshared(packet);
        objectOutputStream.flush();

        if (resetCounter++ > RESETBOUND) {
            // Reset periodically to flush stream handles to the data
            objectOutputStream.reset();
            resetCounter = 0;
        }
    }

    /**
     * This is the whole process for receiving the next packet from the socket.
     * It returns the next BinaryPacket.
     * 
     * @blocking
     */
    protected BinaryPacket receiveInternal() throws IOException,
        ClassNotFoundException {

        BinaryPacket packet = null;
        synchronized (objectInputStream) {
            packet = (BinaryPacket) objectInputStream.readUnshared();
        }
        log.debug("packet received: " + packet);

        if (packet == null || packet.head == null) {
            log.error("packet or head is null");
            return null;
        }

        return packet;
    }

    protected TransferDescription buildTransferDescription(int objectid,
        byte[] description, SubMonitor progress) throws IOException,
        ClassNotFoundException {

        TransferDescription transferDescription = TransferDescription
            .fromByteArray(Util.inflate(description, progress.newChild(1)));
        transferDescription.objectid = objectid;
        return transferDescription;
    }

    public boolean isConnected() {
        return objectInputStream != null && objectOutputStream != null;
    }

    /**
     * It sends the given transferDescription and data direct. Supports
     * cancellation by given SubMonitor.
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

        int countData = Math.max(0, data.length / CHUNKSIZE) + 1;
        progress.beginTask("send direct", countData + 1);

        int objectid = nextObjectId();
        transferDescription.objectid = objectid;

        byte[] descData = null;
        descData = Util.deflate(transferDescription.toByteArray(), progress
            .newChild(1));
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
            sendDirect(BinaryHeaderType.TRANSFERDESCRIPTION, countDescription,
                objectid, descData, progress);

            if (isRejected(objectid))
                throw new RemoteCancellationException();

            // send DATA
            sendDirect(BinaryHeaderType.DATA, countData, objectid, data,
                progress);

            BinaryPacket confirmation;
            try {
                confirmation = remoteTransfers.get(objectid).take();
            } catch (InterruptedException e) {
                log.error("Code not designed to be interrupted",
                    new StackTrace());
                return;
            }
            if (confirmation.isReject())
                throw new RemoteCancellationException();

            assert confirmation.getType() == BinaryHeaderType.FINISHED;

        } catch (LocalCancellationException e) {

            log.debug("send was canceled:" + transferDescription.objectid);

            send(BinaryPacket.create(objectid, BinaryHeaderType.CANCEL));
            throw e;

        } finally {
            remoteTransfers.remove(objectid);
            progress.done();
        }
    }

    private boolean isRejected(int objectid) {
        BinaryPacket packet = remoteTransfers.get(objectid).poll();
        if (packet != null) {
            return packet.isReject();
        }
        return false;
    }

    /**
     * Splits the given data into chunks of CHUNKSIZE to send the BinaryPackets.
     */
    protected void sendDirect(BinaryHeaderType type, int remaining,
        int objectid, byte[] data, SubMonitor progress)
        throws SarosCancellationException, IOException {

        int offset = 0;
        // splits into chunks with BinaryHeader.remaining=0 is the last packet
        int size = Math.min(data.length - offset, CHUNKSIZE);
        for (int idx = remaining; size > 0 && idx > 0; idx--) {

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
            ByteArrayOutputStream bos = new ByteArrayOutputStream(size + 1);
            bos.write(data, offset, size);
            offset += size;
            byte[] chunk = bos.toByteArray();
            BinaryHeader dataHeader = new BinaryHeader(type, idx, chunk.length,
                objectid);

            log.debug("Sending: " + dataHeader.toString());

            send(new BinaryPacket(dataHeader, chunk));
            progress.worked(1);
        }
    }

    protected synchronized int nextObjectId() {
        if (currentObjectId >= Integer.MAX_VALUE)
            currentObjectId = 0;
        else
            currentObjectId++;

        return currentObjectId;
    }

    /**
     * Shutdown the entire connection represented by this BinaryChannel. It
     * closes the Socket, the ObjectInputStream and the ObjectOutputStream.
     */
    public void dispose() {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
            IOUtils.closeQuietly(objectInputStream);
            IOUtils.closeQuietly(objectOutputStream);
        } catch (IOException e) {
            log.error("Close failed cause:", e);
        }
        objectInputStream = null;
        objectOutputStream = null;
        socket = null;
    }

    /**
     * Sends a reject BinaryPacket for the given TransferDescription.
     */
    public void sendReject(TransferDescription transferDescription)
        throws IOException {
        log.debug("sendReject objectid:" + transferDescription.objectid);
        send(BinaryPacket.create(transferDescription.objectid,
            BinaryHeaderType.REJECT));
    }
}