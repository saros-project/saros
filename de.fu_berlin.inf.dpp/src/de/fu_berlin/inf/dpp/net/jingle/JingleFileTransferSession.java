package de.fu_berlin.inf.dpp.net.jingle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;
import org.limewire.nio.NIODispatcher;
import org.limewire.rudp.DefaultRUDPContext;
import org.limewire.rudp.DefaultRUDPSettings;
import org.limewire.rudp.DefaultUDPService;
import org.limewire.rudp.RudpMessageDispatcher;
import org.limewire.rudp.UDPMultiplexor;
import org.limewire.rudp.UDPSelectorProvider;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class implements a file transfer session with jingle.
 * 
 * Jingle is a XMPP-extension with id XEP-0166. Documentation can be found at
 * http://xmpp.org/extensions/xep-0166.html .
 * 
 * This implementation uses TCP as transport protocol which fall back to UDP
 * when a TCP connection failed. To ensure no data loss when transmitting with
 * UDP the RUDP implementation from the Limewire project are used.
 * 
 * Documentation for the RUDP component from limewire can be found at:
 * http://wiki.limewire.org/index.php?title=Javadocs .
 * 
 * @author chjacob
 * @author oezbek
 */
public class JingleFileTransferSession extends JingleMediaSession {

    private static final Logger log = Logger
        .getLogger(JingleFileTransferSession.class.getName());

    public static final int TIMEOUTSECONDS = 15;

    private class ReceiverThread extends Thread {

        private ObjectInputStream input;

        public ReceiverThread(ObjectInputStream ii) {
            this.input = ii;
        }

        /**
         * This executor is used to decouple the reading from the
         * ObjectInputStream and the notification of the listeners. Thus we can
         * continue reading, even while the DataTransferManager is handling our
         * data.
         */
        ExecutorService dispatch = Executors
            .newSingleThreadExecutor(new NamedThreadFactory(
                "JingleFileTransferSession-Dispatch-"));

        /**
         * @review runSafe OK
         */
        @Override
        public void run() {

            try {
                while (!isInterrupted()) {
                    final TransferDescription data;
                    try {
                        data = (TransferDescription) input.readUnshared();
                    } catch (IOException e) {
                        log.error(prefix() + "Crashed", e);
                        return;
                    } catch (ClassNotFoundException e) {
                        log.error(prefix()
                            + "Received unexpected object in ReceiveThread", e);
                        continue;
                    }

                    dispatch.submit(Util.wrapSafe(log, new Runnable() {
                        public void run() {
                            for (IJingleFileTransferListener listener : listeners) {
                                listener.incomingDescription(data,
                                    connectionType);
                            }
                        }
                    }));

                    // Read incoming chunks of data
                    final byte[] content;
                    try {
                        int n = input.readInt();

                        content = new byte[n];

                        int count = input.readInt();
                        int pos = 0;

                        while (count > 0) {
                            input.readFully(content, pos, count);
                            pos += count;

                            count = input.readInt();
                        }

                        if (count == -1)
                            // canceled
                            continue;

                    } catch (IOException e) {
                        log.error(prefix() + "Crashed", e);
                        for (IJingleFileTransferListener listener : listeners) {
                            listener.transferFailed(data, connectionType);
                        }
                        return;
                    }

                    dispatch.submit(Util.wrapSafe(log, new Runnable() {
                        public void run() {
                            for (IJingleFileTransferListener listener : listeners) {
                                listener.incomingData(data,
                                    new ByteArrayInputStream(content),
                                    connectionType);
                            }
                        }
                    }));
                }
            } catch (RuntimeException e) {
                log.error(prefix() + "Internal Error in Receive Thread: ", e);
                // If there is programming problem, close the socket
                close();
            }
        }
    }

    protected String prefix() {
        return "Jingle " + Util.prefix(connectTo);
    }

