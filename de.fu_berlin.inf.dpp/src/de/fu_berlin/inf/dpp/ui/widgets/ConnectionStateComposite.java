package de.fu_berlin.inf.dpp.ui.widgets;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.Connection;
import org.osgi.framework.Version;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.nebula.utils.FontUtils;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

public class ConnectionStateComposite extends Composite {
    private static final String CONNECTED_TOOLTIP = Messages.ConnectionStateComposite_tooltip_connected;

    private static final Logger log = Logger
        .getLogger(ConnectionStateComposite.class);

    @Inject
    protected SarosNet sarosNet;

    @Inject
    protected/* BUG IN PICO cannot resolve bindings in child containers */// @SarosVersion
    Version version;

    @Inject
    protected XMPPAccountStore accountStore;

    protected CLabel stateLabel;

    private ConnectionState lastConnectionState;

    protected final IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            final ConnectionState newState) {
            SWTUtils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    updateLabel(newState);
                }
            });
        }
    };

    public ConnectionStateComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        this.setLayout(LayoutUtils.createGridLayout(1, false, 10, 3, 0, 0));
        stateLabel = new CLabel(this, SWT.NONE);
        stateLabel.setLayoutData(LayoutUtils.createFillHGrabGridData());
        FontUtils.makeBold(stateLabel);

        updateLabel(sarosNet.getConnectionState());
        this.stateLabel.setForeground(getDisplay().getSystemColor(
            SWT.COLOR_WHITE));
        this.stateLabel.setBackground(getDisplay().getSystemColor(
            SWT.COLOR_DARK_GRAY));
        this.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        sarosNet.addListener(connectionListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        sarosNet.removeListener(connectionListener);
    }

    protected void updateLabel(ConnectionState newState) {

        // do not hide the latest error
        if (lastConnectionState == ConnectionState.ERROR
            && newState == ConnectionState.NOT_CONNECTED)
            return;

        lastConnectionState = newState;

        if (stateLabel != null && !stateLabel.isDisposed()) {
            stateLabel.setText(getDescription(newState));

            if (newState == ConnectionState.CONNECTED) {
                stateLabel.setToolTipText(String.format(CONNECTED_TOOLTIP,
                    version));
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
        if (accountStore.isEmpty()) {
            return Messages.ConnectionStateComposite_info_add_jabber_account;
        }

        Exception e = null;
        switch (state) {
        case NOT_CONNECTED:
            // FIXME: fix SarosNet if no ERROR is reported !!!
            e = sarosNet.getConnectionError();
            if (e != null
                && e.toString().equalsIgnoreCase("stream:error (text)")) {
                // the same user logged in via xmpp on another server/host
                SarosView.showNotification("XMPP Connection lost",
                    Messages.ConnectionStateComposite_remote_login_warning);
            }
            return Messages.ConnectionStateComposite_not_connected;
        case CONNECTING:
            return Messages.ConnectionStateComposite_connecting;
        case CONNECTED:
            JID jid = new JID(sarosNet.getConnection().getUser());
            String displayText = jid.getBase()
                + Messages.ConnectionStateComposite_connected;
            return displayText;
        case DISCONNECTING:
            return Messages.ConnectionStateComposite_disconnecting;
        case ERROR:
            e = sarosNet.getConnectionError();
            if (e == null) {
                return Messages.ConnectionStateComposite_error;
            } else if (e.toString().equalsIgnoreCase("stream:error (conflict)")) { //$NON-NLS-1$
                return Messages.ConnectionStateComposite_error_ressource_conflict;
            } else {
                return Messages.ConnectionStateComposite_error_connection_lost;
            }
        default:
            return Messages.ConnectionStateComposite_error_unknown;
        }
    }
}
