/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

/**
 * Dispatches {@link IRosterListener} events to registered listeners. Listeners
 * get notified according to the order induced by
 * {@link PrecedenceRosterListenerComparator}.<br>
 * <br>
 * Concurrency: If an event is being dispatched while a listener is added or
 * removed concurrently, the listener may or may not get notified. Most notably,
 * a listener should be prepared to get notified even after it was removed from
 * a DispatchingRosterListener.
 */
public class DispatchingRosterListener implements IRosterListener {
    protected List<IRosterListener> listeners = new CopyOnWriteArrayList<IRosterListener>();
    protected Comparator<IRosterListener> listenerComparator = new PrecedenceRosterListenerComparator();

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
        // Synchronize to avoid two threads adding the same listener.
        synchronized (this) {
            if (!listeners.contains(rosterListener)) {
                // A CopyOnWriteArrayList doesn't handle sorting, so we have to
                // find the index ourselves.
                int index = 0;
                for (IRosterListener listener : this.listeners) {
                    if (listenerComparator.compare(rosterListener, listener) < 0)
                        index++;
                    else
                        break;
                }
                listeners.add(index, rosterListener);
            }
        }
    }

    public void remove(IRosterListener rosterListener) {
        listeners.remove(rosterListener);
    }
}
