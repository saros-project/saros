package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.Transport;
import de.fu_berlin.inf.dpp.util.CausedIOException;

public abstract class BytestreamTransport implements Transport {

    private static final Logger log = Logger.getLogger(Socks5Transport.class);

    protected BytestreamManager manager;
    protected DataTransferManager dtm;

    /*
     * @param peer the JID of the user we'd like to connect
     * 
     * @return
     */
    public IConnection connect(final JID peer, SubMonitor progress)
        throws IOException {

        try {

            BytestreamSession session = establishSession(peer.toString());
            return new BytestreamConnection(peer, getNetTransferMode(),
                session, dtm);

        } catch (XMPPException e) {
            throw new CausedIOException(e);
        } catch (InterruptedException e) {
            log.debug("Interrupted while initiating session:");
            throw new CausedIOException(e);
        }
    }

    /* 
     * 
     */
    public void disposeXMPPConnection() {
        if (manager != null) {
            manager.removeIncomingBytestreamListener(streamListener);
            // socks5ByteStreamManager.disableService();
            manager = null;
        }
    }

    protected BytestreamListener streamListener = new BytestreamListener() {

        public void incomingBytestreamRequest(BytestreamRequest request) {

            BytestreamSession session = acceptRequest(request);

            if (session == null)
                return;

            JID peer = new JID(request.getFrom());
            try {
                dtm.connectionChanged(peer, new BytestreamConnection(peer,
                    getNetTransferMode(), session, dtm));
            } catch (IOException e) {
                log.error("Bytestream session crashed:", e);
                try {
                    session.close();
                } catch (IOException e1) {
                    // 
                }
            }
        }
    };

    protected BytestreamSession establishSession(String peer)
        throws XMPPException, IOException, InterruptedException {
        return manager.establishSession(peer.toString());
    }

    /**
     * 
     * @param request
     * @return BytestreamSession, null if failed or if answer for bidirectional
     *         connecting (to be overridden in subclasses)
     */
    protected BytestreamSession acceptRequest(BytestreamRequest request) {
        BytestreamSession session = null;
        try {
            session = request.accept();
        } catch (XMPPException e) {
            log.error("Socket crashed, no session for request established:", e);
        } catch (InterruptedException e) {
            log.debug("Interrupted while initiating new session.");
        }
        return session;
    }

    public void prepareXMPPConnection(XMPPConnection connection,
        DataTransferManager dtm) {
        this.dtm = dtm;
        manager = getManager(connection);
        manager.addIncomingBytestreamListener(streamListener);
    }

    @Override
    public String toString() {
        return getNetTransferMode().getXEP();
    }

    abstract protected NetTransferMode getNetTransferMode();

    abstract public BytestreamManager getManager(XMPPConnection connection);

}
