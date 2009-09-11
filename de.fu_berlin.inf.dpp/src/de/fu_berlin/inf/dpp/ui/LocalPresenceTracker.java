package de.fu_berlin.inf.dpp.ui;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.util.DeferredValueChangeListener;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * This class is responsible of setting the presence of Saros to away if the
 * user deactivates the Eclipse window
 */
public class LocalPresenceTracker {

    private static final Logger log = Logger
        .getLogger(LocalPresenceTracker.class);

    protected Saros saros;

    protected XMPPConnection connection = null;

    boolean active = true;

    public LocalPresenceTracker(Saros saros) {

        saros.addListener(new IConnectionListener() {

            public void connectionStateChanged(XMPPConnection connection,
                ConnectionState newState) {

                if (newState == ConnectionState.CONNECTED)
                    setConnection(connection);
                else
                    setConnection(null);
            }
        });

        IWorkbench bench = PlatformUI.getWorkbench();

        if (bench == null) {
            log.error("Could not get IWorkbench!");
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
                Util.wrapSafe(log, new Runnable() {
                    public void run() {
                        log.debug("Eclipse window active: " + active);

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

    protected synchronized void setConnection(XMPPConnection connection) {
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
            presence.setStatus("Eclipse window is active");
        } else {
            presence.setMode(Presence.Mode.away);
            presence.setStatus("Eclipse window is in the background");
        }
        connection.sendPacket(presence);
    }

}
