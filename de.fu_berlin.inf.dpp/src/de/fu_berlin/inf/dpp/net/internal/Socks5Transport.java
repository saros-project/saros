package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.socks5bytestream.Socks5BytestreamManager;
import org.jivesoftware.smackx.socks5bytestream.Socks5BytestreamSession;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

public class Socks5Transport extends BytestreamTransport {

    static final int BIDIRECTIONAL_TEST_INT = 5;
    static final int TEST_TIMEOUT = 1000;

    private static Logger log = Logger.getLogger(Socks5Transport.class);

    protected HashMap<String, Exchanger<Socks5BytestreamSession>> runningConnects = new HashMap<String, Exchanger<Socks5BytestreamSession>>();

    @Override
    protected BytestreamSession acceptRequest(BytestreamRequest request) {

        Socks5BytestreamSession inSession = (Socks5BytestreamSession) super
            .acceptRequest(request);

        if (inSession == null || inSession.isDirect())
            return inSession;

        // return if bidirectional
        if (checkStreamDirection(inSession, true))
            return inSession;

        // any running connects?
        String peer = request.getFrom();
        Exchanger<Socks5BytestreamSession> exchanger = runningConnects
            .get(peer);

        // if so, transmit session to running connect thread and return null
        if (exchanger != null) {
            try {
                exchanger.exchange(inSession);
            } catch (InterruptedException e) {
                log.debug(prefix()
                    + "Wrapping bidirectional string was interrupted.");
            }
            return null;
        }

        // else try to establish a new stream to send
        try {

            /*
             * Use the superclass method because we know already the stream
             * direction and there is no need to wait for another request
             */
            Socks5BytestreamSession outSession = (Socks5BytestreamSession) super
                .establishSession(peer);

            log.debug(prefix() + "wrapped bidirectional session established");
            return new WrappedBidirectionalSocks5BytestreamSession(inSession,
                outSession);

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
    protected BytestreamSession establishSession(String peer)
        throws XMPPException, IOException, InterruptedException {

        // before establishing, we have to put the exchanger to the map
        Exchanger<Socks5BytestreamSession> exchanger = new Exchanger<Socks5BytestreamSession>();
        runningConnects.put(peer, exchanger);

        try {

            Socks5BytestreamSession outSession = (Socks5BytestreamSession) super
                .establishSession(peer);

            if (outSession.isDirect())
                return outSession;

            // return if bidirectional
            if (checkStreamDirection(outSession, false))
                return outSession;

            // else wait for request
            try {
                Socks5BytestreamSession inSession = exchanger.exchange(null,
                    TEST_TIMEOUT, TimeUnit.MILLISECONDS);

                log.debug(prefix()
                    + "wrapped bidirectional session established");
                return new WrappedBidirectionalSocks5BytestreamSession(
                    inSession, outSession);

            } catch (TimeoutException e) {
                throw new IOException(prefix()
                    + "wrapping a bidirectional session timed out. ("
                    + TEST_TIMEOUT + "ms)");
            }

        } finally {
            runningConnects.remove(peer);
        }
    }

    protected boolean checkStreamDirection(Socks5BytestreamSession session,
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
        // TODO remove this line
        SmackConfiguration.setLocalSocks5ProxyEnabled(false);
        return socks5ByteStreamManager;
    }

    @Override
    protected NetTransferMode getNetTransferMode() {
        return NetTransferMode.SOCKS5;
    }

    private String prefix() {
        return "[" + getNetTransferMode().name() + "] ";
    }
}