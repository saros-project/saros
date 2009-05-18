/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;

/**
 * This listener is responsible for updating the User presence state from the
 * roster.
 */
@Component(module = "net")
public class SarosRosterListener implements ConnectionSessionListener {

    @Inject
    protected SharedProjectObservable currentlySharedProject;

    RosterListener listener = new RosterListener() {
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

            if (project == null) {
                return;
            }

            // Update the presence on the User
            User user = project.getParticipant(new JID(presence.getFrom()));
            if (user != null) {
                if (presence.isAvailable()) {
                    user.setPresence(User.UserConnectionState.ONLINE);
                } else {
                    user.setPresence(User.UserConnectionState.OFFLINE);
                }
            }
        }
    };

    public void dispose() {
        connection = null;
    }

    public void prepare(XMPPConnection connection) {
        this.connection = connection;
    }

    XMPPConnection connection;

    Roster roster;

    public void registerListener(Roster roster) {

        if (this.roster != null) {
            this.roster.removeRosterListener(listener);
        }
        this.roster = roster;
        if (this.roster != null) {
            this.roster.addRosterListener(listener);
        }
    }

    public void start() {
        if (connection != null) {
            registerListener(connection.getRoster());
        }
    }

    public void stop() {
        registerListener(null);
        connection = null;
    }

}