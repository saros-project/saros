/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.IXMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This class is responsible for parsing and processing a user list sent to us.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class UserListHandler extends UserListExtension {

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensionUtils
            .getFromHostFilter(sessionManager));
    }

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    @Inject
    protected IXMPPTransmitter transmitter;

    protected SessionManager sessionManager;

    public UserListHandler(SessionManager sessionManager,
        XMPPChatReceiver receiver) {
        this.sessionManager = sessionManager;
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public void userListReceived(JID fromJID, List<UserListEntry> userList) {

        ISharedProject project = sessionManager.getSharedProject();

        assert project != null;
        assert project.getHost().getJID().equals(fromJID);

        log.debug("Received user list");

        for (UserListEntry receivedUser : userList) {

            // Check if we already know this user
            User user = project.getParticipant(receivedUser.getJID());

            if (user == null) {
                // This user is not part of our project
                user = new User(receivedUser.getJID(), receivedUser
                    .getColorID());

                // Add him and send him a message, and tell him our
                // color
                project.addUser(user);

                // TODO This needs to be
                transmitter.sendMessage(user.getJID(), JoinExtension
                    .getDefault().create(project.getLocalUser().getColorID()));
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