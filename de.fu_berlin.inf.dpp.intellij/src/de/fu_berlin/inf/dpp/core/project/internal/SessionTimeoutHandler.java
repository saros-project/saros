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

import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.session.internal.IActivitySequencerCallback;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/**
 * Abstract base class that is already capable of detecting and handling network
 * errors occurred in the {@link ActivitySequencer} component.
 *
 * @author srossbach
 */
abstract class SessionTimeoutHandler implements Startable {

    /**
     * Join timeout when stopping this component
     */
    protected static final long TIMEOUT = 10000L;
    /**
     * Total timeout in milliseconds to removeAll a user(host) or stop the
     * session(client) if no ping or pong packet is received.
     */
    protected static final long PING_PONG_TIMEOUT = Long
        .getLong("de.fu_berlin.inf.dpp.session.timeout.PING_PONG_TIMEOUT",
            60L * 1000L * 5L);
    /**
     * Update interval for sending and / or checking the status of ping and pong
     * packets.
     */
    protected static final long PING_PONG_UPDATE_DELAY = Long
        .getLong("de.fu_berlin.inf.dpp.session.timeout.PING_PONG_UPDATE_DELAY",
            30000L);
    private final static Logger LOG = Logger
        .getLogger(SessionTimeoutHandler.class);
    /**
     * Current session the component is run with.
     */
    protected final ISarosSession session;

    protected final ISarosSessionManager sessionManager;

    protected final ITransmitter transmitter;
    protected final IReceiver receiver;

    /**
     * Current id of the session.
     */
    protected final String currentSessionID;

    private final ActivitySequencer sequencer;

    private final IActivitySequencerCallback callback = new IActivitySequencerCallback() {
        @Override
        public void transmissionFailed(final JID jid) {
            if (session.isHost())
                handleNetworkError(jid, "TxFailure");
            else
                handleNetworkError(jid, "TxFailure");
        }
    };

    protected SessionTimeoutHandler(ISarosSession session,
        ISarosSessionManager sessionManager, ActivitySequencer sequencer,
        ITransmitter transmitter, IReceiver receiver) {
        this.session = session;
        this.sessionManager = sessionManager;
        this.sequencer = sequencer;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.currentSessionID = session.getID();
    }

    @Override
    public void start() {
        sequencer.setCallback(callback);
    }

    @Override
    public void stop() {
        sequencer.setCallback(null);
    }

    /**
     * Handles a network error by either stopping the session or removing the
     * user from the session depending on the state of the local user. This
     * method returns immediately and performs its work in the background.
     *
     * @param jid    the {@linkplain JID} of the user
     * @param reason a reason why a network error occurred
     */
    protected final void handleNetworkError(final JID jid,
        final String reason) {

        String threadName = reason == null ? "" : reason;

        if (session.isHost()) {
            ThreadUtils
                .runSafeAsync("RemoveUser" + threadName, LOG, new Runnable() {
                        @Override
                        public void run() {
                            User user = session.getUser(jid);
                            if (user != null)
                                session.removeUser(user);
                        }
                    }
                );
        } else {
            ThreadUtils
                .runSafeAsync("StopSession" + threadName, LOG, new Runnable() {
                        @Override
                        public void run() {
                            sessionManager.stopSarosSession();
                        }
                    }
                );
        }
    }
}
