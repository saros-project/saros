package de.fu_berlin.inf.dpp.project.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserFinishedProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension.UserListEntry;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListReceivedExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Business Logic for receiving and sending updates of the invitation state of
 * users. Also handles sending and responding to userLists after when a user
 * joined the session
 */

@Component(module = "core")
public class UserInformationHandler implements Startable {

    private static final Logger log = Logger
        .getLogger(UserInformationHandler.class.getName());

    private static final long USER_LIST_SYNCHRONIZE_TIMEOUT = 10000L;

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

    private final PacketListener userFinishedProjectNegotiations = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            handleUserFinishedProjectNegotiationPacket(packet);
        }
    };

    public UserInformationHandler(ISarosSession session,
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

        receiver.addPacketListener(userFinishedProjectNegotiations,
            UserFinishedProjectNegotiationExtension.PROVIDER.getPacketFilter());
    }

    @Override
    public void stop() {
        receiver.removePacketListener(userListListener);
    }

    /**
     * Synchronizes a user list with the given remote users.
     * 
     * @param userList
     *            collection containing the users to update
     * 
     * @param remoteUsers
     *            the users that will receive the user list
     * 
     * @return a list of users that did not reply when synchronizing the user
     *         list
     * 
     * @throws IllegalStateException
     *             if the local user of the session is not the host
     */
    public List<User> synchronizeUserList(Collection<User> userList,
        Collection<User> remoteUsers) {

        List<User> notReplied = new ArrayList<User>();
        List<User> awaitReply = new ArrayList<User>(remoteUsers);

        if (!session.isHost())
            throw new IllegalStateException(
                "only the host can synchronize the user list");

        SarosPacketCollector collector = receiver
            .createCollector(UserListReceivedExtension.PROVIDER
                .getPacketFilter(currentSessionID));

        PacketExtension userListPacket = UserListExtension.PROVIDER
            .create(new UserListExtension(currentSessionID, userList));

        log.debug("synchronizing user list " + userList + " with user(s) "
            + remoteUsers);

        try {
            for (User user : remoteUsers) {
                try {
                    transmitter
                        .sendToSessionUser(user.getJID(), userListPacket);
                } catch (IOException e) {
                    log.error("failed to send user list to user: " + user, e);
                    notReplied.add(user);
                    awaitReply.remove(user);
                }
            }

            long synchronizeStart = System.currentTimeMillis();

            // see BUG #3544930 , the confirmation is useless

            while ((System.currentTimeMillis() - synchronizeStart) < USER_LIST_SYNCHRONIZE_TIMEOUT) {

                if (awaitReply.isEmpty())
                    break;

                Packet result = collector.nextResult(100);

                if (result == null)
                    continue;

                JID jid = new JID(result.getFrom());

                if (!remove(awaitReply, jid)) {
                    log.warn("received user list confirmation from unknown user: "
                        + jid);
                } else {
                    log.debug("received user list confirmation from: " + jid);
                }
            }

            notReplied.addAll(awaitReply);

            if (notReplied.isEmpty())
                log.debug("synchronized user list with user(s) " + remoteUsers);
            else
                log.warn("failed to synchronize user list with user(s) "
                    + notReplied);

            return notReplied;

        } finally {
            collector.cancel();
        }
    }

    /**
     * Informs all clients about the fact that a user now has projects and is
     * able to process IRessourceActivities.
     * 
     * @param remoteUsers
     *            The users to be informed
     * @param jid
     *            The JID of the user this message is about
     */
    public void sendUserFinishedProjectNegotiation(
        Collection<User> remoteUsers, JID jid) {

        PacketExtension packet = UserFinishedProjectNegotiationExtension.PROVIDER
            .create(new UserFinishedProjectNegotiationExtension(
                currentSessionID, jid));

        for (User user : remoteUsers) {
            try {
                transmitter.sendToSessionUser(user.getJID(), packet);
            } catch (IOException e) {
                log.error(
                    "failed to send userFinishedProjectNegotiation-message: "
                        + user, e);
                // TODO remove user from session
            }
        }
    }

    /**
     * Handles incoming UserHasProjects-Packets and forwards the information to
     * the session
     * 
     * @param packet
     */
    private void handleUserFinishedProjectNegotiationPacket(Packet packet) {

        JID fromJID = new JID(packet.getFrom());

        UserFinishedProjectNegotiationExtension payload = UserFinishedProjectNegotiationExtension.PROVIDER
            .getPayload(packet);

        if (payload == null) {
            log.warn("UserFinishedProjectNegotiation-payload is corrupted");
            return;
        }

        User fromUser = session.getUser(fromJID);

        if (!currentSessionID.equals(payload.getSessionID())
            || fromUser == null) {
            log.warn("received UserFinishedProjectNegotiationPacket from "
                + fromJID + " who is not part of the current session");
            return;
        }

        session.userFinishedProjectNegotiation(fromUser);
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

    private boolean remove(Collection<User> users, JID jid) {
        for (Iterator<User> it = users.iterator(); it.hasNext();) {
            User user = it.next();

            if (user.getJID().strictlyEquals(jid)) {
                it.remove();
                return true;
            }
        }

        return false;
    }
}