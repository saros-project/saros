package de.fu_berlin.inf.dpp.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * The RosterTracker is responsible for offering a convenient access for
 * receiving roster listener changes, if one is not interested in tracking
 * whether the connection is changed.
 */
@Component(module = "net")
public class RosterTracker implements IConnectionListener {

    static final Logger log = Logger.getLogger(RosterTracker.class);

    private Connection connection;

    private volatile Roster roster;

    private DispatchingRosterListener listener = new DispatchingRosterListener();

    public RosterTracker(XMPPConnectionService connectionService) {
        connectionService.addListener(this);
    }

    /**
     * Adds a listener to this roster. The listener will be fired anytime one or
     * more changes to the roster are pushed from the server.
     * 
     * @param rosterListener
     *            a roster listener.
     */
    public void addRosterListener(IRosterListener rosterListener) {
        listener.add(rosterListener);
    }

    /**
     * Removes a listener from this roster. The listener will be fired anytime
     * one or more changes to the roster are pushed from the server.
     * 
     * @param rosterListener
     *            a roster listener.
     */
    public void removeRosterListener(IRosterListener rosterListener) {
        listener.remove(rosterListener);
    }

    private void prepareConnection(Connection connection) {
        this.connection = connection;
        setRoster(this.connection.getRoster());
    }

    private void disposeConnection() {
        setRoster(null);
        this.connection = null;
    }

    @Override
    public void connectionStateChanged(Connection connection,
        ConnectionState state) {
        if (state == ConnectionState.CONNECTING) {
            prepareConnection(connection);
        } else if (this.connection != null
            && state != ConnectionState.CONNECTED) {
            disposeConnection();
        }
    }

    private void setRoster(Roster roster) {

        if (this.roster != null)
            this.roster.removeRosterListener(listener);

        if (roster != null)
            roster.addRosterListener(listener);

        this.roster = roster;

        listener.rosterChanged(this.roster);
    }

    /**
     * Returns the roster that this tracker is currently using.
     * 
     * @return the current roster or <code>null</code> if no roster is available
     */
    public Roster getRoster() {
        return roster;
    }

    /**
     * Returns all currently known online presences associated with a JID or an
     * unavailable presence if the user is not online or an empty list if no
     * roster is available.
     */
    public List<Presence> getPresences(JID from) {
        if (from == null)
            throw new IllegalArgumentException("JID cannot be null");

        final Roster currentRoster = roster;

        if (currentRoster == null)
            return Collections.emptyList();

        final List<Presence> presences = new ArrayList<Presence>();

        final Iterator<Presence> it = currentRoster.getPresences(from
            .toString());

        while (it.hasNext())
            presences.add(it.next());

        return presences;
    }

    /**
     * Returns the RQ-JIDs of all presences of the given (plain) JID which are
     * available.
     * 
     * An empty list is returned if no presence for the given JID is online.
     */
    public List<JID> getAvailablePresences(JID from) {

        List<JID> result = new ArrayList<JID>();

        for (Presence presence : getPresences(from)) {
            if (!presence.isAvailable())
                continue;

            String rjid = presence.getFrom();
            if (rjid == null) {
                log.error("presence.getFrom() is null");
                continue;
            }
            result.add(new JID(rjid));
        }

        return result;
    }
}
