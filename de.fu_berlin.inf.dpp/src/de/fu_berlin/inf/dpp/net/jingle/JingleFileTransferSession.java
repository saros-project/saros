package de.fu_berlin.inf.dpp.net.jingle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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

import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Utils;

public class JingleFileTransferSession extends JingleMediaSession implements
    BytestreamSession {

    private static final long TIMEOUTSECONDS = 30;
    protected Logger log = Logger.getLogger(JingleFileTransferSession.class);
    public Socket socket;

    protected String localIP;
    protected int localPort;
    protected String remoteIP;
    protected int remotePort;

    public JingleFileTransferSession(PayloadType payloadType,
        TransportCandidate remote, TransportCandidate local,
        String mediaLocator, JingleSession jingleSession) {
        super(payloadType, remote, local, mediaLocator, jingleSession);

        this.initialize();

    }

    @Override
    public void initialize() {

        this.localIP = this.getLocal().getIp();
        this.localPort = this.getLocal().getPort();

        this.remoteIP = this.getRemote().getIp();
        this.remotePort = this.getRemote().getPort();

        if (isInitiator()) {
            initializeAsServer();
        } else {
            initializeAsClient();
        }

    }

    private void initializeAsClient() {

        Callable<Socket> toExecute = Utils.retryEveryXms(new Callable<Socket>() {
            public Socket call() throws Exception {
                log.debug("Jingle/TCP connection attempt to " + remoteIP + ":"
                    + remotePort);
                return new Socket(remoteIP, remotePort);
            }
        }, 1000);

        connect(toExecute);
    }

    private void initializeAsServer() {

        Callable<Socket> toExecute = new Callable<Socket>() {
            public Socket call() throws Exception {
                log.debug("Starting Jingle/TCP Server Socket on port "
                    + localPort);
                ServerSocket socket = new ServerSocket(localPort);
                return socket.accept();
            }

        };
        connect(toExecute);
    }

    private void connect(Callable<Socket> service) {

        ExecutorService thread = Executors
            .newSingleThreadExecutor(new NamedThreadFactory("Jingle-Connect-"
                + this.getJingleSession().getResponder() + "-"));

        try {
            ExecutorCompletionService<Socket> completionService = new ExecutorCompletionService<Socket>(
                thread);

            Future<Socket> future = completionService.submit(service);
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
                log.debug("Jingle [" + getJID()
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
                log.debug("Jingle [" + getJID()
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

    protected String getJID() {
        if (isInitiator())
            return this.getJingleSession().getInitiator();
        return this.getJingleSession().getResponder();
    }

    private boolean isInitiator() {
        return getJingleSession().getInitiator().equals(
            getJingleSession().getConnection().getUser());
    }

    @Override
    public void setTrasmit(boolean active) {
        log.error("Unexpected call to setTrasmit(active ==" + active + ")");

    }

    @Override
    public void startReceive() {
        // Do nothing -> Users should call send(...) directly

    }

    @Override
    public void startTrasmit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopReceive() {
        // Do nothing -> Users should call send(...) directly
    }

    @Override
    public void stopTrasmit() {
        // Do nothing

    }

    // Bytestream Methods
    public void close() throws IOException {
        Utils.close(socket);
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public int getReadTimeout() throws IOException {
        return socket.getSoTimeout();
    }

    public void setReadTimeout(int timeout) throws IOException {
        socket.setSoTimeout(timeout);
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

}
