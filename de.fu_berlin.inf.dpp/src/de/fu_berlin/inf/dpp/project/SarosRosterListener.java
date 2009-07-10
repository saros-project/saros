/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This listener is responsible for updating the User presence state from the
 * roster.
 */
@Component(module = "net")
public class SarosRosterListener implements ConnectionSessionListener {

    @Inject
    protected SharedProjectObservable currentlySharedProject;

    protected RosterListener listener = new RosterListener() {
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

            SharedProject project = currentlySharedProject.getValue();

            if (project == null)
                return;

            JID presenceJID = new JID(presence.getFrom());

            // The presenceJID can be bare if the roster entry has gone off-line
            if (presenceJID.isBareJID()) {
                // Check if there is a user in this project using the user name
                // part of the JID
                presenceJID = project.getResourceQualifiedJID(presenceJID);

                // No such user in the project
                if (presenceJID == null)
                    return;
            }

            // By now we should have returned or have a RQ-JID
            assert presenceJID.isResourceQualifiedJID();

            // Get the user (if any)
            User user = project.getUser(presenceJID);
            if (user == null)
                return; // PresenceJID does not identify a user in the project

            assert user.getJID().strictlyEquals(presenceJID);

            if (presence.isAvailable()
                && user.getPresence() != UserConnectionState.ONLINE)
                user.setPresence(User.UserConnectionState.ONLINE);

            if (!presence.isAvailable()
                && user.getPresence() != UserConnectionState.OFFLINE)
                user.setPresence(User.UserConnectionState.OFFLINE);
        }
    };

    protected XMPPConnection connection;

    protected Roster roster;

    protected void setRoster(Roster newRoster) {

        // Unregister from current roster (if set)
        if (this.roster != null) {
            this.roster.removeRosterListener(listener);
        }

        this.roster = newRoster;

        // Register to new roster (if set)
        if (this.roster != null) {
            this.roster.addRosterListener(listener);

            // Update all presences
            for (RosterEntry rosterEntry : roster.getEntries()) {
                for (Presence presence : Util.asIterable(roster
                    .getPresences(rosterEntry.getUser()))) {
                    listener.presenceChanged(presence);
                }
            }
        }
    }

    public void startConnection() {
        if (connection != null) {

            Roster roster = connection.getRoster();
            setRoster(roster);
        }
    }

    public void stopConnection() {
        setRoster(null);
    }

    public void disposeConnection() {
        connection = null;
    }

    public void prepareConnection(XMPPConnection connection) {
        this.connection = connection;
    }

}