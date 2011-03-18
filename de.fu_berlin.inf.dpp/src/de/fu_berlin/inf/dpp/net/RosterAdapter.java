package de.fu_berlin.inf.dpp.net;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;

public class RosterAdapter implements IRosterListener {

    public void entriesAdded(Collection<String> addresses) {
        // do nothing
    }

    public void entriesUpdated(Collection<String> addresses) {
        // do nothing
    }

    public void entriesDeleted(Collection<String> addresses) {
        // do nothing
    }

    public void presenceChanged(Presence presence) {
        // do nothing
    }

    public void rosterChanged(Roster roster) {
        // do nothing
    }

}