    private ReceiverThread receiveThread;
    private Set<IJingleFileTransferListener> listeners;
    private UDPSelectorProvider udpSelectorProvider;

    private NetTransferMode connectionType = NetTransferMode.UNKNOWN;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    private String remoteIp;
    private String localIp;
    private int localPort;
    private int remotePort;

    private JID connectTo;

    /**
     * Create AND initialize a new JingleFileTransferSession. This includes
     * connecting to given remote side (if initiator).
     * 
     * @param payloadType
     *            Our PayloadType, which identifies which packets we handle.
     *            Smack takes care of this.
     * @param remote
     *            The remote IP and port suggested by Jingle to us.
     * @param local
     *            Our own IP and port suggested by Jingle to us.
     * @param mediaLocator
     *            MediaLocator to pass to super()
     * @param jingleSession
     *            may be null, the existing JingleSession which we are a part
     *            of.
     * @param listeners
     *            We will notify these listeners if we receive files from the
     *            remote side.
     * @param connectTo
     *            The JID of the user we are connecting to via this transfer
     *            session.
     */
    public JingleFileTransferSession(PayloadType payloadType,
        TransportCandidate remote, TransportCandidate local,
        String mediaLocator, JingleSession jingleSession,
        Set<IJingleFileTransferListener> listeners, JID connectTo) {
        super(payloadType, remote, local, mediaLocator, jingleSession);

        this.connectTo = connectTo;
        this.listeners = listeners;
    }

    /**
     * Initialization of the session. It tries to create sockets for both, TCP
     * and UDP. The UDP Socket is a reliable implementation from the Limewire
     * project. Documentation can be found at http://wiki.limewire.org.
     */
    @Override
    public void initialize() {

        if (this.getLocal().getSymmetric() != null) {

            // A Symmetric connection is one where a RTPBridge is used as a
            // relay

            localIp = this.getLocal().getLocalIp();
            localPort = Util.getFreePort();

            // Since we want to establish a TCP Connection, doing the relay
            // would be impractical anyway
            remoteIp = this.getLocal().getIp();
            remotePort = this.getLocal().getSymmetric().getPort();

            log.warn("Symmetric Connection using RTPBridge is not supported!!"
                + " Attempting anyway: Jingle [" + connectTo.getName()
                + "] Symmetric IPs - local: " + localIp + ":" + localPort
                + " -> remote: " + remoteIp + ":" + remotePort);

        } else {
            localIp = this.getLocal().getLocalIp();
            localPort = this.getLocal().getPort();

            remoteIp = this.getRemote().getIp();
            remotePort = this.getRemote().getPort();

            log.info("Jingle [" + connectTo.getName()
                + "] Not Symmetric IPs - local: " + localIp + ":" + localPort
                + " <-> remote: " + remoteIp + ":" + remotePort);
        }

        // create RUDP service
        RudpMessageDispatcher dispatcher = new RudpMessageDispatcher();
        DefaultUDPService service = new DefaultUDPService(dispatcher);
        RUDPMessageFactory factory = new DefaultMessageFactory();
        udpSelectorProvider = new UDPSelectorProvider(new DefaultRUDPContext(
            factory, NIODispatcher.instance().getTransportListener(), service,
            new DefaultRUDPSettings()));
        UDPMultiplexor udpMultiplexor = udpSelectorProvider.openSelector();
        dispatcher.setUDPMultiplexor(udpMultiplexor);
        NIODispatcher.instance().registerSelector(udpMultiplexor,
            udpSelectorProvider.getUDPSocketChannelClass());
        try {
            service.start(localPort);
        } catch (IOException e) {
            log.error("Jingle [" + connectTo.getName()
                + "] Failed to create RUDP service");
        }

        if (getJingleSession().getInitiator().equals(
            getJingleSession().getConnection().getUser())) {

            initializeAsServer();
        } else {
            initializeAsClient();
        }
    }

