package de.fu_berlin.inf.dpp.ui;

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
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * This class is responsible of setting the presence of Saros to away if the
 * user deactivates the Eclipse window
 */
@Component(module = "ui")
public class LocalPresenceTracker {

    private static final Logger log = Logger
        .getLogger(LocalPresenceTracker.class);

    private static final long DELAY_UNTIL_SET_AWAY = 5 * 60 * 1000; // 5 minutes

    private Connection connection = null;

    private boolean active = true;

    private Thread awayAnnouncer = null;

    public LocalPresenceTracker(XMPPConnectionService connectionService) {

        connectionService.addListener(new IConnectionListener() {

            @Override
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

            @Override
            public void windowOpened(IWorkbenchWindow window) {
                announceAvailable();
            }

            @Override
            public void windowDeactivated(IWorkbenchWindow window) {
                setAway(DELAY_UNTIL_SET_AWAY);
            }

            @Override
            public void windowClosed(IWorkbenchWindow window) {
                setAway(DELAY_UNTIL_SET_AWAY);
            }

            @Override
            public void windowActivated(IWorkbenchWindow window) {
                announceAvailable();
            }

        });

        if (bench.getActiveWorkbenchWindow() != null)
            announceAvailable();
        else
            setAway(DELAY_UNTIL_SET_AWAY);

    }

    private synchronized void setConnection(Connection connection) {
        this.connection = connection;
    }

    private synchronized void setAway(final long delay) {
        if ((awayAnnouncer != null && awayAnnouncer.isAlive()))
            return;

        awayAnnouncer = ThreadUtils.runSafeAsync("AwayAnnouncer", log,
            new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                        announceAway();
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
    }

    private synchronized void announceAway() {

        if (!active)
            return;

        active = false;

        if (connection == null)
            return;

        Presence presence = new Presence(Presence.Type.available);
        presence.setMode(Presence.Mode.away);
        presence.setStatus(Messages.LocalPresenceTracker_eclipse_background);

        connection.sendPacket(presence);
    }

    private synchronized void announceAvailable() {

        if (awayAnnouncer != null) {
            awayAnnouncer.interrupt();
            try {
                awayAnnouncer.join();
            } catch (InterruptedException e) {
                active = false;
                Thread.currentThread().interrupt();
                return;
            }
        }

        awayAnnouncer = null;

        if (active)
            return;

        active = true;

        if (connection == null)
            return;

        Presence presence = new Presence(Presence.Type.available);

        presence.setMode(Presence.Mode.available);
        presence.setStatus(Messages.LocalPresenceTracker_eclipse_active);
        connection.sendPacket(presence);
    }
}
