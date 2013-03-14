package de.fu_berlin.inf.dpp.net.internal;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Encapsulates a BinaryChannel to a particular peer
 * 
 * see {#link
 * de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection}
 */
public class BinaryChannelConnection implements IByteStreamConnection {

    private static final Logger log = Logger
        .getLogger(BinaryChannelConnection.class);

    private static final long TERMINATE_TIMEOUT = 10000L;

    private IByteStreamConnectionListener listener;
    private BinaryChannel binaryChannel;
    private ReceiverThread receiveThread;

    private final JID peer;

    private class ReceiverThread extends Thread {

        private final BinaryChannel channel;

        public ReceiverThread(BinaryChannel binaryChannel) {
            channel = binaryChannel;
        }

        @Override
        public void run() {
            String connection = BinaryChannelConnection.this.toString();

            log.debug(connection + " ReceiverThread started.");
            try {
                while (!isInterrupted())
                    listener.addIncomingTransferObject(channel
                        .receiveIncomingTransferObject());

            } catch (SocketException e) {
                log.debug(connection + " connection closed locally");
            } catch (EOFException e) {
                log.debug(connection + " connection closed remotely");
            } catch (IOException e) {
                log.error(connection + " network error: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error(connection + " internal error: " + e.getMessage(), e);
            } finally {
                close();
            }
        }
    }

    public BinaryChannelConnection(JID peer, BinaryChannel channel,
        IByteStreamConnectionListener listener) {
        this.listener = listener;
        this.peer = peer;
        this.binaryChannel = channel;
        this.receiveThread = new ReceiverThread(binaryChannel);
        this.receiveThread.setName("Binary-Channel-" + peer.getName());
        this.receiveThread.start();
    }

    @Override
    public synchronized boolean isConnected() {
        return binaryChannel.isConnected();
    }

    @Override
    public void close() {
        synchronized (this) {

            if (!isConnected())
                return;

            binaryChannel.close();
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

        listener.connectionClosed(getPeer(), this);
    }

    @Override
    public NetTransferMode getMode() {
        return binaryChannel.getTransferMode();
    }

    @Override
    public JID getPeer() {
        return peer;
    }

    @Override
    public void send(TransferDescription data, byte[] content)
        throws IOException {

        try {
            binaryChannel.send(data, content);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    @Override
    public String toString() {
        return getMode().toString() + " " + Utils.prefix(peer);
    }

}
