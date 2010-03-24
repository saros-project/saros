package de.fu_berlin.inf.dpp.net.jingle;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
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

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.protocol.BinaryChannel;
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
 * The TCP connection uses a bidirectional, Saros specific protocol.
 * 
 * @author chjacob
 * @author oezbek
 * @author sszuecs
 */
public class JingleFileTransferSession extends JingleMediaSession {

    private static final Logger log = Logger
        .getLogger(JingleFileTransferSession.class);

    public static final int TIMEOUTSECONDS = 15;

    protected IJingleFileTransferListener fileTransferListener;

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
            log.debug("Jingle ReceiverThread started.");
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

                        fileTransferListener.incomingData(transferObject);

                    } catch (SarosCancellationException e) {
                        log.info("canceled transfer");
                        if (!progress.isCanceled())
                            progress.setCanceled(true);
                    } catch (SocketException e) {
                        log.debug(prefix() + "Connection was closed by me.");
                        channel.dispose();
                        return;
                    } catch (EOFException e) {
                        log.debug(prefix() + "Connection was closed by peer.");
                        channel.dispose();
                        return;
                    } catch (IOException e) {
                        log.error(prefix() + "Crashed", e);
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
                shutdown();
            }
        }
    }

    protected String prefix() {
        String connection = "";
        if (connectionType == NetTransferMode.JINGLETCP) {
            connection = "/TCP";
        }
        if (connectionType == NetTransferMode.JINGLEUDP) {
            connection = "/UDP";
        }
        return "Jingle" + connection + " " + Util.prefix(connectTo);
    }

    private ReceiverThread receiveThread;

    private UDPSelectorProvider udpSelectorProvider;

    private NetTransferMode connectionType = NetTransferMode.UNKNOWN;

    private BinaryChannel binaryChannel;

    protected IncomingTransferObjectExtensionProvider incomingExtProv;

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
     * @param fileTransferListener
     *            We will notify this listeners if we receive files from the
     *            remote side.
     * @param connectTo
     *            The JID of the user we are connecting to via this transfer
     *            session.
     */
    public JingleFileTransferSession(PayloadType payloadType,
        TransportCandidate remote, TransportCandidate local,
        String mediaLocator, JingleSession jingleSession,
        IJingleFileTransferListener fileTransferListener, JID connectTo,
        IncomingTransferObjectExtensionProvider incomingExtProv) {
        super(payloadType, remote, local, mediaLocator, jingleSession);

        this.connectTo = connectTo;
        this.fileTransferListener = fileTransferListener;
        this.incomingExtProv = incomingExtProv;
    }

    /**
     * Initialization of the session. It tries to create sockets for both, TCP
     * and UDP. The UDP Socket is a reliable implementation from the Limewire
     * project. Documentation can be found at http://wiki.limewire.org.
     */
    @Override
    public synchronized void initialize() {

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

    protected abstract static class SocketCreator implements Callable<Socket> {

        protected SocketCreator(NetTransferMode type) {
            this.type = type;
        }

        protected NetTransferMode type;

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

    protected void connect(Collection<SocketCreator> connects) {

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
                Socket socket = socketFuture.get();
                this.connectionType = futures.get(socketFuture).getType();
                binaryChannel = new BinaryChannel(socket, this.connectionType);
                this.receiveThread = new ReceiverThread(binaryChannel);
                this.receiveThread.start();

                // Make sure the other connect-futures are canceled
                for (Future<Socket> future : futures.keySet()) {
                    future.cancel(true);
                }
                return;

            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                log.debug("Jingle [" + connectTo.getName()
                    + "] Could not connect with either TCP or UDP.");
                continue;
            } catch (IOException e) {
                log.debug("Jingle " + Util.prefix(connectTo)
                    + "Failed to listen with either TCP or UDP.", e);

                shutdown();
            }
        }

        // Timeout, so cancel all
        for (Future<Socket> future : futures.keySet()) {
            future.cancel(true);
        }

        // Failed to connect...
        assert !binaryChannel.isConnected();
    }

    /**
     * This method is called from the JingleFileTransferManager to send files
     * with this session.
     * 
     * @throws JingleSessionException
     *             if sending failed.
     * @throws IOException
     */
    public NetTransferMode send(TransferDescription transferDescription,
        byte[] content, SubMonitor progress) throws SarosCancellationException,
        JingleSessionException, IOException {

        if (progress.isCanceled())
            throw new LocalCancellationException();

        if (!binaryChannel.isConnected()) {
            throw new JingleSessionException("Failed to send files with Jingle");
        }

        binaryChannel.sendDirect(transferDescription, content, progress);

        return connectionType;
    }

    /**
     * Sends a message to reject a transfer described by the given
     * TransferDescription.
     */
    public void sendReject(TransferDescription transferDescription)
        throws IOException {
        binaryChannel.sendReject(transferDescription);
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
        shutdown();
    }

    public synchronized void shutdown() {
        binaryChannel.dispose();
        connectionType = null;
    }

    public boolean isConnected() {
        return binaryChannel.isConnected();
    }

    public NetTransferMode getConnectionType() {
        return connectionType;
    }
}
