package de.fu_berlin.inf.dpp.net.business;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.Startable;

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

/**
 * Business Logic for handling Invitation requests
 */

// FIMXE move to *project.internal package
// FIMXE this component uses the network but is not a net component !
@Component(module = "net")
public class UserListHandler implements Startable {

    private static final Logger log = Logger.getLogger(UserListHandler.class
        .getName());

    private final ITransmitter transmitter;

    private final IReceiver receiver;

    private final ISarosSession session;

    private final SessionIDObservable sessionID;

    private String currentSessionID;

    private final PacketListener userListListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            handleUserListUpdate(packet);
        }
    };

    public UserListHandler(ISarosSession session,
        SessionIDObservable sessionID, ITransmitter transmitter,
        IReceiver receiver) {
        this.session = session;
        this.sessionID = sessionID;
        this.transmitter = transmitter;
        this.receiver = receiver;
    }

    @Override
    public void start() {
        currentSessionID = sessionID.getValue();
        /*
         * FIXME: add the session ID to the filter so we do not need to handle
         * it in handeUserListUpdate Should be done when there are major changes
         * made to the network layer as this change would cause an
         * incompatibility with older versions.
         */
        receiver.addPacketListener(userListListener,
            UserListExtension.PROVIDER.getPacketFilter());
    }

    @Override
    public void stop() {
        receiver.removePacketListener(userListListener);
    }

    private void handleUserListUpdate(Packet packet) {
        JID fromJID = new JID(packet.getFrom());

        log.debug("received user list from " + fromJID);

        UserListExtension userListInfo = UserListExtension.PROVIDER
            .getPayload(packet);

        if (userListInfo == null) {
            log.warn("user list payload is corrupted");
            return;
        }

        User fromUser = session.getUser(fromJID);

        if (!currentSessionID.equals(userListInfo.getSessionID())
            || fromUser == null) {
            log.warn("received user list from " + fromJID
                + " who is not part of the current session");
            return;
        }

        /*
         * TODO: the host should be able to send user lists which will contain
         * users that currently left the session.
         * 
         * Another reason would be: Carls network crashes ... Alice detects this
         * and would send a "Carl removed from session message" to all other
         * session users.
         */
        for (UserListEntry userEntry : userListInfo.userList) {

            User user = session.getUser(userEntry.jid);

            // new session user
            if (user == null) {

                user = new User(session, userEntry.jid, userEntry.colorID,
                    userEntry.favoriteColorID);

                user.setPermission(userEntry.permission);
                session.addUser(user);
            } else {
                // existing session user
                user.setPermission(userEntry.permission);
            }
        }

        sendUserListConfirmation(fromJID);
    }

    private void sendUserListConfirmation(JID to) {
        log.debug("sending user list received confirmation to " + to);
        try {
            transmitter.sendToSessionUser(to,
                UserListReceivedExtension.PROVIDER
                    .create(new UserListReceivedExtension(currentSessionID)));
        } catch (IOException e) {
            log.error("failed to send user list received confirmation to: "
                + to, e);
        }
    }
}