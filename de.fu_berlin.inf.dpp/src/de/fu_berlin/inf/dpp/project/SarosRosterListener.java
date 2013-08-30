/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterAdapter;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This listener is responsible for updating the User presence state from the
 * roster.
 */
/*
 * TODO this class is currently not used and should be rewritten to inform the
 * session components about changes of user nicknames etc.
 */
@Component(module = "misc")
public class SarosRosterListener extends RosterAdapter {

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Override
    public void presenceChanged(Presence presence) {

        ISarosSession sarosSession = sarosSessionObservable.getValue();

        if (sarosSession == null)
            return;

        JID presenceJID = new JID(presence.getFrom());

        // The presenceJID can be bare if the roster entry has gone off-line
        if (presenceJID.isBareJID()) {
            presenceJID = sarosSession.getResourceQualifiedJID(presenceJID);

            if (presenceJID == null)
                return;
        }

        // By now we should have returned or have a RQ-JID
        assert presenceJID.isResourceQualifiedJID();

        User user = sarosSession.getUser(presenceJID);

        if (user == null)
            return;

        assert user.getJID().strictlyEquals(presenceJID);
    }

    @Override
    public void rosterChanged(Roster roster) {

        if (roster == null)
            return;

        for (RosterEntry rosterEntry : roster.getEntries()) {
            for (Presence presence : Utils.asIterable(roster
                .getPresences(rosterEntry.getUser()))) {
                presenceChanged(presence);
            }
        }
    }

    public SarosRosterListener(RosterTracker rosterTracker) {
        rosterTracker.addRosterListener(this);
    }

}