package de.fu_berlin.inf.dpp.net.jingle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
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
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.SocketConnection;
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

    protected DataTransferManager dataTransferManager;

    protected String prefix() {
        return "Jingle/UDP " + Util.prefix(connectTo);
    }

    private UDPSelectorProvider udpSelectorProvider;
    private SocketConnection socketConnection;
    private NetTransferMode connectionType = NetTransferMode.UNKNOWN;

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
     * @param dataTransferManager
     *            We will notify this dtm if we receive files from the remote
     *            side.
     * @param connectTo
     *            The JID of the user we are connecting to via this transfer
     *            session.
     */
    public JingleFileTransferSession(PayloadType payloadType,
        TransportCandidate remote, TransportCandidate local,
        String mediaLocator, JingleSession jingleSession,
        DataTransferManager dataTransferManager, JID connectTo) {
        super(payloadType, remote, local, mediaLocator, jingleSession);

        this.connectTo = connectTo;
        this.dataTransferManager = dataTransferManager;
    }

    /**
     * Initialization of the session. It tries to create sockets for both, TCP
     * and UDP. The UDP Socket is a reliable implementation from the Limewire
     * project. Documentation can be found at http://wiki.limewire.org.
     */
    @Override
    public synchronized void initialize() {

        localIp = this.getLocal().getLocalIp();
        localPort = this.getLocal().getPort();

        remoteIp = this.getRemote().getIp();
        remotePort = this.getRemote().getPort();

        log.info("Jingle [" + connectTo.getName()
            + "] Not Symmetric IPs - local: " + localIp + ":" + localPort
            + " <-> remote: " + remoteIp + ":" + remotePort);

        initializeRudp();

        if (getJingleSession().getInitiator().equals(
            getJingleSession().getConnection().getUser())) {

            initializeAsServer();
        } else {
            initializeAsClient();
        }
    }

    private void initializeRudp() {
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
    }

    private Socket createRudpSocket() throws UnknownHostException, IOException {
        Socket usock = udpSelectorProvider.openSocketChannel().socket();
        usock.setSoTimeout(0);
        usock.setKeepAlive(true);
        usock.connect(new InetSocketAddress(InetAddress.getByName(remoteIp),
            remotePort));
        return usock;
    }

    protected void initializeAsClient() {

        SocketCreator creator = SocketCreator.getWrapped(
            NetTransferMode.JINGLEUDP, Util.delay(7500, Util.retryEveryXms(
                new Callable<Socket>() {
                    public Socket call() throws Exception {

                        log.debug("Jingle/UDP connection attempt to "
                            + remoteIp + ":" + remotePort);
                        try {

                            return createRudpSocket();
                        } catch (Exception e) {
                            log
                                .debug("Jingle/UDP connection attempt FAILED to "
                                    + remoteIp
                                    + ":"
                                    + remotePort
                                    + " - "
                                    + e.getMessage());
                            throw e;
                        }
                    }
                }, 1000)));
        connect(creator);
    }

    protected void initializeAsServer() {

        SocketCreator creator = new SocketCreator(NetTransferMode.JINGLEUDP) {

            public Socket call() throws Exception {

                log.debug("Starting Jingle/UDP Server Socket on port "
                    + localPort);

                try {
                    return createRudpSocket();
                } catch (Exception e) {
                    log.warn("Jingle/UDP Server Socket on port " + localPort
                        + " failed to open: " + e.getMessage());
                    throw e;
                }
            }
        };
        connect(creator);

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

    protected void connect(SocketCreator creator) {

        ExecutorService thread = Executors
            .newSingleThreadExecutor(new NamedThreadFactory("Jingle-Connect-"
                + connectTo.getName() + "-"));

        try {
            ExecutorCompletionService<Socket> completionService = new ExecutorCompletionService<Socket>(
                thread);

            Future<Socket> future = completionService.submit(creator);

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
                    + "] Could not connect with UDP.");

            }

            try {
                Socket socket = socketFuture.get();

                this.connectionType = NetTransferMode.JINGLEUDP;
                this.socketConnection = new SocketConnection(connectTo,
                    this.connectionType, socket, dataTransferManager);

            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                log.debug("Jingle [" + connectTo.getName()
                    + "] Could not connect withUDP.");
                return;
            } catch (IOException e) {
                log.debug("Jingle " + Util.prefix(connectTo)
                    + "Failed to listen with UDP.", e);

                shutdown();
            }

            // Timeout, so cancel all

            future.cancel(true);

            // Failed to connect...
            assert !isConnected();
        } finally {
            thread.shutdown();
        }
    }

    public synchronized SocketConnection getConnection() {
        return socketConnection;
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
        if (socketConnection != null) {
            socketConnection.close();
            socketConnection = null;
        }
    }

    public synchronized boolean isConnected() {
        return socketConnection != null && socketConnection.isConnected();
    }

    public NetTransferMode getConnectionType() {
        return connectionType;
    }
}
