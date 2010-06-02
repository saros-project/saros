package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.socks5bytestream.Socks5BytestreamManager;
import org.jivesoftware.smackx.socks5bytestream.Socks5BytestreamSession;
import org.jivesoftware.smackx.socks5bytestream.Socks5Proxy;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;

/**
 * Transport class for SOCKS5 bytestreams. In case of mediated connections a
 * second connection is established from the peer to see whether it is direct.
 * If none of them is direct, the one of the first connector is used.
 * 
 * However, still there might be a server that only supports unidirectional
 * SOCKS5 bytestreams (i.e. OpenFire). In that case both mediated unidirectional
 * connections are wrapped into a bidirectional one. (see {#link
 * WrappedBidirectionalSocks5BytestreamSession})
 * 
 * @author jurke
 */
public class Socks5Transport extends BytestreamTransport {

    private static Logger log = Logger.getLogger(Socks5Transport.class);
    private static final int BIDIRECTIONAL_TEST_INT = 5;
    private static final int TEST_TIMEOUT = 1000;
    private static final String RESPONSE_SESSION_ID_PREFIX = "response_js5";
    private static final Random randomGenerator = new Random();
    private static final int NUMBER_OF_RESPONSE_THREADS = 10;

    private static Socks5Transport instance = null;

    private Socks5Transport() {
        executorService = Executors.newFixedThreadPool(
            NUMBER_OF_RESPONSE_THREADS, new NamedThreadFactory(
                "SOCKS5_Establish_response_connection"));
    }

    public static Socks5Transport getTransport() {
        if (instance == null)
            instance = new Socks5Transport();
        return instance;
    }

    protected HashMap<String, Exchanger<Socks5BytestreamSession>> runningConnects = new HashMap<String, Exchanger<Socks5BytestreamSession>>();