    protected void initializeAsClient() {

        ArrayList<SocketCreator> creators = new ArrayList<SocketCreator>(2);

        creators.add(SocketCreator.getWrapped(NetTransferMode.JINGLETCP, Util
            .retryEvery500ms(new Callable<Socket>() {
                public Socket call() throws Exception {
                    return new Socket(remoteIp, remotePort);
                }
            })));

        creators.add(SocketCreator.getWrapped(NetTransferMode.JINGLEUDP, Util
            .delay(7500, Util.retryEvery500ms(new Callable<Socket>() {
                public Socket call() throws Exception {

                    Socket usock = udpSelectorProvider.openSocketChannel()
                        .socket();
                    usock.setSoTimeout(0);
                    usock.setKeepAlive(true);
                    usock.connect(new InetSocketAddress(InetAddress
                        .getByName(remoteIp), remotePort));
                    return usock;
                }
            }))));

        connect(creators);
    }

    protected void initializeAsServer() {

        ArrayList<SocketCreator> creators = new ArrayList<SocketCreator>(2);

        creators.add(new SocketCreator(NetTransferMode.JINGLETCP) {

            public Socket call() throws Exception {

                ServerSocket serverSocket = new ServerSocket(localPort);
                serverSocket.setSoTimeout(30000);

                return serverSocket.accept();
            }
        });

        creators.add(new SocketCreator(NetTransferMode.JINGLEUDP) {

            public Socket call() throws Exception {
                Socket usock = udpSelectorProvider.openSocketChannel().socket();
                usock.setSoTimeout(0);
                usock.connect(new InetSocketAddress(InetAddress
                    .getByName(remoteIp), remotePort));
                usock.setKeepAlive(true);

                return usock;
            }
        });

        connect(creators);
    }

    abstract static class SocketCreator implements Callable<Socket> {

        SocketCreator(NetTransferMode type) {
            this.type = type;
        }

        NetTransferMode type;

        public NetTransferMode getType() {
            return this.type;
        }

        public static SocketCreator getWrapped(NetTransferMode type,
            final Callable<Socket> callable) {
            return new SocketCreator(type) {
                public Socket call() throws Exception {
                    return callable.call();
                }
            };
        }
    }

