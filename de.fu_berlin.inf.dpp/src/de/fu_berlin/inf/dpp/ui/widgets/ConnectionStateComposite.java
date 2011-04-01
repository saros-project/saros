package de.fu_berlin.inf.dpp.ui.widgets;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.util.FontUtils;
import de.fu_berlin.inf.dpp.util.Utils;

public class ConnectionStateComposite extends Composite {
    private static final String CONNECTED_TOOLTIP = "Connected using Saros %s";

    private static final Logger log = Logger
        .getLogger(ConnectionStateComposite.class);

    protected final IConnectionListener connectionListener = new IConnectionListener() {
        public void connectionStateChanged(XMPPConnection connection,
            final ConnectionState newState) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    updateLabel(newState);
                }
            });
        }
    };

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected XMPPAccountStore accountStore;

    protected CLabel stateLabel;

    public ConnectionStateComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        this.setLayout(LayoutUtils.createGridLayout(1, false, 10, 3, 0, 0));
        stateLabel = new CLabel(this, SWT.NONE);
        stateLabel.setLayoutData(LayoutUtils.createFillHGrabGridData());
        FontUtils.makeBold(stateLabel);

        updateLabel(saros.getConnectionState());
        this.stateLabel.setForeground(getDisplay().getSystemColor(
            SWT.COLOR_WHITE));
        this.stateLabel.setBackground(getDisplay().getSystemColor(
            SWT.COLOR_DARK_GRAY));
        this.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        saros.addListener(connectionListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        saros.removeListener(connectionListener);
    }

    protected void updateLabel(ConnectionState newState) {
        if (stateLabel != null && !stateLabel.isDisposed()) {
            stateLabel.setText(getDescription(newState));

            if (newState == ConnectionState.CONNECTED) {
                stateLabel.setToolTipText(String.format(CONNECTED_TOOLTIP,
                    saros.getVersion()));
            } else {
                stateLabel.setToolTipText(null);
            }
            layout();
        }
    }

    /**
     * @param state
     * @return a nice string description of the given state, which can be used
     *         to be shown in labels (e.g. CONNECTING becomes "Connecting...").
     */
    public String getDescription(ConnectionState state) {
        if (!accountStore.hasActiveAccount()) {
            return "Add XMPP/Jabber account first";
        }

        switch (state) {
        case NOT_CONNECTED:
            return "Not connected";
        case CONNECTING:
            return "Connecting...";
        case CONNECTED:
            JID jid = new JID(saros.getConnection().getUser());
            return jid.getBase();
        case DISCONNECTING:
            return "Disconnecting...";
        case ERROR:
            Exception e = saros.getConnectionError();
            if (e == null) {
                return "Error";
            } else {
                return "Error (" + e.getMessage() + ")";
            }
        default:
            return "UNKNOWN STATE";
        }
    }
}
