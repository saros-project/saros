/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This listener is responsible for updating the User presence state from the
 * roster.
 */
@Component(module = "net")
public class SarosRosterListener {

    public class Listener implements IRosterListener {
        public void entriesAdded(Collection<String> addresses) {
            // ignore
        }

        public void entriesUpdated(Collection<String> addresses) {
            // TODO Check if it affects one of our participants in a session
        }

        public void entriesDeleted(Collection<String> addresses) {
            // TODO Check if it affects one of our participants in a session
        }

        public void presenceChanged(Presence presence) {

            ISarosSession sarosSession = sarosSessionObservable.getValue();

            if (sarosSession == null)
                return;

            JID presenceJID = new JID(presence.getFrom());

            // The presenceJID can be bare if the roster entry has gone off-line
            if (presenceJID.isBareJID()) {
                // Check if there is a user in this project using the user name
                // part of the JID
                presenceJID = sarosSession.getResourceQualifiedJID(presenceJID);

                // No such user in the project
                if (presenceJID == null)
                    return;
            }

            // By now we should have returned or have a RQ-JID
            assert presenceJID.isResourceQualifiedJID();

            // Get the user (if any)
            User user = sarosSession.getUser(presenceJID);
            if (user == null)
                return; // PresenceJID does not identify a user in the project

            assert user.getJID().strictlyEquals(presenceJID);

            if (presence.isAvailable()) {
                if (user.getConnectionState() != UserConnectionState.ONLINE) {
                    user.setConnectionState(User.UserConnectionState.ONLINE);
                }
                user.setAway(presence.isAway());
            }

            if (!presence.isAvailable()
                && user.getConnectionState() != UserConnectionState.OFFLINE)
                user.setConnectionState(User.UserConnectionState.OFFLINE);
        }

        public void rosterChanged(Roster roster) {

            if (roster == null) {
                // The connection is now offline
                return;
            }

            // Update all presences
            for (RosterEntry rosterEntry : roster.getEntries()) {
                for (Presence presence : Utils.asIterable(roster
                    .getPresences(rosterEntry.getUser()))) {
                    listener.presenceChanged(presence);
                }
            }
        }
    }

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected RosterTracker rosterTracker;

    public SarosRosterListener(RosterTracker rosterTracker) {
        rosterTracker.addRosterListener(listener);
    }

    protected IRosterListener listener = new Listener();

}