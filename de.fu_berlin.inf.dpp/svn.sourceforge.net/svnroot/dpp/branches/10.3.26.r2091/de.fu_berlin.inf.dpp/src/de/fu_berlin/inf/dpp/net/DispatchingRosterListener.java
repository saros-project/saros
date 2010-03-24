/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.Collection;
import java.util.TreeSet;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

public class DispatchingRosterListener implements IRosterListener {

    protected TreeSet<IRosterListener> listeners = new TreeSet<IRosterListener>(
        new PrecedenceRosterListenerComparator());

    public void entriesAdded(Collection<String> addresses) {

        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesAdded(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }

        }
    }

    public void entriesUpdated(Collection<String> addresses) {

        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesUpdated(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }

    }

    public void entriesDeleted(Collection<String> addresses) {

        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesDeleted(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }

    }

    public void presenceChanged(Presence presence) {

        for (IRosterListener listener : this.listeners) {
            try {
                listener.presenceChanged(presence);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }

    }

    public void rosterChanged(Roster roster) {
        for (IRosterListener listener : this.listeners) {
            try {
                listener.rosterChanged(roster);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
    }

    public void add(IRosterListener rosterListener) {
        if (!listeners.contains(rosterListener)) {
            listeners.add(rosterListener);
        }
    }

    public void remove(IRosterListener rosterListener) {
        listeners.remove(rosterListener);
    }
}