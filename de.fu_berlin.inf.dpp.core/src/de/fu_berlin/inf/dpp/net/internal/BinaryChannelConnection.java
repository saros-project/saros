package de.fu_berlin.inf.dpp.net.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

/**
 * BinaryChannelConnection is a class that encapsulates a bidirectional
 * communication channel between two participants.
 * 
 * The threading requirements of this class are the following:
 * 
 * send() is a reentrant method for sending data. Any number of threads can call
 * it in parallel. </p> <b>Note:</b> The maximum number of concurrent threads is
 * 32 !
 * 
 * 
 * @author sszuecs
 * @author coezbek
 * @author srossbach
 */
public class BinaryChannelConnection implements IByteStreamConnection {

    private static final Logger log = Logger
        .getLogger(BinaryChannelConnection.class);

    private static final long TERMINATE_TIMEOUT = 10000L;

    private static class Opcode {
        /* these opcodes will be cropped to byte values, do not exceed 0xFF ! */

        private static final int TRANSFERDESCRIPTION = 0xFA;
        private static final int DATA = 0xFB;
    }

    /**
     * Max size of data chunks
     */
    private static final int CHUNKSIZE = 32 * 1024 - 1;

    private IByteStreamConnectionListener listener;
    private ReceiverThread receiveThread;

    private final JID peer;

    private final String connectionID;

    private IDPool idPool = new IDPool();

    private boolean connected;
    private boolean initialized;

    private Map<Integer, ByteArrayOutputStream> pendingFragmentedPackets = new HashMap<Integer, ByteArrayOutputStream>();
    private Map<Integer, BinaryXMPPExtension> pendingXMPPExtensions = new HashMap<Integer, BinaryXMPPExtension>();

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private BytestreamSession session;

    /**
     * NetTransferMode to identify the transport method of the underlying socket
     * connection.
     */
    private NetTransferMode transferMode;

    private class ReceiverThread extends Thread {

        @Override
        public void run() {
            String connection = BinaryChannelConnection.this.toString();

            log.debug(connection + " ReceiverThread started.");
            try {
                while (!isInterrupted())
                    listener.receive(readNextXMPPExtension());

            } catch (SocketException e) {
                log.debug(connection + " connection closed locally: "
                    + e.getMessage());
            } catch (EOFException e) {
                log.debug(connection + " connection closed remotely:"
                    + e.getMessage());
            } catch (IOException e) {
                log.error(connection + " network error: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error(connection + " internal error: " + e.getMessage(), e);
            } finally {
                close();
            }
        }
    }

    public BinaryChannelConnection(JID peer, String connectionID,
        BytestreamSession session, NetTransferMode mode,
        IByteStreamConnectionListener listener) throws IOException {
        this.listener = listener;
        this.peer = peer;
        this.connectionID = connectionID;
        this.session = session;
        this.session.setReadTimeout(0); // keep connection alive
        this.transferMode = mode;

        outputStream = new DataOutputStream(new BufferedOutputStream(
            session.getOutputStream()));
        inputStream = new DataInputStream(new BufferedInputStream(
            session.getInputStream()));
    }

    @Override
    public synchronized void initialize() {
        if (initialized)
            return;

        /*
         * it is ok to start the receiver a bit later because the data will be
         * already buffered by SMACK or the OS
         */
        receiveThread = new ReceiverThread();
        receiveThread.setName("BinaryChannel-" + peer.getName());
        receiveThread.start();
        connected = true;
        initialized = true;
    }

    @Override
    public String getConnectionID() {
        return connectionID;
    }

    @Override
    public synchronized boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        synchronized (this) {

            if (!isConnected())
                return;

            try {
                session.close();
            } catch (Exception e) {
                log.error("failed to gracefully close connection " + this, e);
            } finally {
                connected = false;
            }
        }

        assert receiveThread != null;

