/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.IXMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class UserListHandler extends UserListExtension {

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensions
            .getFromHostFilter());
    }

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    protected IXMPPTransmitter transmitter;

    public UserListHandler(IXMPPTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    @Override
    public void userListReceived(JID fromJID, List<User> userList) {

        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        assert project != null;
        assert project.getHost().getJID().equals(fromJID);

        log.debug("Received user list");

        for (User receivedUser : userList) {

            // Check if we already know this user
            User user = project.getParticipant(receivedUser.getJID());

            if (user == null) {
                // This user is not part of our project
                user = receivedUser;

                // Add him and send him a message, and tell him our
                // color
                project.addUser(user);
                transmitter.sendMessage(user.getJID(), JoinExtension
                    .getDefault().create(
                        Saros.getDefault().getLocalUser().getColorID()));
            } else {
                // User already exists

                // Check if the existing user has the color that we
                // expect
                if (user.getColorID() != receivedUser.getColorID()) {
                    log.warn("Received color id doesn't match known color id");
                }

                // Update his role
                user.setUserRole(receivedUser.getUserRole());
            }
        }
    }
}