package de.fu_berlin.inf.dpp.net.xmpp.roster;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;

public interface IRosterListener extends RosterListener {

  public void rosterChanged(Roster roster);
}
