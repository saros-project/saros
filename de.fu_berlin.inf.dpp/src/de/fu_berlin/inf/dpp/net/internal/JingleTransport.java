package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.ITransport;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.FileTransferConnection;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;

public class JingleTransport implements ITransport {

    private Saros saros;

    public JingleTransport(Saros saros) {
        this.saros = saros;
    }

    @Inject
    protected DiscoveryManager discoveryManager;

    public IBytestreamConnection connect(JID peer, SubMonitor progress)
        throws IOException {

        FileTransferConnection connection = null;
        try {
            connection = (FileTransferConnection) manager.connect(peer,
                progress);
        } catch (JingleSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return connection;
    }

    @Inject
    protected PreferenceUtils preferenceUtils;

    public void disposeXMPPConnection() {

        if (!manager.isNUll()) {
            manager.cancelThread();

            manager.terminateAllJingleSessions();
            manager.setNull();
        }
    }

    private JingleFileTransferManager manager;

    public void prepareXMPPConnection(final XMPPConnection connection,
        final DataTransferManager dtm) {

        manager = JingleFileTransferManager.getManager(saros, connection, dtm);

    }

    @Override
    public String toString() {
        return "Jingle/UDP";
    }

    public NetTransferMode getDefaultNetTransferMode() {
        return NetTransferMode.JINGLEUDP;
    }

}