    protected String getNextResponseSessionID() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(RESPONSE_SESSION_ID_PREFIX);
        buffer.append(Math.abs(randomGenerator.nextLong()));
        return buffer.toString();
    }

    protected void closeQuietly(BytestreamSession stream) {
        if (stream == null)
            return;
        try {
            stream.close();
        } catch (Exception e) {
            //
        }
    }

    protected void cancelQuietly(Future<Socks5BytestreamSession> future) {
        try {
            if (future.isDone()) {
                try {
                    closeQuietly(future.get());
                } catch (Exception e) {
                    //
                }
            }
            future.cancel(true);
        } catch (Exception e) {
            //
        }
    }

    protected ExecutorService executorService;

    protected Future<Socks5BytestreamSession> futureToEstablishResponseSession(
        final String peer) {

        return executorService.submit(new Callable<Socks5BytestreamSession>() {
            public Socks5BytestreamSession call() throws Exception {
                return (Socks5BytestreamSession) establishResponseSession(peer);
            }
        });
    }

    protected void handleResponse(BytestreamRequest request)
        throws XMPPException, InterruptedException {

        String peer = request.getFrom();
        Socks5BytestreamSession inSession = (Socks5BytestreamSession) request
            .accept();

        // get running connect
        Exchanger<Socks5BytestreamSession> exchanger = runningConnects
            .get(peer);

        if (exchanger == null) {
            log
                .error(prefix()
                    + "Received response for wrapped unidirectional SOCKS5 session without a running connect");
            return;
        }

        try {
            exchanger.exchange(inSession, TEST_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.debug(prefix()
                + "Wrapping bidirectional stream was interrupted.");
            closeQuietly(inSession);
        } catch (TimeoutException e) {
            log
                .error(prefix()
                    + "Wrapping bidirectional stream timed out in Request! Shouldn't have happened.");
            closeQuietly(inSession);
        }

    }

    @Override
    protected BinaryChannel acceptRequest(BytestreamRequest request)
        throws IOException, XMPPException, InterruptedException {

        String peer = request.getFrom();

        if (isResponse(request)) {
            logStartSession("recieving response connection from " + peer);
            handleResponse(request);
            return null;
        } else {
            logStartSession("recieving request from " + peer);

            // start to establish response
            Future<Socks5BytestreamSession> responseFuture = futureToEstablishResponseSession(peer);

            Socks5BytestreamSession inSession = (Socks5BytestreamSession) request
                .accept();

            if (inSession.isDirect()) {
                cancelQuietly(responseFuture);
                return new BinaryChannel(inSession,
                    NetTransferMode.SOCKS5_DIRECT);
            }

            Socks5BytestreamSession outSession = null;

            try {

                outSession = responseFuture.get();

                if (outSession.isDirect()) {
                    log
                        .debug(prefix()
                            + "newly established session is direct! Discarding the other.");
                    closeQuietly(inSession);
                    return new BinaryChannel(outSession,
                        NetTransferMode.SOCKS5_DIRECT);
                }

                String msg = prefix()
                    + "response connection is mediated, too, ";

                // only check stream direction if no response
                if (streamIsBidirectional(inSession, true)) {
                    log
                        .debug(msg
                            + "but at least the server allows bidirectional connections.");
                    closeQuietly(outSession);
                    return new BinaryChannel(inSession,
                        NetTransferMode.SOCKS5_MEDIATED);
                }

                log
                    .debug(msg
                        + "and the server does not allow bidirectional connections. Wrapped session established.");
                BytestreamSession session = new WrappedBidirectionalSocks5BytestreamSession(
                    inSession, outSession);
                return new BinaryChannel(session,
                    NetTransferMode.SOCKS5_MEDIATED);

            } catch (IOException e) {
                log
                    .error(
                        prefix()
                            + "Socket crashed while initiating sending session (for wrapping)",
                        e);
            } catch (ExecutionException e) {
                log
                    .error(
                        "An error occured while establishing a response connection ",
                        e.getCause());
            } catch (InterruptedException e) {
                log
                    .debug(prefix()
                        + "Interrupted while recieving request to establish a new connection");
            }

        }

        return null;
    }

    private boolean localSOCKS5ProxyIsRunning() {
        if (SmackConfiguration.isLocalSocks5ProxyEnabled()) {
            if (!Socks5Proxy.getSocks5Proxy().isRunning()) {
                log.warn(prefix()
                    + "Local SOCKS5 proxy enabled but couldn't start");
                return false;
            }
            return true;
        } else
            return false;
    }

    private int getLocalSocks5ProxyPort() {
        int port = SmackConfiguration.getLocalSocks5ProxyPort();
        int realPort = Socks5Proxy.getSocks5Proxy().getPort();

        if (port != realPort && -port != realPort)
            log.info(prefix() + "proxy port is " + realPort
                + " (configured to " + port + ")");
        return realPort;
    }

    private void logStartSession(String introduction) {
        log.debug(prefix()
            + introduction
            + " with local proxy "
            + (localSOCKS5ProxyIsRunning() ? "enabled (Port "
                + getLocalSocks5ProxyPort() + ")."
                : "disabled. Local Adresses: "
                    + Socks5Proxy.getSocks5Proxy().getLocalAddresses()
                        .toString()));

    }

    @Override
    protected BinaryChannel establishBinaryChannel(String peer,
        SubMonitor progress) throws XMPPException, IOException,
        InterruptedException {

        // before establishing, we have to put the exchanger to the map
        Exchanger<Socks5BytestreamSession> exchanger = new Exchanger<Socks5BytestreamSession>();
        runningConnects.put(peer, exchanger);

        logStartSession("establishing new connection to " + peer);

        try {

            Exception exception = null;
            Socks5BytestreamSession outSession = null;
            // Do we get a wroking connection?
            try {

                outSession = (Socks5BytestreamSession) manager
                    .establishSession(peer);

                if (outSession.isDirect())
                    return new BinaryChannel(outSession,
                        NetTransferMode.SOCKS5_DIRECT);

                log.debug(prefix()
                    + "session is mediated. Waiting for peer to connect ...");

                progress
                    .subTask("SOCKS5 stream is mediated. Waiting for peer to connect ...");
                progress.worked(5);

            } catch (IOException e) {
                exception = e;
            } catch (XMPPException e) {
                exception = e;
            }

            if (exception != null) {

                log.warn(prefix() + "could not connect to " + peer
                    + " because: " + exception.getMessage()
                    + ". Waiting for peer to connect ...");

                progress.subTask("Could not connect to " + peer
                    + ". Waiting for peer to connect ...");
                progress.worked(5);
            }

            // else wait for request
            try {
                Socks5BytestreamSession inSession = exchanger.exchange(null,
                    TEST_TIMEOUT + 10000, TimeUnit.MILLISECONDS);

                if (inSession.isDirect()) {
                    log
                        .debug(prefix()
                            + "response connection is direct! Discarding the other.");
                    closeQuietly(outSession);
                    return new BinaryChannel(inSession,
                        NetTransferMode.SOCKS5_DIRECT);
                }

                String msg = prefix()
                    + "response connection is mediated, too, ";

                if (streamIsBidirectional(outSession, false)) {
                    closeQuietly(inSession);
                    log
                        .debug(msg
                            + "but at least the server allows bidirectional connections.");
                    return new BinaryChannel(outSession,
                        NetTransferMode.SOCKS5_MEDIATED);
                }

                log
                    .debug(msg
                        + "and the server does not allow bidirectional connections. Wrapped session established.");
                BytestreamSession session = new WrappedBidirectionalSocks5BytestreamSession(
                    inSession, outSession);
                return new BinaryChannel(session,
                    NetTransferMode.SOCKS5_MEDIATED);

            } catch (TimeoutException e) {
                closeQuietly(outSession);
                throw new CausedIOException(prefix()
                    + "waiting for a response session timed out. ("
                    + TEST_TIMEOUT + "ms)", e);
            }
        } finally {
            runningConnects.remove(peer);
        }
    }

    protected boolean streamIsBidirectional(Socks5BytestreamSession session,
        boolean sendFirst) {

        try {
            OutputStream out = session.getOutputStream();
            InputStream in = session.getInputStream();
            int test = 0;

            session.setReadTimeout(TEST_TIMEOUT);

            if (sendFirst) {
                out.write(BIDIRECTIONAL_TEST_INT);
                test = in.read();
            } else {
                test = in.read();
                out.write(BIDIRECTIONAL_TEST_INT);
            }

            if (test == BIDIRECTIONAL_TEST_INT) {
                log.trace(prefix() + "stream is bidirectional. ("
                    + (sendFirst ? "sending" : "receiving") + ")");
                return true;
            } else {
                log
                    .error(prefix()
                        + "stream seems to work but recieved wrong result: "
                        + test);
            }

        } catch (SocketTimeoutException ste) {
            // expected if unidirectional stream
        } catch (IOException e) {
            log
                .error(
                    prefix()
                        + "stream direction test failed. However, still trying to establish a bidirectional one.",
                    e);
            return false;
        }
        /*
         * Note: the streams cannot be closed here - even not the unused ones -
         * as setting the timeout later on would throw an exception
         */

        log.debug(prefix()
            + "stream is unidirectional. Trying to wrap bidirectional one.");

        return false;
    }

    @Override
    public BytestreamManager getManager(XMPPConnection connection) {
        Socks5BytestreamManager socks5ByteStreamManager = Socks5BytestreamManager
            .getBytestreamManager(connection);
        socks5ByteStreamManager.setTargetResponseTimeout(10000);
        return socks5ByteStreamManager;
    }

    protected BytestreamSession establishResponseSession(String peer)
        throws XMPPException, IOException, InterruptedException {

        logStartSession("establishing new response connection to " + peer);

        return manager.establishSession(peer.toString(), this
            .getNextResponseSessionID());
    }

    protected boolean isResponse(BytestreamRequest request) {
        return request.getSessionID().startsWith(RESPONSE_SESSION_ID_PREFIX);
    }

    @Override
    public NetTransferMode getDefaultNetTransferMode() {
        return NetTransferMode.SOCKS5;
    }

    protected String prefix() {
        return "[" + getDefaultNetTransferMode().name() + "] ";
    }

    @Override
    public void disposeXMPPConnection() {
        List<Runnable> notCommenced = executorService.shutdownNow();
        if (notCommenced.size() > 0)
            log
                .warn(prefix()
                    + "threads for response connections found that didn't commence yet");
        super.disposeXMPPConnection();
    }

    /**
     * Wraps two Socks5BytestreamSessions in one, where for the first one, "in",
     * the InputStream has to work, for the second one, "out", the OutputStream.
     */
    protected class WrappedBidirectionalSocks5BytestreamSession implements
        BytestreamSession {

        protected Socks5BytestreamSession in;
        protected Socks5BytestreamSession out;

        public WrappedBidirectionalSocks5BytestreamSession(
            Socks5BytestreamSession in, Socks5BytestreamSession out) {
            this.in = in;
            this.out = out;

        }

        public void close() throws IOException {
            IOException e = null;

            try {
                in.close();
            } catch (IOException e1) {
                e = e1;
            }

            try {
                out.close();
            } catch (IOException e1) {
                e = e1;
            }

            if (e != null)
                throw e;
        }

        public InputStream getInputStream() throws IOException {
            return in.getInputStream();
        }

        public OutputStream getOutputStream() throws IOException {
            return out.getOutputStream();
        }

        public int getReadTimeout() throws IOException {
            return in.getReadTimeout();
        }

        public void setReadTimeout(int timeout) throws IOException {
            in.setReadTimeout(timeout);
        }

    }

}