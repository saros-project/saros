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

public class ConnectDisconnectAction extends Action implements
        IConnectionListener {

    private final IPropertyChangeListener propertyListener;

    public ConnectDisconnectAction() {
        updateStatus();
        Saros.getDefault().addListener(this);

        this.propertyListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(PreferenceConstants.USERNAME)) {
                    updateStatus();
                }
            }
        };
        Saros.getDefault().getPreferenceStore().addPropertyChangeListener(
                this.propertyListener);
    }

    @Override
    public void run() {
        new Thread(new Runnable() {
            public void run() {
                Saros saros = Saros.getDefault();
                if (saros.isConnected()) {
                    saros.disconnect();
                } else {

                    // display task progress information (begin) in status line
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            IStatusLineManager slm = getStatusmanager();
                            IProgressMonitor monitor = slm.getProgressMonitor();
                            monitor.beginTask("Connecting...",
                                    IProgressMonitor.UNKNOWN);
                        }
                    });

                    saros.connect();

                    // display task progress information (end) in status line
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            IStatusLineManager slm = getStatusmanager();
                            slm.setMessage("Connecting..");
                            IProgressMonitor monitor = slm.getProgressMonitor();
                            monitor.done();
                        }
                    });

                }
            }

            private IStatusLineManager getStatusmanager() {
                return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().getViewReferences()[0].getView(false)
                        .getViewSite().getActionBars().getStatusLineManager();
            }
        }).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState) {
        updateStatus();
    }

    private void updateStatus() {
        ConnectionState state = Saros.getDefault().getConnectionState();

        switch (state) {
        case CONNECTED:
        case CONNECTING:
            setImageDescriptor(SarosUI.getImageDescriptor("/icons/connect.png"));
            break;

        case ERROR:
        case NOT_CONNECTED:
        case DISCONNECTING:
            setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
            break;
        }

        String username = Saros.getDefault().getPreferenceStore().getString(
                PreferenceConstants.USERNAME);

        setEnabled((state == ConnectionState.CONNECTED)
                || (((state == ConnectionState.NOT_CONNECTED) || (state == ConnectionState.ERROR)) && ((username != null) && (username
                        .length() > 0))));
        updateText();
    }

    private void updateText() {
        ConnectionState state = Saros.getDefault().getConnectionState();
        String text = SarosUI.getDescription(state);

        setText(text);
    }
}
