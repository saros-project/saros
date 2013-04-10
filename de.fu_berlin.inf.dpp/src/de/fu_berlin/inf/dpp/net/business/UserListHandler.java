package de.fu_berlin.inf.dpp.net.business;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension.UserListEntry;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListReceivedExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;

/**
 * Business Logic for handling Invitation requests
 */

// FIXME move into session scope
@Component(module = "net")
public class UserListHandler {

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    @Inject
    private ITransmitter transmitter;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private SessionIDObservable sessionID;

    public UserListHandler(IReceiver receiver) {
        // TODO SessionID-Filter
        receiver.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());

                log.debug("received user list from " + fromJID);

                UserListExtension userListInfo = UserListExtension.PROVIDER
                    .getPayload(packet);

                if (userListInfo == null) {
                    log.warn("user list payload is corrupted");
                    return;
                }

                ISarosSession sarosSession = sessionManager.getSarosSession();
                assert sarosSession != null;

                User fromUser = sarosSession.getUser(fromJID);

                if (fromUser == null) {
                    log.warn("received user list from " + fromJID
                        + " who is not part of the current session");
                    return;
                }

                // Adding new users
                User newUser;
                for (UserListEntry userEntry : userListInfo.userList) {

                    // Check if we already know this user
                    User user = sarosSession.getUser(userEntry.jid);

                    // new session user
                    if (user == null) {

                        newUser = new User(sarosSession, userEntry.jid,
                            userEntry.colorID, userEntry.favoriteColorID);

                        newUser.setPermission(userEntry.permission);
                        sarosSession.addUser(newUser);
                    } else {
                        // User already exists

                        // Update his permission
                        user.setPermission(userEntry.permission);
                    }
                }
                sendUserListConfirmation(fromJID);
            }

        }, UserListExtension.PROVIDER.getPacketFilter());
    }

    private void sendUserListConfirmation(JID to) {
        log.debug("sending user list received confirmation to " + to);
        try {
            transmitter
                .sendToSessionUser(to,
                    UserListReceivedExtension.PROVIDER
                        .create(new UserListReceivedExtension(sessionID
                            .getValue())));
        } catch (IOException e) {
            log.error("failed to send user list received confirmation to: "
                + to, e);
        }
    }
}