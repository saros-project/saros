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

        BytestreamSession session = null;
        try {
            session = manager.establishSession(peer.toString());
        } catch (XMPPException e) {
            throw new CausedIOException(e);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new BytestreamConnection(peer, getNetTransferMode(), session,
            dtm);
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

            BytestreamSession session;
            try {
                session = request.accept();
            } catch (XMPPException e) {
                log.error("Socket crashed:", e); // TODO
                return;
            } catch (InterruptedException e) {
                log.error("Socket crashed:", e); // TODO
                return;
            }

            JID peer = new JID(request.getFrom());
            try {
                dtm.connectionChanged(peer, new BytestreamConnection(peer,
                    getNetTransferMode(), session, dtm));
            } catch (IOException e) {
                log.error("Socket crashed:"); // TODO
                try {
                    session.close();
                } catch (IOException e1) {
                    //
                }
            }
        }
    };

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
