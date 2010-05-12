package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;

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

    protected static final int BIDIRECTIONAL = 5;
    protected static final int TEST_TIMEOUT = 1000;

    private static Logger log = Logger.getLogger(Socks5Transport.class);

    protected HashMap<String, Callback> runningConnects = new HashMap<String, Callback>();
    protected HashMap<String, Socks5BytestreamSession> runningRequests = new HashMap<String, Socks5BytestreamSession>();

    protected boolean isConnectingTo(String peer) {
        return (runningConnects.get(peer) != null);
    }

    protected class Callback {
        Socks5BytestreamSession pending = null;
        public Thread thread;

        public Socks5BytestreamSession get() throws InterruptedException,
            XMPPException {
            return pending;
        }

        public void set(Socks5BytestreamSession session) {
            pending = session;
        }

    }

    @Override
    protected BytestreamSession acceptRequest(BytestreamRequest request) {

        Socks5BytestreamSession inSession = (Socks5BytestreamSession) super
            .acceptRequest(request);

        if (inSession == null || inSession.isDirect())
            return inSession;

        if (checkStreamDirection(inSession, true))
            return inSession;

        String peer = request.getFrom();
        // any running connects?
        Callback callback = runningConnects.get(peer);

        if (callback != null) {
            // if so, transmit session and wake up
            callback.set(inSession);
            callback.thread.resume();
            return null;
        }
        try {

            Socks5BytestreamSession outSession = (Socks5BytestreamSession) super
                .establishSession(peer, false);

            return new WrappedBidirectionalSocks5BytestreamSession(inSession,
                outSession);

        } catch (XMPPException e) {
            log
                .error(
                    "Socket crashed for wrapped directional Socks5 connections:",
                    e);
        } catch (InterruptedException e) {
            log.error("Interrupted while initiating Session:", e);
        } catch (IOException e) {
            log
                .error(
                    "Socket crashed for wrapped directional Socks5 connections:",
                    e);
        }

        try {
            inSession.close();
        } catch (IOException e) {
            // nothing to do here
        }
        return null;
    }

    @Override
    protected BytestreamSession establishSession(String peer,
        boolean isInitiator) throws XMPPException, IOException,
        InterruptedException {

        Callback callback = new Callback();
        callback.thread = Thread.currentThread();
        runningConnects.put(peer, callback);

        Socks5BytestreamSession outSession = (Socks5BytestreamSession) super
            .establishSession(peer, isInitiator);

        if (outSession.isDirect())
            return outSession;

        if (checkStreamDirection(outSession, false))
            return outSession;

        Socks5BytestreamSession inSession = runningRequests.get(peer);

        if (inSession == null) {
            // final Callback callback = new Callback();
            // callback.thread = Thread.currentThread();
            // runningConnects.put(peer, callback);
            // TODO this is a hack only

            if (callback.get() == null)
                callback.thread.suspend();

            /*
             * FutureTask<Socks5BytestreamSession> future = new
             * FutureTask<Socks5BytestreamSession>( new
             * Callable<Socks5BytestreamSession>() { public
             * Socks5BytestreamSession call() throws Exception {
             * 
             * return callback.get(); } });
             */

            inSession = callback.get();

        }

        return new WrappedBidirectionalSocks5BytestreamSession(inSession,
            outSession);
    }

    @SuppressWarnings( { "null" })
    protected boolean checkStreamDirection(Socks5BytestreamSession session,
        boolean toSendFirst) {

        OutputStream out = null;
        InputStream in = null;
        try {
            session.setReadTimeout(TEST_TIMEOUT);

            out = session.getOutputStream();
            in = session.getInputStream();
            int test = 0;

            if (toSendFirst) {
                out.write(BIDIRECTIONAL);
                test = in.read();
            } else {
                test = in.read();
                out.write(BIDIRECTIONAL);
            }

            if (test == BIDIRECTIONAL) {
                log.debug("SOCKS5 stream is bidirectional. ("
                    + (toSendFirst ? "sending" : "receiving") + ")");
                return true;
            }

        } catch (SocketTimeoutException ste) {
            log
                .debug("SOCKS5 stream is unidirectional. Trying to wrap bidirectional one.");
        } catch (Exception e) {
            log.error("SOCKS5 stream direction test failed. ", e);
        }
        // TODO: how to close and reopen the streams

        /*
         * finally { try { out.close(); in.close(); } catch (IOException e) { //
         * } }
         */
        return false;
    }

    @Override
    public BytestreamManager getManager(XMPPConnection connection) {
        Socks5BytestreamManager socks5ByteStreamManager = Socks5BytestreamManager
            .getBytestreamManager(connection);
        socks5ByteStreamManager.setTargetResponseTimeout(10000);
        SmackConfiguration.setLocalSocks5ProxyEnabled(false);
        /*
         * neuer Port für jede Verbindung: bspw. für mehrere Eclipse auf einem
         * Rechner oder um Probleme mit belegten Ports zu umgehen
         */
        SmackConfiguration
            .setLocalSocks5ProxyPort((int) (Math.random() * 300) + 7778);
        return socks5ByteStreamManager;
    }

    @Override
    protected NetTransferMode getNetTransferMode() {
        return NetTransferMode.SOCKS5;
    }
}