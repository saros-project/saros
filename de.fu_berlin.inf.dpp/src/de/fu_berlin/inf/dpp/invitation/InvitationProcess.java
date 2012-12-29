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
package de.fu_berlin.inf.dpp.invitation;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * @author rdjemili
 * @author sotitas
 */
public abstract class InvitationProcess extends CancelableProcess {

    private static final Logger log = Logger.getLogger(InvitationProcess.class);

    @Inject
    protected ITransmitter transmitter;
    protected JID peer;
    protected String description;
    protected final int colorID;

    @Inject
    protected ISarosSessionManager sarosSessionManager;

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    protected final String invitationID;

    public InvitationProcess(String invitationID, JID peer, String description,
        int colorID, SarosContext sarosContext) {
        this.invitationID = invitationID;
        this.peer = peer;
        this.description = description;
        this.colorID = colorID;
        sarosContext.initComponent(this);
        this.invitationProcesses.addInvitationProcess(this);
    }

    public JID getPeer() {
        return this.peer;
    }

    /**
     * @return the user-provided informal description that can be provided with
     *         an invitation.
     */
    public String getDescription() {
        return this.description;
    }

    @Override
    protected void notifyCancellation(SarosCancellationException exception) {

        if (!(exception instanceof LocalCancellationException))
            return;

        LocalCancellationException cause = (LocalCancellationException) exception;

        if (cause.getCancelOption() != CancelOption.NOTIFY_PEER)
            return;

        log.debug("notifying remote contact " + Utils.prefix(getPeer())
            + " of the local cancellation");

        PacketExtension notification = CancelInviteExtension.PROVIDER
            .create(new CancelInviteExtension(invitationID, cause.getMessage()));

        transmitter.sendMessageToUser(getPeer(), notification);
    }
}
