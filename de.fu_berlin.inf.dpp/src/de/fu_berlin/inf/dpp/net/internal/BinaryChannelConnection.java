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

    private IByteStreamConnectionListener listener;
    private BinaryChannel binaryChannel;
    private ReceiverThread receiveThread;

    private JID peer;

    protected String prefix() {
        return this.getMode().toString() + " " + Utils.prefix(peer);
    }

    protected class ReceiverThread extends Thread {

        private final BinaryChannel channel;

        public ReceiverThread(BinaryChannel binaryChannel) {
            channel = binaryChannel;
        }

        @Override
        public void run() {
            log.debug(prefix() + "ReceiverThread started.");
            try {
                while (!isInterrupted())
                    listener.addIncomingTransferObject(channel
                        .receiveIncomingTransferObject());

            } catch (SocketException e) {
                log.debug(prefix() + "Connection was closed by me. "
                    + e.getMessage());
                return;
            } catch (EOFException e) {
                log.debug(prefix() + "Connection was closed by peer. "
                    + e.getMessage());
                return;
            } catch (IOException e) {
                log.error(prefix() + "Network IO Exception: " + e.getMessage(),
                    e);
                return;
            } catch (ClassNotFoundException e) {
                log.error(prefix()
                    + "Received unexpected object in ReceiveThread", e);
                return;
            } catch (Exception e) {
                log.error(prefix() + "Internal Error in Receive Thread: ", e);
                return;
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
        this.receiveThread.start();
    }

    @Override
    public synchronized boolean isConnected() {
        return binaryChannel.isConnected();
    }

    @Override
    public synchronized void close() {
        if (!isConnected())
            return;

        receiveThread.interrupt();
        listener.connectionClosed(getPeer(), this);
        binaryChannel.close();
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

}
