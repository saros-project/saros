/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.Collection;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.util.StackTrace;

public class DispatchingRosterListener implements IRosterListener {

    private static final Logger log = Logger
        .getLogger(DispatchingRosterListener.class);

    protected TreeSet<IRosterListener> listeners = new TreeSet<IRosterListener>(
        new PrecedenceRosterListenerComparator());

    /**
     * Currently executed method
     */
    protected String concurrentModificationChecker;

    protected synchronized void enter(String methodName) {
        if (concurrentModificationChecker != null)
            log.warn("Threading violation: About to enter " + methodName
                + ", but still in " + concurrentModificationChecker,
                new StackTrace());
        else
            concurrentModificationChecker = methodName;
    }

    protected synchronized void leave(String methodName) {
        if (!concurrentModificationChecker.equals(methodName))
            log.warn("Threading violation: About to leave " + methodName
                + ", but other method currently being processed "
                + concurrentModificationChecker, new StackTrace());
        else
            concurrentModificationChecker = null;
    }

    public void entriesAdded(Collection<String> addresses) {
        enter("entriesAdded");
        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesAdded(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
        leave("entriesAdded");
    }

    public void entriesUpdated(Collection<String> addresses) {
        enter("entriesUpdated");
        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesUpdated(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
        leave("entriesUpdated");
    }

    public void entriesDeleted(Collection<String> addresses) {
        enter("entriesDeleted");
        for (IRosterListener listener : this.listeners) {
            try {
                listener.entriesDeleted(addresses);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
        leave("entriesDeleted");
    }

    public void presenceChanged(Presence presence) {
        enter("presenceChanged");
        for (IRosterListener listener : this.listeners) {
            try {
                listener.presenceChanged(presence);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
        leave("presenceChanged");
    }

    public void rosterChanged(Roster roster) {
        enter("rosterChanged");
        for (IRosterListener listener : this.listeners) {
            try {
                listener.rosterChanged(roster);
            } catch (RuntimeException e) {
                RosterTracker.log.error("Internal error:", e);
            }
        }
        leave("rosterChanged");
    }

    public void add(IRosterListener rosterListener) {
        enter("add");
        if (!listeners.contains(rosterListener)) {
            listeners.add(rosterListener);
        }
        leave("add");
    }

    public void remove(IRosterListener rosterListener) {
        enter("remove");
        listeners.remove(rosterListener);
        leave("remove");
    }
}