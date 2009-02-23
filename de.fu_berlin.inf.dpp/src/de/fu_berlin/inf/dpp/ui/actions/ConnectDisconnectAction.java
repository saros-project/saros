/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class ConnectDisconnectAction extends Action {

    private static final Logger log = Logger
        .getLogger(ConnectDisconnectAction.class.getName());

    public ConnectDisconnectAction() {
        updateStatus();

        Saros.getDefault().addListener(new IConnectionListener() {
            public void connectionStateChanged(XMPPConnection connection,
                ConnectionState newState) {
                updateStatus();
            }
        });

        Saros.getDefault().getPreferenceStore().addPropertyChangeListener(
            new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getProperty()
                        .equals(PreferenceConstants.USERNAME)) {
                        updateStatus();
                    }
                }
            });

    }

    @Override
    public void run() {

        Util.runSafeAsync("ConnectDisconnectAction-", log, new Runnable() {
            public void run() {
                runConnectDisconnect();
            }
        });
    }

    protected IStatusLineManager getStatusmanager() {
        // TODO check for NPE
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage().getViewReferences()[0].getView(false)
            .getViewSite().getActionBars().getStatusLineManager();
    }

    public void updateStatus() {
        try {
            ConnectionState state = Saros.getDefault().getConnectionState();

            log.debug("New State == " + state);

            switch (state) {
            case CONNECTED:
            case CONNECTING:
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/connect.png"));
                break;

            case ERROR:
            case NOT_CONNECTED:
            case DISCONNECTING:
            default:
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            }

            String username = Saros.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.USERNAME);

            boolean validUsername = (username != null)
                && (username.length() > 0);
            boolean canConnect = validUsername
                && (state == ConnectionState.NOT_CONNECTED || state == ConnectionState.ERROR);
            setEnabled(state == ConnectionState.CONNECTED || canConnect);

            setText(SarosUI.getDescription(state));
        } catch (RuntimeException e) {
            log.error("Internal error in ConnectDisconnectAction:", e);
        }
    }

    protected void runConnectDisconnect() {
        try {
            Saros saros = Saros.getDefault();
            if (saros.isConnected()) {
                saros.disconnect();
            } else {
                // display task progress information (begin) in status
                // line
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        IStatusLineManager slm = getStatusmanager();
                        IProgressMonitor monitor = slm.getProgressMonitor();
                        monitor.beginTask("Connecting...",
                            IProgressMonitor.UNKNOWN);
                    }
                });

                saros.connect();

                // display task progress information (end) in status
                // line
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        IStatusLineManager slm = getStatusmanager();
                        slm.setMessage("Connecting..");
                        IProgressMonitor monitor = slm.getProgressMonitor();
                        monitor.done();
                    }
                });

            }
        } catch (RuntimeException e) {
            log.error("Internal error in ConnectDisconnectAction:", e);
        }
    }
}
