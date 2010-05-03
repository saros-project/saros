/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

public class DispatchingRosterListener implements IRosterListener {

    protected TreeSet<IRosterListener> listeners = new TreeSet<IRosterListener>(
        new PrecedenceRosterListenerComparator());

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReadLock readLock = lock.readLock();
    private WriteLock writeLock = lock.writeLock();

    public void entriesAdded(Collection<String> addresses) {
        readLock.lock();
        try {
            for (IRosterListener listener : this.listeners) {
                try {
                    listener.entriesAdded(addresses);
                } catch (RuntimeException e) {
                    RosterTracker.log.error("Internal error:", e);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void entriesUpdated(Collection<String> addresses) {
        readLock.lock();
        try {
            for (IRosterListener listener : this.listeners) {
                try {
                    listener.entriesUpdated(addresses);
                } catch (RuntimeException e) {
                    RosterTracker.log.error("Internal error:", e);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void entriesDeleted(Collection<String> addresses) {
        readLock.lock();
        try {
            for (IRosterListener listener : this.listeners) {
                try {
                    listener.entriesDeleted(addresses);
                } catch (RuntimeException e) {
                    RosterTracker.log.error("Internal error:", e);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void presenceChanged(Presence presence) {
        readLock.lock();
        try {
            for (IRosterListener listener : this.listeners) {
                try {
                    listener.presenceChanged(presence);
                } catch (RuntimeException e) {
                    RosterTracker.log.error("Internal error:", e);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void rosterChanged(Roster roster) {
        readLock.lock();
        try {
            for (IRosterListener listener : this.listeners) {
                try {
                    listener.rosterChanged(roster);
                } catch (RuntimeException e) {
                    RosterTracker.log.error("Internal error:", e);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void add(IRosterListener rosterListener) {
        writeLock.lock();
        try {
            if (!listeners.contains(rosterListener)) {
                listeners.add(rosterListener);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void remove(IRosterListener rosterListener) {
        writeLock.lock();
        try {
            listeners.remove(rosterListener);
        } finally {
            writeLock.unlock();
        }
    }
}