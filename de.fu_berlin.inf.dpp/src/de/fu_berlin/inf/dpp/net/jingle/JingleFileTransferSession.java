package de.fu_berlin.inf.dpp.net.jingle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
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
public class JingleFileTransferSession extends JingleMediaSession implements
    BytestreamSession {

    private static final Logger log = Logger
        .getLogger(JingleFileTransferSession.class);

    public static final int TIMEOUTSECONDS = 30;

    protected DataTransferManager dataTransferManager;

    private UDPSelectorProvider udpSelectorProvider;

    private String remoteIp;
    private String localIp;
    private int localPort;
    private int remotePort;
    private Socket socket;
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
        this.initialize();
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

        createRUDP();

        if (isInitiator()) {
            initializeAsServer();
        } else {
            initializeAsClient();
        }
    }

    private boolean isInitiator() {
        return getJingleSession().getInitiator().equals(
            getJingleSession().getConnection().getUser());
    }

    private void createRUDP() {

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

    protected void initializeAsClient() {

        SocketCreator creator = SocketCreator.getWrapped(
            NetTransferMode.JINGLETCP, Util.retryEveryXms(
                new Callable<Socket>() {
                    public Socket call() throws Exception {
                        log.debug("Jingle/TCP connection attempt to "
                            + remoteIp + ":" + remotePort);
                        try {
                            return new Socket(remoteIp, remotePort);
                        } catch (SocketException e) {
                            log
                                .debug("Jingle/TCP connection attempt FAILED to "
                                    + remoteIp
                                    + ":"
                                    + remotePort
                                    + " - "
                                    + e.getMessage());
                            throw e;
                        }
                    }
                }, 1000));
        connect(creator);
    }

    protected void initializeAsServer() {

        SocketCreator creator = new SocketCreator(NetTransferMode.JINGLETCP) {

            public Socket call() throws Exception {

                log.debug("Starting Jingle/TCP Server Socket on port "
                    + localPort);
                try {
                    ServerSocket serverSocket = new ServerSocket(localPort);
                    return serverSocket.accept();
                } catch (SocketException e) {
                    log.warn("Jingle/TCP Server Socket on port " + localPort
                        + " did not receive a connection attempt within 30s: "
                        + e.getMessage());
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
                    + "] Could not connect with TCP.");

                return;
            }

            try {

                socket = socketFuture.get();

            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                log.debug("Jingle [" + connectTo.getName()
                    + "] Could not connect with TCP.");

            }

            if (!this.socket.isConnected()) {
                future.cancel(true);
                log.debug("Failed to connect");
            }

        } finally {
            thread.shutdown();
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

    public void close() {
        Util.close(this.socket);

    }

    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    public int getReadTimeout() throws IOException {
        return this.socket.getSoTimeout();
    }

    public void setReadTimeout(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);

    }

    public boolean isConnected() {
        return socket.isConnected();
    }

}
