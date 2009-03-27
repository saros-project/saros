package de.fu_berlin.inf.dpp.ui;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;
import de.fu_berlin.inf.dpp.util.Util;

public class NetworkView extends ViewPart implements IConnectionListener {

    private static final Logger log = Logger.getLogger(NetworkView.class
        .getName());

    private Text loggingView;

    @Override
    public void createPartControl(Composite parent) {
        Composite rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(new FillLayout());
        SashForm sash = new SashForm(rootComposite, SWT.VERTICAL);
        this.loggingView = new Text(sash, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);

        // register as connection listener
        Saros.getDefault().addListener(this);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    public void transportClosed(TransportCandidate cand) {
        // TODO Auto-generated method stub

    }

    public void transportClosedOnError(XMPPException e) {
        // TODO Auto-generated method stub

    }

    public void transportEstablished(TransportCandidate local,
        TransportCandidate remote) {
        loggingView.append("Jingle transport estabblished: "
            + local.getLocalIp() + " <-> " + remote.getLocalIp());

    }

    public void connected(final String protocol, final String remote) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                loggingView.append("P2P Connected with " + protocol + " to "
                    + remote + "\n");
            }
        });
    }

    public void exceptionOccured(JingleSessionException exception) {
        // TODO Auto-generated method stub

    }

    public void failedToSendFileListWithJingle(JID jid,
        TransferDescription transferList) {
        loggingView.append("Failed to send File with Jingle to " + jid);
    }

    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED) {
            // ((XMPPChatTransmitter) Saros.getDefault().getSessionManager()
            // .getTransmitter()).getJingleManager()
            // .addJingleFileTransferListener(this);

        } else if (newState == ConnectionState.NOT_CONNECTED) {
            // TODO remove as listener?
        }

    }

    /*
     * NetTransferMode incoming = data .getIncomingTransferMode(jid);
     * NetTransferMode outgoing = data .getOutgoingTransferMode(jid);
     * 
     * if (incoming.equals(outgoing)) { result.append(" [" + outgoing.toString()
     * + "]", StyledString.QUALIFIER_STYLER); } else { result.append(" [" +
     * outgoing.toString() + "<->" + incoming.toString() + "]",
     * StyledString.QUALIFIER_STYLER); }
     */

    public void incomingData(TransferDescription data, InputStream input,
        NetTransferMode mode) {
        // TODO Auto-generated method stub

    }

}
