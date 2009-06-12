package de.fu_berlin.inf.dpp.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The RosterTracker is responsible for offering an convenient access for
 * receiving roster listener changes, if one is not interested in tracking
 * whether the connection is changed.
 */
@Component(module = "net")
public class RosterTracker implements ConnectionSessionListener {

    private static final Logger log = Logger.getLogger(RosterTracker.class
        .getName());

    protected List<RosterListener> rosterListeners = new ArrayList<RosterListener>();

    protected XMPPConnection connection;

    protected Roster roster;

    protected RosterListener rosterListener = new RosterListener() {
        public void entriesAdded(Collection<String> addresses) {
            try {
                for (RosterListener listener : rosterListeners) {
                    listener.entriesAdded(addresses);
                }
            } catch (RuntimeException e) {
                log.error("Internal error:", e);
            }
        }

        public void entriesUpdated(Collection<String> addresses) {
            try {
                for (RosterListener listener : rosterListeners) {
                    listener.entriesUpdated(addresses);
                }
            } catch (RuntimeException e) {
                log.error("Internal error:", e);
            }
        }

        public void entriesDeleted(Collection<String> addresses) {
            try {
                for (RosterListener listener : rosterListeners) {
                    listener.entriesDeleted(addresses);
                }
            } catch (RuntimeException e) {
                log.error("Internal error:", e);
            }
        }

        public void presenceChanged(Presence presence) {
            try {
                for (RosterListener listener : rosterListeners) {
                    listener.presenceChanged(presence);
                }
            } catch (RuntimeException e) {
                log.error("Internal error:", e);
            }
        }
    };

    /**
     * Adds a listener to this roster. The listener will be fired anytime one or
     * more changes to the roster are pushed from the server.
     * 
     * @param rosterListener
     *            a roster listener.
     */
    public void addRosterListener(RosterListener rosterListener) {
        if (!rosterListeners.contains(rosterListener)) {
            rosterListeners.add(rosterListener);
        }
    }

    /**
     * Removes a listener from this roster. The listener will be fired anytime
     * one or more changes to the roster are pushed from the server.
     * 
     * @param rosterListener
     *            a roster listener.
     */
    public void removeRosterListener(RosterListener rosterListener) {
        rosterListeners.remove(rosterListener);
    }

    public void disposeConnection() {
        if (this.connection == null) {
            log.warn("DisposeConnection called without "
                + "previous call to prepare");
            return;
        }
        this.connection = null;
    }

    public void prepareConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public void startConnection() {

        if (this.connection == null) {
            log
                .error("StartConnection called without previous call to prepare");
            return;
        }
        if (this.roster != null) {
            log.warn("StartConnection called without previous call to stop");
            stopConnection();
        }
        this.roster = this.connection.getRoster();
        // TODO This is too late, we might miss Roster events... reload?
        this.roster.addRosterListener(rosterListener);
    }

    public void stopConnection() {
        if (this.connection == null) {
            log.warn("StopConnection called without previous call to prepare");
        }
        if (this.roster == null) {
            log.warn("StopConnection called without previous call to start");
            return;
        }

        this.roster.removeRosterListener(rosterListener);
        this.roster = null;
    }

    /**
     * Returns all currently known online presences associated with a JID or an
     * unavailable presence if the user is not online or an empty list if no
     * roster is available.
     */
    public Iterable<Presence> getPresences(JID from) {
        if (from == null)
            throw new IllegalArgumentException("JID cannot be null");

        if (roster == null)
            return Collections.emptyList();

        return Util.asIterable(roster.getPresences(from.toString()));
    }

    /**
     * Returns the RQ-JIDs of all presences of the given (plain) 
     * JID which are available.
     * 
     * An empty list is returned if no presence for the given JID is online.
     */
    public Iterable<JID> getAvailablePresences(JID from) {
        if (from == null)
            throw new IllegalArgumentException("JID cannot be null");

        if (roster == null)
            return Collections.emptyList();

        List<JID> result = new ArrayList<JID>(10);
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
