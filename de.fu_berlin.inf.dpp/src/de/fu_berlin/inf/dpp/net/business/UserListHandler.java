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
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This class is responsible for parsing and processing a user list sent to us.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class UserListHandler {

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    @Inject
    protected IXMPPTransmitter transmitter;

    @Inject
    protected JoinExtension joinExtension;

    protected SessionManager sessionManager;

    protected Handler handler;

    protected SessionIDObservable sessionIDObservable;

    public UserListHandler(SessionManager sessionManager,
        XMPPChatReceiver receiver, SessionIDObservable sessionIDObservable) {

        this.sessionIDObservable = sessionIDObservable;
        this.sessionManager = sessionManager;
        this.handler = new Handler(sessionIDObservable);

        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends UserListExtension {

        public Handler(SessionIDObservable sessionID) {
            super(sessionID);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(), PacketExtensionUtils
                .getFromHostFilter(sessionManager));
        }

        @Override
        public void userListReceived(JID fromJID, List<UserListEntry> userList) {

            ISharedProject project = sessionManager.getSharedProject();

            assert project != null;

            User fromUser = project.getParticipant(fromJID);

            if (fromUser == null || !fromUser.isHost()) {
                log.error("Received UserList from user which "
                    + "is not part of our session or is not host: " + fromJID);
                return;
            }

            log.debug("Received user list");

            for (UserListEntry receivedUser : userList) {

                // Check if we already know this user
                User user = project.getParticipant(receivedUser.getJID());

                if (user == null) {
                    // This user is not part of our project
                    user = new User(project, receivedUser.getJID(),
                        receivedUser.getColorID());

                    // Add him and send him a message, and tell him our
                    // color
                    project.addUser(user);

                    // TODO This needs to be
                    transmitter.sendMessage(user.getJID(), joinExtension
                        .create(project.getLocalUser().getColorID()));
                } else {
                    // User already exists

                    // Check if the existing user has the color that we
                    // expect
                    if (user.getColorID() != receivedUser.getColorID()) {
                        log
                            .warn("Received color id doesn't match known color id");
                    }

                    // Update his role
                    user.setUserRole(receivedUser.getUserRole());
                }
            }
        }
    }
}