/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

/**
 * Dispatches {@link IRosterListener} events to registered listeners. <br>
 * Concurrency: If an event is being dispatched while a listener is added or
 * removed concurrently, the listener may or may not get notified. Most notably,
 * a listener should be prepared to get notified even after it was removed from
 * a DispatchingRosterListener.
 */
public class DispatchingRosterListener implements IRosterListener {
    protected List<IRosterListener> listeners = new CopyOnWriteArrayList<IRosterListener>();

    @Override
    public void entriesAdded(Collection<String> addresses) {
        for (IRosterListener listener : listeners) {
            try {
                listener.entriesAdded(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        for (IRosterListener listener : listeners) {
            try {
                listener.entriesUpdated(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesDeleted(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        for (IRosterListener listener : listeners) {
            try {
                listener.presenceChanged(presence);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
    }

    @Override
    public void rosterChanged(Roster roster) {
        for (IRosterListener listener : listeners) {
            try {
                listener.rosterChanged(roster);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
    }

    public void add(IRosterListener rosterListener) {
        listeners.add(rosterListener);
    }

    public void remove(IRosterListener rosterListener) {
        listeners.remove(rosterListener);
    }
}
