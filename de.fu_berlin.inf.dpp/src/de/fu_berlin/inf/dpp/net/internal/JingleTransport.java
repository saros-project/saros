package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionRequestListener;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.ITransport;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.FileTransferConnection;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;

public class JingleTransport implements ITransport {

    private static final Logger log = Logger.getLogger(JingleTransport.class);

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    private Saros saros;
    private JingleFileTransferManager manager;
    private DataTransferManager dtm;
    private static JingleTransport instance = null;

    private JingleTransport(Saros saros) {
        this.saros = saros;
    }

    public static JingleTransport getTransport(Saros saros) {
        if (instance == null)
            instance = new JingleTransport(saros);
        return instance;
    }

    public IBytestreamConnection connect(final JID peer, SubMonitor progress)
        throws IOException, InterruptedException {

        progress.subTask("Try to initiate bytestream with " + toString());
        BinaryChannel channel = establishBinaryChannel(peer.toString(),
            progress);
        return new BinaryChannelConnection(peer, channel, dtm);
    }

    public void disposeXMPPConnection() {
        if (manager != null) {
            manager.removeIncomingJingleSessionListener(streamListener);
            manager = null;
        }
    }

    protected JingleSessionRequestListener streamListener = new JingleSessionRequestListener() {
        public void sessionRequested(JingleSessionRequest request) {
            try {
                JID jid = new JID(request.getFrom());
                FileTransferConnection incoming = manager
                    .getFileConnection(jid);
                log.debug("Receiving Jingle Session Request from " + jid);

                incoming = manager.new FileTransferConnection(jid);
                manager.putConnection(incoming, jid);
                incoming.setState(incoming.getState());

                try {
                    // Accept the call
                    JingleSession session = request.accept();
                    incoming.putSession(session);
                    manager.initJingleListener(incoming, jid);
                    session.startIncoming();
                } catch (XMPPException e) {
                    log.error("Failed to start JingleSession", e);
                    incoming.putSession(null);
                    incoming.setState(JingleConnectionState.ERROR);
                }
            } catch (Exception e) {
                log.error("Starting Session failed: ", e);
            }
        }
    };

    protected BinaryChannel establishBinaryChannel(String peer,
        SubMonitor progress) throws IOException {
        BytestreamSession session = null;
        try {
            session = manager.establishSession(peer.toString());
        } catch (Exception e) {
            log.debug("Could not establish a JingleSession");
        }
        return new BinaryChannel(session, getDefaultNetTransferMode());
    }

    public void prepareXMPPConnection(XMPPConnection connection,
        DataTransferManager dtm) {
        this.dtm = dtm;
        manager = getManager(connection);
        manager.addIncomingBytestreamListener(streamListener);
    }

    public JingleFileTransferManager getManager(XMPPConnection connection) {
        return JingleFileTransferManager.getManager(saros, connection, dtm);
    }

    @Override
    public String toString() {
        return getDefaultNetTransferMode().getXEP();
    }

    public NetTransferMode getDefaultNetTransferMode() {
        return NetTransferMode.JINGLEUDP;
    }

}
