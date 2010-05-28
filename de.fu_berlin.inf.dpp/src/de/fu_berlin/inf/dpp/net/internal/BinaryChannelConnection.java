package de.fu_berlin.inf.dpp.net.internal;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Encapsulates a BinaryChannel to a particular peer
 * 
 * see {#link
 * de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection}
 */
public class BinaryChannelConnection implements IBytestreamConnection {

    private static final Logger log = Logger
        .getLogger(BinaryChannelConnection.class);

    private IBytestreamConnectionListener listener;
    private BinaryChannel binaryChannel;
    private ReceiverThread receiveThread;
    private SubMonitor progress = null;

    private JID peer;

    protected String prefix() {
        return this.getMode().toString() + " " + Util.prefix(peer);
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
                     * TODO: if we implement network view it should return a
                     * ProgressMonitor with Util#getRunnableContext(), that we
                     * can use here.
                     */
                    progress = SubMonitor.convert(new NullProgressMonitor());
                    progress.beginTask("receive", 100);

                    try {
                        IncomingTransferObject transferObject = channel
                            .receiveIncomingTransferObject(progress.newChild(1));

                        listener.addIncomingTransferObject(transferObject);

                    } catch (SarosCancellationException e) {
                        log.info("canceled transfer");
                        if (progress != null && !progress.isCanceled())
                            progress.setCanceled(true);
                        close(); // might be a local cancel
                        return;
                    } catch (SocketException e) {
                        log.debug(prefix() + "Connection was closed by me. ");
                        close();
                        return;
                    } catch (EOFException e) {
                        log.debug(prefix() + "Connection was closed by peer.");
                        close();
                        return;
                    } catch (IOException e) {
                        if (e.getMessage().contains("Socket already disposed"))
                            return;
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

    public BinaryChannelConnection(JID peer, BinaryChannel channel,
        IBytestreamConnectionListener listener) {
        this.listener = listener;
        this.peer = peer;
        this.binaryChannel = channel;
        this.receiveThread = new ReceiverThread(binaryChannel);
        this.receiveThread.start();
    }

    public synchronized boolean isConnected() {
        return binaryChannel != null && binaryChannel.isConnected();
    }

    public synchronized void close() {
        if (!isConnected())
            return;
        progress.setCanceled(true);
        listener.connectionClosed(getPeer(), this);
        binaryChannel.dispose();
        binaryChannel = null;
        progress = null;
    }

    public NetTransferMode getMode() {
        if (binaryChannel == null)
            return NetTransferMode.UNKNOWN;
        return binaryChannel.transferMode;
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
