/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.negotiation;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * @author rdjemili
 * @author sotitas
 */
public abstract class SessionNegotiation extends Negotiation {

    private static final Logger log = Logger
        .getLogger(SessionNegotiation.class);

    /**
     * Timeout for all packet exchanges during the session negotiation
     */
    protected static final long PACKET_TIMEOUT = Long.getLong(
        "de.fu_berlin.inf.dpp.negotiation.session.PACKET_TIMEOUT", 30000L);

    /**
     * Timeout on how long the session negotiation should wait for the remote
     * user to accept the invitation
     */
    protected static final long INVITATION_ACCEPTED_TIMEOUT = Long.getLong(
        "de.fu_berlin.inf.dpp.negotiation.session.INVITATION_ACCEPTED_TIMEOUT",
        600000L);

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected IReceiver receiver;

    @Inject
    protected SessionNegotiationHookManager hookManager;

    private final String negotiationID;

    protected final String description;

    // FIXME make final
    protected JID peer;

    protected final String peerNickname;

    protected ISarosSession sarosSession;

    public SessionNegotiation(final String negotiationID, final JID peer,
        final String description, final ISarosContext sarosContext) {
        this.negotiationID = negotiationID;
        this.peer = peer;
        this.description = description;
        sarosContext.initComponent(this);

        String nickname = XMPPUtils.getNickname(
            sarosContext.getComponent(XMPPConnectionService.class), peer);

        peerNickname = nickname == null ? peer.getBareJID().toString()
            : nickname;
    }

    public JID getPeer() {
        return peer;
    }

    /**
     * @return the user-provided informal description that can be provided with
     *         an invitation.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the ID of this negotiation.
     * 
     * @return the ID
     */
    public final String getID() {
        return negotiationID;
    }

    @Override
    protected void notifyCancellation(SarosCancellationException exception) {

        if (!(exception instanceof LocalCancellationException))
            return;

        LocalCancellationException cause = (LocalCancellationException) exception;

        if (cause.getCancelOption() != CancelOption.NOTIFY_PEER)
            return;

        log.debug("notifying remote contact " + getPeer()
            + " of the local cancellation");

        PacketExtension notification = CancelInviteExtension.PROVIDER
            .create(new CancelInviteExtension(negotiationID, cause.getMessage()));

        transmitter.sendPacketExtension(getPeer(), notification);
    }

    /**
     * Returns the next packet from a collector.
     * 
     * @param collector
     *            the collector to monitor
     * @param timeout
     *            the amount of time to wait for the next packet (in
     *            milliseconds)
     * @return the collected packet or <code>null</code> if no packet was
     *         received
     * @throws SarosCancellationException
     *             if the negotiation was canceled
     */
    protected final Packet collectPacket(PacketCollector collector, long timeout)
        throws SarosCancellationException {

        Packet packet = null;

        while (timeout > 0) {
            checkCancellation(CancelOption.NOTIFY_PEER);

            packet = collector.nextResult(1000);

            if (packet != null)
                break;

            timeout -= 1000;
        }
        return packet;
    }

    @Override
    protected void notifyTerminated(NegotiationListener listener) {
        listener.negotiationTerminated(this);
    }
}
