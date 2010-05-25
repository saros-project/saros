package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Exchanger;
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

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.CausedIOException;

/**
 * Transport class for SOCKS5 In case of unidirectional connections (i.e.
 * OpenFire) a second connection is established and both are wrapped in a
 * bidirectional one (see {#link WrappedBidirectionalSocks5BytestreamSession})
 */
public class Socks5Transport extends BytestreamTransport {

    private static Logger log = Logger.getLogger(Socks5Transport.class);
    private static final int BIDIRECTIONAL_TEST_INT = 5;
    private static final int TEST_TIMEOUT = 1000;
    private static final String RESPONSE_SESSION_ID_PREFIX = "response_js5";
    private static final Random randomGenerator = new Random();

    private static Socks5Transport instance = null;

    private Socks5Transport() {
        //
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

    @Override
    protected BinaryChannel acceptRequest(BytestreamRequest request)
        throws IOException, XMPPException, InterruptedException {

        Socks5BytestreamSession inSession = (Socks5BytestreamSession) request
            .accept();
        String peer = request.getFrom();

        if (inSession == null)
            return null;

        if (isResponse(request)) {

            // get running connect
            Exchanger<Socks5BytestreamSession> exchanger = runningConnects
                .get(peer);

            if (exchanger == null) {
                log
                    .error(prefix()
                        + "Received response for wrapped unidirectional SOCKS5 session without a running connect");
                return null;
            }

            try {
                exchanger.exchange(inSession, TEST_TIMEOUT,
                    TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.debug(prefix()
                    + "Wrapping bidirectional stream was interrupted.");
            } catch (TimeoutException e) {
                log
                    .error(prefix()
                        + "Wrapping bidirectional stream timed out in Request! Should have happened.");
            }
            /*
             * don't return a channel if it is a response (handled by running
             * connect)
             */
            return null;
        }

        /*
         * if the session is directed has to checked after isResponse: one peer
         * might have a local SOCKS5 proxy enabled and the other not
         */
        if (inSession.isDirect())
            return new BinaryChannel(inSession, NetTransferMode.SOCKS5_DIRECT);

        // only check stream direction if no response
        if (streamIsBidirectional(inSession, true))
            return new BinaryChannel(inSession, NetTransferMode.SOCKS5_MEDIATED);

        try {

            Socks5BytestreamSession outSession = (Socks5BytestreamSession) establishResponseSession(peer);

            if (outSession.isDirect()) {
                log.debug(prefix()
                    + "no need for wrapping: response connection is direct.");
                inSession.close();
                return new BinaryChannel(outSession,
                    NetTransferMode.SOCKS5_DIRECT);
            }

            log.debug(prefix() + "wrapped bidirectional session established");
            BytestreamSession session = new WrappedBidirectionalSocks5BytestreamSession(
                inSession, outSession);
            return new BinaryChannel(session, NetTransferMode.SOCKS5_MEDIATED);

        } catch (XMPPException e) {
            log
                .error(
                    prefix()
                        + "Socket crashed while initiating sending session (for wrapping)",
                    e);
        } catch (IOException e) {
            log
                .error(
                    prefix()
                        + "Socket crashed while initiating sending session (for wrapping)",
                    e);
        } catch (InterruptedException e) {
            log.error(prefix() + "Interrupted while initiating Session:", e);
        }

        try {
            inSession.close();
        } catch (IOException e) {
            // nothing to do here
        }
        return null;
    }

    @Override
    protected BinaryChannel establishBinaryChannel(String peer,
        SubMonitor progress) throws XMPPException, IOException,
        InterruptedException {

        // before establishing, we have to put the exchanger to the map
        Exchanger<Socks5BytestreamSession> exchanger = new Exchanger<Socks5BytestreamSession>();
        runningConnects.put(peer, exchanger);

        log
            .debug(prefix()
                + "establishing new connection with local proxy "
                + (SmackConfiguration.isLocalSocks5ProxyEnabled() ? "enabled (Port "
                    + SmackConfiguration.getLocalSocks5ProxyPort() + ")."
                    : "disabled."));

        try {

            Socks5BytestreamSession outSession = (Socks5BytestreamSession) manager
                .establishSession(peer);

            if (outSession.isDirect())
                return new BinaryChannel(outSession,
                    NetTransferMode.SOCKS5_DIRECT);

            // return if bidirectional
            if (streamIsBidirectional(outSession, false))
                return new BinaryChannel(outSession,
                    NetTransferMode.SOCKS5_MEDIATED);

            progress
                .subTask("SOCKS5 stream is unidirectional. Waiting for peer to connect ...");

            // else wait for request
            try {
                Socks5BytestreamSession inSession = exchanger.exchange(null,
                    TEST_TIMEOUT + 10000, TimeUnit.MILLISECONDS);

                if (inSession.isDirect()) {
                    log
                        .debug(prefix()
                            + " no need for wrapping: response connection is direct.");
                    outSession.close();
                    return new BinaryChannel(inSession,
                        NetTransferMode.SOCKS5_DIRECT);
                }

                log.debug(prefix()
                    + "wrapped bidirectional session established");
                BytestreamSession session = new WrappedBidirectionalSocks5BytestreamSession(
                    inSession, outSession);
                return new BinaryChannel(session,
                    NetTransferMode.SOCKS5_MEDIATED);

            } catch (TimeoutException e) {
                throw new CausedIOException(prefix()
                    + "wrapping a bidirectional session timed out. ("
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
                log.debug(prefix() + "stream is bidirectional. ("
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