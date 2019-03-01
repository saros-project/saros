package saros.net.xmpp.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import saros.annotations.Component;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;

/**
 * The RosterTracker is responsible for offering a convenient access for receiving roster listener
 * changes, if one is not interested in tracking whether the connection is changed.
 */
@Component(module = "net")
public class RosterTracker {

  private static final Logger LOG = Logger.getLogger(RosterTracker.class);

  private Connection connection;

  private volatile Roster roster;

  private List<IRosterListener> listeners = new CopyOnWriteArrayList<IRosterListener>();

  private final IConnectionListener connectionListener =
      new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {
          if (state == ConnectionState.CONNECTING) {
            prepareConnection(connection);
          } else if (state != ConnectionState.CONNECTED) {
            disposeConnection();
          }
        }
      };

  private final IRosterListener forwarder =
      new IRosterListener() {

        @Override
        public void entriesAdded(Collection<String> addresses) {
          forwardEntriesAdded(addresses);
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
          forwardEntriesDeleted(addresses);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
          forwardEntriesUpdated(addresses);
        }

        @Override
        public void presenceChanged(Presence presence) {
          forwardPresenceChanged(presence);
        }

        @Override
        public void rosterChanged(Roster roster) {
          forwardRosterChanged(roster);
        }
      };

  public RosterTracker(XMPPConnectionService connectionService) {
    connectionService.addListener(connectionListener);
  }

  /**
   * Adds a listener to this roster. The listener will be fired anytime one or more changes to the
   * roster are pushed from the server.
   *
   * @param rosterListener a roster listener.
   */
  public void addRosterListener(IRosterListener rosterListener) {
    listeners.add(rosterListener);
  }

  /**
   * Removes a listener from this roster. The listener will be fired anytime one or more changes to
   * the roster are pushed from the server.
   *
   * @param rosterListener a roster listener.
   */
  public void removeRosterListener(IRosterListener rosterListener) {
    listeners.remove(rosterListener);
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
   * Returns all currently known online presences associated with a JID or an unavailable presence
   * if the user is not online or an empty list if no roster is available.
   */
  public List<Presence> getPresences(JID from) {
    if (from == null) throw new IllegalArgumentException("JID cannot be null");

    final Roster currentRoster = roster;

    if (currentRoster == null) return Collections.emptyList();

    final List<Presence> presences = new ArrayList<Presence>();

    final Iterator<Presence> it = currentRoster.getPresences(from.toString());

    while (it.hasNext()) presences.add(it.next());

    return presences;
  }

  /**
   * Returns the RQ-JIDs of all presences of the given (plain) JID which are available.
   *
   * <p>An empty list is returned if no presence for the given JID is online.
   */
  public List<JID> getAvailablePresences(JID from) {

    List<JID> result = new ArrayList<JID>();

    for (Presence presence : getPresences(from)) {
      if (!presence.isAvailable()) continue;

      String rjid = presence.getFrom();
      if (rjid == null) {
        LOG.error("presence.getFrom() is null");
        continue;
      }
      result.add(new JID(rjid));
    }

    return result;
  }

  private void prepareConnection(Connection connection) {
    this.connection = connection;
    setRoster(this.connection.getRoster());
  }

  private void disposeConnection() {
    if (this.connection != null) setRoster(null);

    this.connection = null;
  }

  private void setRoster(Roster roster) {

    if (this.roster != null) this.roster.removeRosterListener(forwarder);

    if (roster != null) roster.addRosterListener(forwarder);

    this.roster = roster;

    forwarder.rosterChanged(this.roster);
  }

  private void forwardEntriesAdded(Collection<String> addresses) {
    for (IRosterListener listener : listeners) {
      try {
        listener.entriesAdded(addresses);
      } catch (RuntimeException e) {
        LOG.error("invoking listener: " + listener + " failed", e);
      }
    }
  }

  private void forwardEntriesDeleted(Collection<String> addresses) {
    for (IRosterListener listener : listeners) {
      try {
        listener.entriesDeleted(addresses);
      } catch (RuntimeException e) {
        LOG.error("invoking listener: " + listener + " failed", e);
      }
    }
  }

  private void forwardEntriesUpdated(Collection<String> addresses) {
    for (IRosterListener listener : this.listeners) {
      try {
        listener.entriesUpdated(addresses);
      } catch (RuntimeException e) {
        LOG.error("invoking listener: " + listener + " failed", e);
      }
    }
  }

  private void forwardPresenceChanged(Presence presence) {
    for (IRosterListener listener : listeners) {
      try {
        listener.presenceChanged(presence);
      } catch (RuntimeException e) {
        LOG.error("invoking listener: " + listener + " failed", e);
      }
    }
  }

  private void forwardRosterChanged(Roster roster) {
    for (IRosterListener listener : listeners) {
      try {
        listener.rosterChanged(roster);
      } catch (RuntimeException e) {
        LOG.error("invoking listener: " + listener + " failed", e);
      }
    }
  }
}
