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
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.io.IOException;

/**
 * Component for detecting network errors on the client side of a session.
 *
 * @author srossbach
 */
public final class ClientSessionTimeoutHandler extends SessionTimeoutHandler {

    private static final Logger LOG = Logger
        .getLogger(ClientSessionTimeoutHandler.class);

    private boolean shutdown;

    private boolean pingReceived;

    private long lastPingReceived;
    private final PacketListener pingPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            synchronized (ClientSessionTimeoutHandler.this) {
                lastPingReceived = System.currentTimeMillis();
                pingReceived = true;
                ClientSessionTimeoutHandler.this.notifyAll();
            }
        }
    };
    private final Runnable clientSessionTimeoutWatchdog = new Runnable() {

        @Override
        public void run() {
            while (true) {

                boolean abort = false;

                synchronized (ClientSessionTimeoutHandler.this) {

                    while (!pingReceived && !shutdown) {
                        try {
                            ClientSessionTimeoutHandler.this
                                .wait(PING_PONG_UPDATE_DELAY);
                        } catch (InterruptedException e) {
                            if (!shutdown)
                                LOG.error("watchdog shutdown prematurly", e);

                            return;
                        }

                        if ((System.currentTimeMillis() - lastPingReceived)
                            > PING_PONG_TIMEOUT) {
                            abort = true;
                            break;
                        }
                    }

                    if (shutdown)
                        return;

                    pingReceived = false;
                }

                if (abort) {
                    LOG.error("no ping received, reached timeout = "
                        + PING_PONG_TIMEOUT);
                    handleNetworkError(session.getHost().getJID(), "RxFailure");
                    return;
                }

                try {
                    transmitter.send(ISarosSession.SESSION_CONNECTION_ID,
                        session.getHost().getJID(), PongExtension.PROVIDER
                            .create(new PongExtension(currentSessionID))
                    );
                } catch (IOException e) {
                    LOG.error("failed to send pong", e);
                    handleNetworkError(session.getHost().getJID(), "TxFailure");
                }
            }
        }
    };
    private Thread workerThread;

    public ClientSessionTimeoutHandler(ISarosSession session,
        ISarosSessionManager sessionManager, ActivitySequencer sequencer,
        ITransmitter transmitter, IReceiver receiver) {
        super(session, sessionManager, sequencer, transmitter, receiver);
    }

    @Override
    public void start() {

        if (session.isHost())
            throw new IllegalStateException(
                "component cannot be started in host mode");

        super.start();

        lastPingReceived = System.currentTimeMillis();

        receiver.addPacketListener(pingPacketListener,
            PingExtension.PROVIDER.getPacketFilter(currentSessionID));

        workerThread = ThreadUtils
            .runSafeAsync("ClientSessionTimeoutWatchdog", LOG,
                clientSessionTimeoutWatchdog);
    }

    @Override
    public void stop() {
        super.stop();

        receiver.removePacketListener(pingPacketListener);

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
}
