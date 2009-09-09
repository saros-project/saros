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

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.ConfigurationWizard;
import de.fu_berlin.inf.dpp.util.Util;

public class ConnectDisconnectAction extends Action implements Disposable {

    private static final Logger log = Logger
        .getLogger(ConnectDisconnectAction.class.getName());

    protected IStatusLineManager statusLineManager;

    protected Saros saros;

    protected SarosUI sarosUI;

    protected StatisticManager statisticManager;

    protected ErrorLogManager errorLogManager;

    protected PreferenceUtils preferenceUtils;

    protected IConnectionListener connectionListener = new IConnectionListener() {
        public void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState) {
            updateStatus();
        }
    };

    protected IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.USERNAME)) {
                updateStatus();
            }
        }
    };

    public ConnectDisconnectAction(SarosUI sarosUI, Saros saros,
        IStatusLineManager statusLineManager,
        StatisticManager statisticManager, ErrorLogManager errorLogManager,
        PreferenceUtils preferenceUtils) {

        this.saros = saros;
        this.statusLineManager = statusLineManager;
        this.sarosUI = sarosUI;
        this.statisticManager = statisticManager;
        this.errorLogManager = errorLogManager;
        this.preferenceUtils = preferenceUtils;

        updateStatus();

        saros.addListener(connectionListener);
        saros.getPreferenceStore().addPropertyChangeListener(
            propertyChangeListener);
    }

    public void dispose() {
        saros.removeListener(connectionListener);
        saros.getPreferenceStore().removePropertyChangeListener(
            propertyChangeListener);
    }

    @Override
    public void run() {

        Util.runSafeAsync("ConnectDisconnectAction-", log, new Runnable() {
            public void run() {
                runConnectDisconnect();
            }
        });
    }

    protected void runConnectDisconnect() {
        try {
            if (saros.isConnected()) {
                saros.disconnect();
                return;
            }
            /*
             * see if we have a user name and an agreement to submitting user
             * statistics and the error log, if not, show wizard before
             * connecting
             */
            boolean hasUsername = preferenceUtils.hasUserName();
            boolean hasAgreement = statisticManager.hasStatisticAgreement()
                && errorLogManager.hasErrorLogAgreement();

            if (!hasUsername || !hasAgreement) {
                boolean ok = showConfigurationWizard(!hasUsername,
                    !hasAgreement);
                if (!ok)
                    return;
            }
            saros.connect(false);

        } catch (RuntimeException e) {
            log.error("Internal error in ConnectDisconnectAction:", e);
        }
    }

    /**
     * Opens the ConfigurationWizard to let the user specify his account
     * settings and the agreement for statistic and error log submissions.
     * 
     * @param askForAccount
     * @param askAboutStatisticTransfer
     * @return true if the user finished the wizard successfully, false if he
     *         canceled the dialog or an error occurred
     */
    protected boolean showConfigurationWizard(final boolean askForAccount,
        final boolean askAboutStatisticTransfer) {

        try {
            return Util.runSWTSync(new Callable<Boolean>() {

                public Boolean call() {
                    Wizard wiz = new ConfigurationWizard(askForAccount,
                        askAboutStatisticTransfer);
                    WizardDialog dialog = new WizardDialog(
                        EditorAPI.getShell(), wiz);
                    int status = dialog.open();
                    return (status == Window.OK);
                }

            });
        } catch (Exception e) {
            log.error("Unable to open the ConfigurationWizard", e);
            return false;
        }

    }

    protected void setStatusBar(final String message, final boolean start) {

        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {

                statusLineManager.setMessage(message);

                IProgressMonitor monitor = statusLineManager
                    .getProgressMonitor();
                if (start)
                    monitor.beginTask(message, IProgressMonitor.UNKNOWN);
                else
                    monitor.done();
            }
        });

    }

    protected void updateStatus() {
        try {
            ConnectionState state = saros.getConnectionState();

            switch (state) {
            case CONNECTED:
                String user = "";
                XMPPConnection c = saros.getConnection();
                if (c != null)
                    user = " as " + c.getUser();
                setStatusBar("Connected" + user, false);
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/connect.png"));
                break;
            case CONNECTING:
                setStatusBar("Connecting...", true);
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/connect.png"));
                break;
            case ERROR:
                setStatusBar("Error...", false);
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            case NOT_CONNECTED:
                setStatusBar("Not connected", false);
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            case DISCONNECTING:
            default:
                setStatusBar("Disconnecting...", true);
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            }

            /*
             * Enable the button, if we are in the given states, even if we do
             * not have a valid user name or statistics agreement, because we
             * show the configuration dialog in such a case
             */
            setEnabled(state == ConnectionState.CONNECTED
                || state == ConnectionState.NOT_CONNECTED
                || state == ConnectionState.ERROR);

            if (saros.isConnected()) {
                setText("Disconnect (current state is: "
                    + sarosUI.getDescription(state) + ")");
            } else {
                setText("Connect");
            }
        } catch (RuntimeException e) {
            log.error("Internal error in ConnectDisconnectAction:", e);
        }
    }

}
