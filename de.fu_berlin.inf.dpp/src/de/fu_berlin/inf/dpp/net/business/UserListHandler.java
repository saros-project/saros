package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension.UserListEntry;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Business Logic for handling Invitation requests
 */
@Component(module = "net")
public class UserListHandler {

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    @Inject
    private XMPPTransmitter transmitter;

    @Inject
    private ISarosSessionManager sessionManager;

    public UserListHandler(IReceiver receiver) {
        // TODO SessionID-Filter
        receiver.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());

                log.debug("Inv" + Utils.prefix(fromJID) + ": Received userList");
                UserListExtension userListInfo = UserListExtension.PROVIDER
                    .getPayload(packet);

                if (userListInfo == null) {
                    log.warn("Inv" + Utils.prefix(fromJID)
                        + ": The received userList packet's"
                        + " payload is null.");
                    return;
                }

                ISarosSession sarosSession = sessionManager.getSarosSession();
                assert sarosSession != null;

                User fromUser = sarosSession.getUser(fromJID);

                if (fromUser == null) {
                    log.error("Received userList from buddy who "
                        + "is not part of our session: "
                        + Utils.prefix(fromJID));
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
                            userEntry.colorID);

                        newUser.setPermission(userEntry.permission);
                        sarosSession.addUser(newUser);
                    } else {
                        // User already exists

                        // Update his permission
                        user.setPermission(userEntry.permission);
                    }
                }
                transmitter.sendUserListConfirmation(fromJID);
            }

        }, UserListExtension.PROVIDER.getPacketFilter());
    }
}