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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * BinaryChannelConnection is a class that encapsulates a bidirectional
 * communication channel between two participants.
 * 
 * The threading requirements of this class are the following:
 * 
 * send() is a reentrant method for sending data. Any number of threads can call
 * it in parallel.
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

    private AtomicInteger nextFragmentId = new AtomicInteger(0);

    private boolean connected;

    private Map<Integer, ByteArrayOutputStream> pendingFragmentedPackets = new HashMap<Integer, ByteArrayOutputStream>();
    private Map<Integer, BinaryChannelTransferObject> pendingTransferObjects = new HashMap<Integer, BinaryChannelTransferObject>();

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
                    listener
                        .addIncomingTransferObject(receiveIncomingTransferObject());

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

        connected = true;

        this.receiveThread = new ReceiverThread();
        this.receiveThread.setName("BinaryChannel-" + peer.getName());
        this.receiveThread.start();
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

        try {
            int fragmentId = nextFragmentId.getAndIncrement() & 0x7FFF;

            byte[] descData = TransferDescription.toByteArray(data);

            assert content.length > 0;

            int chunks = ((content.length - 1) / CHUNKSIZE) + 1;

            sendTransferDescription(descData, fragmentId, chunks);

            splitAndSend(content, chunks, fragmentId);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Reads the next incoming transfer object. The payload of this object may
     * not completely received at this point !
     * 
     * @return returns the next incoming transfer object
     * 
     * @throws IOException
     *             If the associated socket broke, while reading or if the
     *             socket has already been disposed.
     * @throws ClassNotFoundException
     *             If the data sent from the other side could not be decoded.
     */
    private IncomingTransferObject receiveIncomingTransferObject()
        throws IOException, ClassNotFoundException {

        while (!Thread.currentThread().isInterrupted()) {

            int opcode = inputStream.read();

            if (opcode == -1)
                throw new EOFException("no stream data available");

            int fragmentId;

            if (log.isTraceEnabled())
                log.trace("processing opcode: "
                    + Integer.toHexString(opcode).toUpperCase());

            int payloadLength;

            switch (opcode) {
            case Opcode.TRANSFERDESCRIPTION:
                fragmentId = inputStream.readShort();
                int chunks = inputStream.readInt();
                payloadLength = inputStream.readInt();

                if (payloadLength <= 0 || payloadLength > CHUNKSIZE)
                    throw new ProtocolException(
                        "payload length field contains corrupted value: 0 < "
                            + payloadLength + " <= " + CHUNKSIZE);

                byte[] transferDescriptionData = new byte[payloadLength];
                inputStream.readFully(transferDescriptionData);

                TransferDescription transferDescription = TransferDescription
                    .fromByteArray(transferDescriptionData);

                BinaryChannelTransferObject oldTransferObject = pendingTransferObjects
                    .put(fragmentId, new BinaryChannelTransferObject(
                        transferMode, transferDescription, chunks));

                if (oldTransferObject != null)
                    throw new IOException(
                        "replaced an transfer object that is still transmitted");
                break;

            case Opcode.DATA:
                fragmentId = inputStream.readShort();
                payloadLength = inputStream.readInt();

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

                if (!pendingTransferObjects.get(fragmentId).isLastChunk())
                    break;

                pendingFragmentedPackets.remove(fragmentId);

                BinaryChannelTransferObject fullyReceivedTransferObject = pendingTransferObjects
                    .remove(fragmentId);

                payload = out.toByteArray();
                payloadLength = payload.length;

                out = null; // help GC

                if (fullyReceivedTransferObject.getTransferDescription()
                    .compressContent())
                    payload = Utils.inflate(payload, null);

                fullyReceivedTransferObject.setPayload(payloadLength, payload);

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
        outputStream.write(Opcode.DATA);
        outputStream.writeShort(fragmentId);
        outputStream.writeInt(length);
        outputStream.write(data, offset, length);
        outputStream.flush();
    }

    private synchronized void sendTransferDescription(byte[] description,
        int fragmentId, int chunks) throws IOException {
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
        return getMode().toString() + " " + Utils.prefix(peer);
    }
}
