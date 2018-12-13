package de.fu_berlin.inf.dpp.ui.core_facades;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.roster.IRosterListener;
import de.fu_berlin.inf.dpp.net.xmpp.roster.RosterTracker;
import de.fu_berlin.inf.dpp.observables.ObservableValue;
import de.fu_berlin.inf.dpp.observables.ValueChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

/**
 * Bundles all backend calls to alter the currently active account's contact list. Provides a
 * {@linkplain #addListener(RosterChangeListener) listener interface} for notifications on any
 * roster changes.
 */
public class RosterFacade {

  private static final String CONNECTION_STATE_FAILURE =
      "Invalide state, connection might be lost.";

  /**
   * Shorthand name for <code>
   * ValueChangeListener&lt;List&lt;Pair&lt;RosterEntry, Presence&gt;&gt;&gt;</code>
   */
  public abstract static class RosterChangeListener
      implements ValueChangeListener<List<Pair<RosterEntry, Presence>>> {
    // just a shorthand name
  }

  /**
   * Listens to changes propagated by the {@link RosterTracker} and converts them such that
   * regardless of the roster change, a full list of all entries including their presence is made
   * available to registered {@link RosterChangeListener}s.
   */
  private class RosterTrackerListener implements IRosterListener {
    final ObservableValue<List<Pair<RosterEntry, Presence>>> observable =
        new ObservableValue<List<Pair<RosterEntry, Presence>>>(null);

    @Override
    public void entriesUpdated(Collection<String> addresses) {
      notifyListeners();
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
      notifyListeners();
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
      notifyListeners();
    }

    @Override
    public void presenceChanged(Presence presence) {
      notifyListeners();
    }

    @Override
    public void rosterChanged(Roster roster) {
      notifyListeners();
    }

    private void notifyListeners() {
      observable.setValue(getEntries());
    }

    private List<Pair<RosterEntry, Presence>> getEntries() {
      Roster roster = null;
      try {
        roster = getRoster();
      } catch (XMPPException e) {
        // No roster available, clear entries
        return new ArrayList<Pair<RosterEntry, Presence>>();
      }

      List<Pair<RosterEntry, Presence>> entries =
          new ArrayList<Pair<RosterEntry, Presence>>(roster.getEntries().size());
      /*
       * Buggish SMACK crap at its best ! The entries returned here can be
       * just plain references (see implementation) so we have to lookup
       * them correctly !
       */
      for (RosterEntry entryReference : roster.getEntries()) {
        final RosterEntry correctEntry = roster.getEntry(entryReference.getUser());

        if (correctEntry == null) continue;

        Presence presence = roster.getPresence(correctEntry.getUser());
        entries.add(Pair.of(correctEntry, presence));
      }

      return entries;
    }
  }

  private final RosterTrackerListener rosterListener = new RosterTrackerListener();

  private final XMPPConnectionService connectionService;

  /**
   * Created by PicoContainer
   *
   * @param connectionService
   * @param rosterTracker
   * @see HTMLUIContextFactory
   */
  public RosterFacade(XMPPConnectionService connectionService, RosterTracker rosterTracker) {

    this.connectionService = connectionService;
    rosterTracker.addRosterListener(rosterListener);
  }

  /**
   * Add a new listener to be notified on all changes to the roster
   *
   * @param listener
   */
  public void addListener(RosterChangeListener listener) {
    rosterListener.observable.add(listener);
  }

  /**
   * Deletes a contact from the contact list
   *
   * @param jid the JID of the contact to be deleted
   */
  public void deleteContact(JID jid) throws XMPPException {
    try {
      XMPPUtils.removeFromRoster(connectionService.getConnection(), getEntry(jid));
    } catch (IllegalStateException e) {
      throw new XMPPException(CONNECTION_STATE_FAILURE, e);
    }
  }

  /**
   * Renames a contact (given by JID)
   *
   * @param jid the JID of the contact to be renamed
   * @param name the new name of the contact
   * @throws XMPPException
   */
  public void renameContact(JID jid, String name) throws XMPPException {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    }

    try {
      getEntry(jid).setName(name);
    } catch (IllegalStateException e) {
      throw new XMPPException(CONNECTION_STATE_FAILURE, e);
    }
  }

  /**
   * Adds a contact to the contact list
   *
   * @param jid the JID of the contact to be added
   * @param nickname the nickname of the contact
   */
  public void addContact(JID jid, String nickname) throws XMPPException {
    try {
      getRoster().createEntry(jid.getBase(), nickname, null);
    } catch (IllegalStateException e) {
      throw new XMPPException(CONNECTION_STATE_FAILURE, e);
    }
  }

  /**
   * Note that all modifying methods of the returned roster instance might throw {@link
   * IllegalStateException} if the connection is lost in between operations.
   *
   * @return the roster for the currently active connection.
   * @throws XMPPException if the connection isn't established,<br>
   */
  private Roster getRoster() throws XMPPException {
    Roster roster = connectionService.getRoster();
    if (roster == null) {
      throw new XMPPException(CONNECTION_STATE_FAILURE);
    }

    return roster;
  }

  /**
   * @param jid to get the associated roster entry from
   * @return the roster entry for the given jid
   * @throws XMPPException if the connection isn't established,<br>
   *     if no entry couldn't been found
   */
  private RosterEntry getEntry(JID jid) throws XMPPException {
    RosterEntry entry = getRoster().getEntry(jid.getBase());
    if (entry == null) {
      throw new XMPPException("Couldn't find an entry for " + jid.getBareJID());
    }
    return entry;
  }
}
