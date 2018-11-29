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

import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.PacketExtension;

/**
 * @author rdjemili
 * @author sotitas
 */
public abstract class SessionNegotiation extends Negotiation {

  private static final Logger log = Logger.getLogger(SessionNegotiation.class);

  /** Timeout for all packet exchanges during the session negotiation */
  protected static final long PACKET_TIMEOUT =
      Long.getLong("de.fu_berlin.inf.dpp.negotiation.session.PACKET_TIMEOUT", 30000L);

  /**
   * Timeout on how long the session negotiation should wait for the remote user to accept the
   * invitation
   */
  protected static final long INVITATION_ACCEPTED_TIMEOUT =
      Long.getLong("de.fu_berlin.inf.dpp.negotiation.session.INVITATION_ACCEPTED_TIMEOUT", 600000L);

  /**
   * Timeout on how long the session negotiation should wait for the remote user to connect to the
   * host side.
   */
  protected static final long CONNECTION_ESTABLISHED_TIMEOUT =
      Long.getLong(
          "de.fu_berlin.inf.dpp.negotiation.session.CONNECTION_ESTABLISHED_TIMEOUT", 120000L);

  protected final SessionNegotiationHookManager hookManager;

  protected final ISarosSessionManager sessionManager;

  protected final String description;

  protected ISarosSession sarosSession;

  public SessionNegotiation(
      final String id,
      final JID peer,
      final String description,
      final ISarosSessionManager sessionManager,
      final SessionNegotiationHookManager hookManager,
      final ITransmitter transmitter,
      final IReceiver receiver) {
    super(id, peer, transmitter, receiver);

    this.sessionManager = sessionManager;
    this.hookManager = hookManager;
    this.description = description;
  }

  /** @return the user-provided informal description that can be provided with an invitation. */
  public String getDescription() {
    return description;
  }

  @Override
  protected void notifyCancellation(SarosCancellationException exception) {

    if (!(exception instanceof LocalCancellationException)) return;

    LocalCancellationException cause = (LocalCancellationException) exception;

    if (cause.getCancelOption() != CancelOption.NOTIFY_PEER) return;

    log.debug("notifying remote contact " + getPeer() + " of the local cancellation");

    PacketExtension notification =
        CancelInviteExtension.PROVIDER.create(
            new CancelInviteExtension(getID(), cause.getMessage()));

    transmitter.sendPacketExtension(getPeer(), notification);
  }

  @Override
  protected void notifyTerminated(NegotiationListener listener) {
    listener.negotiationTerminated(this);
  }
}
