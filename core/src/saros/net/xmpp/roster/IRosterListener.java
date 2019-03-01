package de.fu_berlin.inf.dpp.net.xmpp.roster;

import java.util.Collection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

public interface IRosterListener extends RosterListener {

  @Override
  public default void entriesAdded(Collection<String> addresses) {
    // NOP
  }

  @Override
  public default void entriesUpdated(Collection<String> addresses) {
    // NOP
  }

  @Override
  public default void entriesDeleted(Collection<String> addresses) {
    // NOP
  }

  @Override
  public default void presenceChanged(Presence presence) {
    // NOP
  }

  public default void rosterChanged(Roster roster) {
    // NOP
  }
}