        if (Thread.currentThread() != receiveThread) {
            try {
                receiveThread.join(TERMINATE_TIMEOUT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (receiveThread.isAlive()) {
                log.warn("timeout while waiting for closure of binary channel "
                    + this);
                receiveThread.interrupt();
            }
        }

        listener.connectionClosed(connectionID, getPeer(), this);
    }

    @Override
    public NetTransferMode getMode() {
        return transferMode;
    }

    @Override
    public JID getPeer() {
        return peer;
    }

    @Override
    public void send(TransferDescription data, byte[] content)
        throws IOException {

        if (!isConnected())
            throw new EOFException("connection is closed");

        final int fragmentId = idPool.nextID();

        if (fragmentId < 0)
            throw new IOException("concurrent access threshold exeeded");

        try {

            byte[] descData = TransferDescription.toByteArray(data);

            assert content.length > 0;

            int chunks = ((content.length - 1) / CHUNKSIZE) + 1;

            sendTransferDescription(descData, fragmentId, chunks);

            splitAndSend(content, chunks, fragmentId);
        } catch (IOException e) {
            close();
            throw e;
        } finally {
            idPool.freeID(fragmentId);
        }
    }

    /**
     * Reads the next XMPP extension.
     * 
     * @return returns the next incoming transfer object
     * 
     * @throws IOException
     *             If the associated socket broke, while reading or if the
     *             socket has already been disposed.
     */
    private BinaryXMPPExtension readNextXMPPExtension() throws IOException {

        while (!Thread.currentThread().isInterrupted()) {

            int opcode = inputStream.read();

            if (opcode == -1)
                throw new EOFException("no stream data available");

            int fragmentId;

            int payloadLength;

            switch (opcode) {
            case Opcode.TRANSFERDESCRIPTION:
                fragmentId = inputStream.readShort();
                int chunks = inputStream.readInt();
                payloadLength = inputStream.readInt();

                if (log.isTraceEnabled()) {
                    log.trace("processing opcode 0x"
                        + Integer.toHexString(opcode).toUpperCase()
                        + " [TFD]: id=" + fragmentId + ", chunks=" + chunks
                        + ", TFD len=" + payloadLength + " bytes");
                }

                if (payloadLength <= 0 || payloadLength > CHUNKSIZE)
                    throw new ProtocolException(
                        "payload length field contains corrupted value: 0 < "
                            + payloadLength + " <= " + CHUNKSIZE);

                byte[] transferDescriptionData = new byte[payloadLength];
                inputStream.readFully(transferDescriptionData);

                TransferDescription transferDescription = TransferDescription
                    .fromByteArray(transferDescriptionData);

                BinaryXMPPExtension oldTransferObject = pendingXMPPExtensions
                    .put(fragmentId, new BinaryXMPPExtension(transferMode,
                        transferDescription, chunks));

                if (oldTransferObject != null)
                    throw new IOException(
                        "replaced a XMPP extension that is still transmitted");
                break;

            case Opcode.DATA:
                fragmentId = inputStream.readShort();
                payloadLength = inputStream.readInt();

                if (log.isTraceEnabled()) {
                    log.trace("processing opcode 0x"
                        + Integer.toHexString(opcode).toUpperCase()
                        + " [DATA]: id=" + fragmentId + ", DATA len="
                        + payloadLength + " bytes");
                }

                if (payloadLength <= 0 || payloadLength > CHUNKSIZE)
                    throw new ProtocolException(
                        "payload length field contains corrupted value: 0 < "
                            + payloadLength + " <= " + CHUNKSIZE);

                byte[] payload = new byte[payloadLength];
                inputStream.readFully(payload);

                ByteArrayOutputStream out = pendingFragmentedPackets
                    .get(fragmentId);

                if (out == null) {
                    out = new ByteArrayOutputStream(payloadLength * 2);
                    pendingFragmentedPackets.put(fragmentId, out);
                }

                out.write(payload);
                out.flush();

                if (!pendingXMPPExtensions.get(fragmentId).isLastChunk())
                    break;

                pendingFragmentedPackets.remove(fragmentId);

                BinaryXMPPExtension fullyReceivedTransferObject = pendingXMPPExtensions
                    .remove(fragmentId);

                payload = out.toByteArray();
                out = null; // help GC

                fullyReceivedTransferObject.setPayload(payload.length, payload);

                return fullyReceivedTransferObject;
            default:
                close();
                throw new ProtocolException("unknown opcode: 0x"
                    + Integer.toHexString(opcode).toUpperCase());
            }
        }

        // clear the interrupt flag
        Thread.interrupted();
        throw new InterruptedIOException(
            "interrupted while reading stream data");
    }

    private synchronized void sendData(int fragmentId, byte[] data, int offset,
        int length) throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("sending data: id=" + fragmentId + ", len=" + length
                + " bytes");
        }

        outputStream.write(Opcode.DATA);
        outputStream.writeShort(fragmentId);
        outputStream.writeInt(length);
        outputStream.write(data, offset, length);
        outputStream.flush();
    }

    private synchronized void sendTransferDescription(byte[] description,
        int fragmentId, int chunks) throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("sending transfer description: id=" + fragmentId
                + ", len=" + description.length + " bytes");
        }

        outputStream.write(Opcode.TRANSFERDESCRIPTION);
        outputStream.writeShort(fragmentId);
        outputStream.writeInt(chunks);
        outputStream.writeInt(description.length);
        outputStream.write(description);
        outputStream.flush();
    }

    /**
     * Splits the given data into chunks of CHUNKSIZE to send the BinaryPackets.
     */
    private void splitAndSend(byte[] data, int chunks, int fragmentId)
        throws IOException {

        int offset = 0;
        int length = 0;

        while (chunks-- > 0) {

            length = Math.min(data.length - offset, CHUNKSIZE);

            sendData(fragmentId, data, offset, length);

            offset += length;
        }
    }

    @Override
    public String toString() {
        return getMode().toString() + " " + peer;
    }

    static class IDPool {

        private final int MAX_ID = 32;
        private int pool = 0; // 32 ids

        // see
        // http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogDeBruijn
        private final static int LOG_2_TABLE[] = { 0, 1, 28, 2, 29, 14, 24, 3,
            30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
            12, 18, 6, 11, 5, 10, 9 };

        public synchronized int nextID() {

            final int bitIdx = Integer.lowestOneBit(~pool);

            if (bitIdx == 0)
                return -1;

            pool |= bitIdx;

            return LOG_2_TABLE[(bitIdx * 0x077CB531) >>> 27];
        }

        public synchronized void freeID(int id) {
            if (id < 0 || id >= MAX_ID)
                return;

            int bitIdx = 1 << id;
            pool &= (~bitIdx);
        }
    }
}
