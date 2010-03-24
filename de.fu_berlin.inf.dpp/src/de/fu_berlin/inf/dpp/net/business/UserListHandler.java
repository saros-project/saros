package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo.JoinExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo.UserListEntry;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Business Logic for handling Invitation requests
 */
@Component(module = "net")
public class UserListHandler {

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected CancelInviteExtension cancelInviteExtension;

    @Inject
    protected SarosUI sarosUI;

    protected final SessionIDObservable sessionIDObservable;

    public UserListHandler(XMPPReceiver receiver,
        SessionIDObservable sessionIDObservablePar,
        final JoinExtensionProvider userListExtProv) {
        this.sessionIDObservable = sessionIDObservablePar;
        // TODO SessionID-Filter
        receiver.addPacketListener(new PacketListener() {

            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());

                log.debug("Inv" + Util.prefix(fromJID) + ": Received userList");
                UserListInfo userListInfo = userListExtProv.getPayload(packet);

                if (userListInfo == null) {
                    log.warn("Inv" + Util.prefix(fromJID)
                        + ": The received userList packet's"
                        + " payload is null.");
                    return;
                }

                ISharedProject sharedProject = sessionManager
                    .getSharedProject();
                assert sharedProject != null;

                User fromUser = sharedProject.getUser(fromJID);

                if (fromUser == null || !fromUser.isHost()) {
                    log.error("Received UserList from user which "
                        + "is not part of our session or is not host: "
                        + Util.prefix(fromJID));
                    return;
                }

                // Adding new users
                User newUser;
                for (UserListEntry userEntry : userListInfo.userList) {

                    // Check if we already know this user
                    User user = sharedProject.getUser(userEntry.jid);

                    if (user == null) {
                        // This user is not part of our project
                        newUser = new User(sharedProject, userEntry.jid,
                            userEntry.colorID);
                        newUser.setUserRole(userEntry.userRole);
                        if (userEntry.invitationComplete)
                            newUser.invitationCompleted();

                        // Add him and send him a message, and tell him our
                        // colour
                        sharedProject.addUser(newUser);
                    } else {
                        // User already exists

                        // Check if the existing user has the colour that we
                        // expect
                        if (user.getColorID() != userEntry.colorID) {
                            log.warn("Received color id doesn't"
                                + " match known color id");
                        }

                        // Update his role
                        user.setUserRole(userEntry.userRole);

                        // Update invitation status
                        if (userEntry.invitationComplete
                            && !user.isInvitationComplete()) {
                            sharedProject.userInvitationCompleted(user);
                        }
                    }
                }
                transmitter.sendUserListConfirmation(fromJID);
            }

        }, userListExtProv.getPacketFilter());
    }
}