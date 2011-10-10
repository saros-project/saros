package de.fu_berlin.inf.dpp.ui;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.util.DeferredValueChangeListener;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * This class is responsible of setting the presence of Saros to away if the
 * user deactivates the Eclipse window
 */
@Component(module = "ui")
public class LocalPresenceTracker {

    private static final Logger log = Logger
        .getLogger(LocalPresenceTracker.class);

    protected Connection connection = null;

    boolean active = true;

    public LocalPresenceTracker(SarosNet sarosNet) {

        sarosNet.addListener(new IConnectionListener() {

            public void connectionStateChanged(Connection connection,
                ConnectionState newState) {

                if (newState == ConnectionState.CONNECTED)
                    setConnection(connection);
                else
                    setConnection(null);
            }
        });

        IWorkbench bench;
        try {
            bench = PlatformUI.getWorkbench();
        } catch (IllegalStateException e) {
            log.warn("Workbench not found, assuming headless test"); //$NON-NLS-1$
            return;
        }
        if (bench == null) {
            log.error("Could not get IWorkbench!"); //$NON-NLS-1$
            return;
        }

        bench.addWindowListener(new IWindowListener() {

            public void windowOpened(IWorkbenchWindow window) {
                setActiveDeferred(true);
            }

            public void windowDeactivated(IWorkbenchWindow window) {
                setActiveDeferred(false);
            }

            public void windowClosed(IWorkbenchWindow window) {
                setActiveDeferred(false);
            }

            public void windowActivated(IWorkbenchWindow window) {
                setActiveDeferred(true);
            }

            protected ValueChangeListener<Boolean> windowChanges = new ValueChangeListener<Boolean>() {
                public void setValue(Boolean newValue) {
                    setActive(newValue);
                }
            };

            /**
             * Defer sending of events for 5 seconds in case the user comes back
             * quickly
             */
            protected DeferredValueChangeListener<Boolean> deferrer = new DeferredValueChangeListener<Boolean>(
                windowChanges, 5, TimeUnit.SECONDS);

            protected void setActiveDeferred(final boolean active) {
                Utils.wrapSafe(log, new Runnable() {
                    public void run() {
                        log.debug("Eclipse window now " //$NON-NLS-1$
                            + (active ? "  " : "in") + "active."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        /*
                         * Wait one second before sending an active presence
                         * update and 5 seconds for an away update.
                         */
                        deferrer.setValue(active, active ? 1 : 5,
                            TimeUnit.SECONDS);
                    }
                }).run();
            }
        });

        setActive(bench.getActiveWorkbenchWindow() != null);

    }

    protected synchronized void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected synchronized void setActive(boolean newValue) {
        if (active == newValue || connection == null) {
            return;
        }
        active = newValue;

        Presence presence = new Presence(Presence.Type.available);
        if (active) {
            presence.setMode(Presence.Mode.available);
            presence.setStatus(Messages.LocalPresenceTracker_eclipse_active);
        } else {
            presence.setMode(Presence.Mode.away);
            presence.setStatus(Messages.LocalPresenceTracker_eclipse_background);
        }
        connection.sendPacket(presence);
    }

}
