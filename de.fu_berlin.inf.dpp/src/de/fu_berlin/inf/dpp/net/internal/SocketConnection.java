package de.fu_berlin.inf.dpp.net.internal;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.protocol.BinaryChannel;
import de.fu_berlin.inf.dpp.util.Util;

public class SocketConnection implements IConnection {

    private static final Logger log = Logger.getLogger(SocketConnection.class);

    protected Socket socket;
    private DataTransferManager listener;
    private BinaryChannel binaryChannel;
    private ReceiverThread receiveThread;

    private NetTransferMode mode;

    private JID peer;

    protected String prefix() {
        return mode.toString() + " " + Util.prefix(peer);
    }

    protected class ReceiverThread extends Thread {

        private final BinaryChannel channel;

        public ReceiverThread(BinaryChannel binaryChannel) {
            channel = binaryChannel;
        }

        /**
         * @review runSafe OK
         */
        @Override
        public void run() {
            log.debug(prefix() + "ReceiverThread started.");
            try {
                while (!isInterrupted()) {
                    /*
                     * TODO: ask GUI if user wants to get the data. It should
                     * return a ProgressMonitor with Util#getRunnableContext(),
                     * that we can use here.
                     */
                    SubMonitor progress = SubMonitor
                        .convert(new NullProgressMonitor());
                    progress.beginTask("receive", 100);

                    try {
                        IncomingTransferObject transferObject = channel
                            .receiveIncomingTransferObject(progress.newChild(1));

                        listener.addIncomingTransferObject(transferObject);

                    } catch (SarosCancellationException e) {
                        log.info("canceled transfer");
                        if (!progress.isCanceled())
                            progress.setCanceled(true);
                    } catch (SocketException e) {
                        log.debug(prefix() + "Connection was closed by me.");
                        close();
                        return;
                    } catch (EOFException e) {
                        log.debug(prefix() + "Connection was closed by peer.");
                        close();
                        return;
                    } catch (IOException e) {
                        log.error(prefix() + "Crashed", e);
                        close();
                        return;
                    } catch (ClassNotFoundException e) {
                        log.error(prefix()
                            + "Received unexpected object in ReceiveThread", e);
                        continue;
                    }
                }
            } catch (RuntimeException e) {
                log.error(prefix() + "Internal Error in Receive Thread: ", e);
                // If there is programming problem, close the socket
                close();
            }
        }
    }

    public SocketConnection(JID peer, NetTransferMode mode, Socket socket,
        DataTransferManager listener) throws IOException {
        this.socket = socket;
        this.socket.setSoTimeout(0);
        this.listener = listener;
        this.peer = peer;
        this.mode = mode;
        this.binaryChannel = new BinaryChannel(socket, this.mode);
        this.receiveThread = new ReceiverThread(binaryChannel);
        this.receiveThread.start();
    }

    public SocketConnection(JID peer, NetTransferMode mode,
        BytestreamSession socket, DataTransferManager dtm) throws IOException {
        this.socket = null;
        this.listener = dtm;
        this.mode = mode;
        this.peer = peer;
        this.binaryChannel = new BinaryChannel(socket, this.mode);

    }

    public boolean isConnected() {
        return binaryChannel != null && binaryChannel.isConnected();
    }

    public void close() {
        if (binaryChannel != null) {
            listener.connectionClosed(getPeer(), this);
            binaryChannel.dispose();
            binaryChannel = null;
        }
    }

    public NetTransferMode getMode() {
        return mode;
    }

    public JID getPeer() {
        return peer;
    }

    public void send(TransferDescription data, byte[] content,
        SubMonitor callback) throws IOException, SarosCancellationException {

        try {
            this.binaryChannel.sendDirect(data, content, callback);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

}
