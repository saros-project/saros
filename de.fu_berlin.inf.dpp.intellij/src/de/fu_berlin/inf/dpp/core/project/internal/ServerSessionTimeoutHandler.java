/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.project.internal;

import de.fu_berlin.inf.dpp.communication.extensions.PingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.PongExtension;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Component for detecting network errors on the server side of a session.
 *
 * @author srossbach
 */
public final class ServerSessionTimeoutHandler extends SessionTimeoutHandler {

    private static final Logger LOG = Logger
        .getLogger(ServerSessionTimeoutHandler.class);
    /**
     * List containing the current users of the session including their last
     * pong response time.
     */
    private final List<UserPongStatus> currentUsers = new ArrayList<UserPongStatus>();
    private final PacketListener pongPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            JID jid = new JID(packet.getFrom());
            synchronized (ServerSessionTimeoutHandler.this) {
                for (UserPongStatus status : currentUsers) {
                    if (status.user.getJID().strictlyEquals(jid))
                        status.lastPongReceivedTime = System
                            .currentTimeMillis();
                }
            }
        }
    };
    private final ISharedProjectListener sessionEventListener = new AbstractSharedProjectListener() {
        @Override
        public void userJoined(User user) {
            synchronized (ServerSessionTimeoutHandler.this) {
                if (!user.isLocal())
                    currentUsers.add(new UserPongStatus(user));
            }
        }
    };
    private Thread workerThread;
    private boolean shutdown;
    private final Runnable serverSessionTimeoutWatchdog = new Runnable() {

        @Override
        public void run() {
            while (true) {

                removeInactiveUsers();

                final long pingUserStartTime = System.currentTimeMillis();

                final List<User> usersToPing = getCurrentUsers();
                final List<User> removedUsers = new ArrayList<User>();

                for (User user : usersToPing) {
                    synchronized (ServerSessionTimeoutHandler.this) {
                        if (shutdown)
                            return;
                    }

                    try {
                        transmitter.send(ISarosSession.SESSION_CONNECTION_ID,
                            user.getJID(), PingExtension.PROVIDER
                                .create(new PingExtension(currentSessionID))
                        );
                    } catch (IOException e) {

                        removedUsers.add(user);

                        if (!user.isInSession())
                            continue;

                        LOG.error("failed to send ping to: " + user, e);
                        handleNetworkError(user.getJID(), "TxFailure");
                    }
                }

                removeUsers(removedUsers);

                long pingUserDurationTime =
                    System.currentTimeMillis() - pingUserStartTime;

                final List<User> usersToRemove = getTimedOutUsers(
                    System.currentTimeMillis(), PING_PONG_TIMEOUT);

                for (User user : usersToRemove) {
                    LOG.error("no pong received from user " + user
                        + ", reached timeout = " + PING_PONG_TIMEOUT);
                    handleNetworkError(user.getJID(), "RxFailure");

                }

                removeUsers(usersToRemove);

                if (pingUserDurationTime > PING_PONG_UPDATE_DELAY)
                    continue;

                synchronized (ServerSessionTimeoutHandler.this) {
                    if (shutdown)
                        return;

                    try {
                        ServerSessionTimeoutHandler.this
                            .wait(PING_PONG_UPDATE_DELAY);
                    } catch (InterruptedException e) {
                        if (!shutdown)
                            LOG.error("watchdog shutdown prematurly", e);

                        return;
                    }
                }
            }
        }
    };

    public ServerSessionTimeoutHandler(ISarosSession session,
        ISarosSessionManager sessionManager, ActivitySequencer sequencer,
        ITransmitter transmitter, IReceiver receiver,
        SessionIDObservable sessionID) {
        super(session, sessionManager, sequencer, transmitter, receiver);
    }

    @Override
    public void start() {

        if (!session.isHost())
            throw new IllegalStateException(
                "component cannot be started in client mode");

        super.start();

        receiver.addPacketListener(pongPacketListener,
            PongExtension.PROVIDER.getPacketFilter(currentSessionID));

        session.addListener(sessionEventListener);

        workerThread = ThreadUtils
            .runSafeAsync("ServerSessionTimeoutWatchdog", LOG,
                serverSessionTimeoutWatchdog);
    }

    @Override
    public void stop() {
        super.stop();

        receiver.removePacketListener(pongPacketListener);

        session.removeListener(sessionEventListener);

        synchronized (this) {
            shutdown = true;
            notifyAll();
        }

        try {
            workerThread.join(TIMEOUT);
        } catch (InterruptedException e) {
            LOG.warn("interrupted while waiting for " + workerThread.getName()
                + " thread to terminate");

            Thread.currentThread().interrupt();
        }

        if (workerThread.isAlive())
            LOG.error(workerThread.getName() + " thread is still running");
    }

    private synchronized List<User> getTimedOutUsers(final long currentTime,
        final long timeout) {
        List<User> users = new ArrayList<User>();

        for (Iterator<UserPongStatus> it = currentUsers.iterator(); it
            .hasNext(); ) {

            UserPongStatus status = it.next();

            if (currentTime - status.lastPongReceivedTime > timeout) {
                users.add(status.user);
                it.remove();
            }
        }

        return users;

    }

    /**
     * Returns all users that are currently marked as <tt>inSession</tt> from
     * the {@link #currentUsers} list.
     */
    private synchronized List<User> getCurrentUsers() {
        List<User> users = new ArrayList<User>();

        for (Iterator<UserPongStatus> it = currentUsers.iterator(); it
            .hasNext(); ) {

            UserPongStatus status = it.next();

            if (status.user.isInSession())
                users.add(status.user);
        }

        return users;
    }

    /**
     * Removes the all users from the {@link #currentUsers} list that are no
     * longer marked as <tt>inSession</tt>.
     */
    private synchronized void removeInactiveUsers() {
        for (Iterator<UserPongStatus> it = currentUsers.iterator(); it
            .hasNext(); ) {

            UserPongStatus status = it.next();

            if (!status.user.isInSession())
                it.remove();
        }
    }

    /**
     * Removes the given users from the {@link #currentUsers} list.
     */
    private synchronized void removeUsers(final Collection<User> users) {
        for (User user : users) {
            for (Iterator<UserPongStatus> it = currentUsers.iterator(); it
                .hasNext(); ) {
                UserPongStatus status = it.next();

                if (status.user == user) {
                    it.remove();
                    break;
                }
            }
        }
    }

    private static class UserPongStatus {

        private final User user;
        private volatile long lastPongReceivedTime;

        private UserPongStatus(User user) {
            this.user = user;
            this.lastPongReceivedTime = System.currentTimeMillis();
        }

    }
}