    private void connect(Collection<SocketCreator> connects) {

        ExecutorCompletionService<Socket> completionService = new ExecutorCompletionService<Socket>(
            Executors.newFixedThreadPool(connects.size(),
                new NamedThreadFactory("Jingle-Connect-" + connectTo.getName()
                    + "-")));

        Map<Future<Socket>, SocketCreator> futures = new HashMap<Future<Socket>, SocketCreator>();

        for (SocketCreator creator : connects) {
            futures.put(completionService.submit(creator), creator);
        }

        for (int i = 0; i < connects.size(); i++) {

            Future<Socket> socketFuture = null;
            try {
                socketFuture = completionService.poll(TIMEOUTSECONDS,
                    TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            }

            if (socketFuture == null) {
                log.debug("Jingle [" + connectTo.getName()
                    + "] Could not connect with either TCP or UDP.");
                break;
            }

            try {
                this.socket = socketFuture.get();
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                log.debug("Jingle [" + connectTo.getName()
                    + "] Could not connect with either TCP or UDP.");
                continue;
            }

            try {
                this.objectOutputStream = new ObjectOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
                this.objectOutputStream.flush();
                this.objectInputStream = new ObjectInputStream(
                    new BufferedInputStream(socket.getInputStream()));

                this.receiveThread = new ReceiverThread(objectInputStream);
                this.receiveThread.start();

                this.connectionType = futures.get(socketFuture).getType();

                // Make sure the other connect-futures are canceled
                for (Future<Socket> future : futures.keySet()) {
                    future.cancel(true);
                }

                return;
            } catch (IOException e) {
                log.debug("Jingle [" + connectTo.getName() + "] "
                    + "Failed to listen with either TCP or UDP.", e);

                close();
            }
        }

        // Timeout, so cancel all
        for (Future<Socket> future : futures.keySet()) {
            future.cancel(true);
        }

        // Failed to connect...
        assert objectOutputStream == null && objectInputStream == null;
    }

    /**
     * This field is used by the send method to reset the given object stream
     * ever now and then.
     */
    private int resetCounter = 0;

    /**
     * Constant sent after the last chunk to indicate the end of the single file.
     */
    protected static final int DONE = 0;

    /** 
     * Constant indicating that the user has canceled the transfer. The peer will discard the file.
     */
    protected static final int CANCELED = -1;

    /**
     * Chunksize used for splitting the data to send into packets in between of which the sending of the file can be canceled.
     */
    protected static final int CHUNKSIZE = 32000;

    /**
     * This method is called from the JingleFileTransferManager to send files
     * with this session.
     * 
     * @throws JingleSessionException
     *             if sending failed.
     */
    public synchronized NetTransferMode send(TransferDescription transferData,
        byte[] content, SubMonitor progress) throws JingleSessionException {

        if (objectOutputStream == null) {
            throw new JingleSessionException("Failed to send files with Jingle");
        }

        progress.setWorkRemaining(content.length);
        try {
            long startTime = System.currentTimeMillis();
            objectOutputStream.writeUnshared(transferData);
            // Prevent caching of byte[] data in the handle table of the OOS

            // Write Size:
            objectOutputStream.writeInt(content.length);

            int i = 0;
            while (i < content.length) {
                if (progress.isCanceled()) {
                    objectOutputStream.writeInt(CANCELED);
                    objectOutputStream.flush();
                    throw new CancellationException();
                }

                int count = Math.min(CHUNKSIZE, content.length - i);
                objectOutputStream.writeInt(count);
                objectOutputStream.write(content, i, count);
                progress.worked(count);
                i += count;
            }
            objectOutputStream.writeInt(DONE);
            objectOutputStream.flush();

            if (resetCounter++ > 128) {
                // Reset periodically to flush stream handles to the data
                objectOutputStream.reset();
                resetCounter = 0;
            }

            long endTime = System.currentTimeMillis();
            long delta = endTime - startTime;
            String throughput;
            if (delta == 0) {
                throughput = "";
            } else {
                throughput = " (" + (content.length / 1024) + "kb in " + delta
                    + " ms at " + (1000 * content.length / 1024) / delta
                    + " kb/s)";
            }

            log.debug(prefix() + "Sent" + throughput + ": " + transferData);
            return connectionType;
        } catch (IOException e) {
            throw new JingleSessionException(prefix() + "Failed to send files");
        } finally {
            progress.done();
        }
    }

    /**
     * This method is called from Jingle AFTER a jingle session is established.
     * We thus could start sending here, but we want that others call us using
     * send.
     */
    @Override
    public void startTrasmit() {
        // Do nothing -> Users should call send(...) directly
    }

    @Override
    public void stopTrasmit() {
        // Do nothing -> Users should call send(...) directly
    }

    @Override
    public void setTrasmit(boolean active) {
        log.error("Unexpected call to setTrasmit(active ==" + active + ")");
    }

    /**
     * This method is called from Jingle AFTER a jingle session is established.
     * Since we want others to call us, we need to be ready for transmitting
     * before this
     */
    @Override
    public void startReceive() {
        // do nothing.
    }

    @Override
    public void stopReceive() {
        close();
    }

    public synchronized void close() {

        if (receiveThread != null)
            receiveThread.interrupt();

        Util.close(socket);
        Util.close(objectInputStream);
        Util.close(objectOutputStream);

        objectInputStream = null;
        objectOutputStream = null;
        socket = null;

        connectionType = null;
    }

    public synchronized boolean isConnected() {
        return objectInputStream != null && objectOutputStream != null;
    }

    public NetTransferMode getConnectionType() {
        return connectionType;
    }
}
