/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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

import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;

/**
 * @author rdjemili
 * @author sotitas
 */
public abstract class InvitationProcess {

    protected final ITransmitter transmitter;
    protected JID peer;
    protected String description;
    protected final int colorID;

    protected InvitationProcessObservable invitationProcesses;

    public InvitationProcess(ITransmitter transmitter, JID peer,
        String description, int colorID,
        InvitationProcessObservable invitationProcesses) {
        this.transmitter = transmitter;
        this.peer = peer;
        this.description = description;
        this.colorID = colorID;
        this.invitationProcesses = invitationProcesses;
        this.invitationProcesses.addInvitationProcess(this);
    }

    /**
     * @return the peer that is participating with us in this process. For an
     *         incoming invitation this is the inviter. For an outgoing
     *         invitation this is the invitee.
     */
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
    public String toString() {
        return "InvitationProcess(peer:" + this.peer + ")";
    }

    /**
     * 
     * @return the name of the project that is shared by the peer.
     */
    public abstract String getProjectName();

    public enum CancelOption {
        /**
         * Use this option if the peer should be notified that the invitation
         * has been cancelled. He gets a message with the cancellation reason.
         */
        NOTIFY_PEER,
        /**
         * Use this option if the peer should not be notified that the
         * invitation has been cancelled.
         */
        DO_NOT_NOTIFY_PEER;
    }

    public enum CancelLocation {
        /**
         * Use this option if the invitation has been cancelled by the local
         * user.
         */
        LOCAL,
        /**
         * Use this option if the invitation has been cancelled by the remote
         * user.
         */
        REMOTE;
    }

    public abstract void remoteCancel(String errorMsg);

    public interface IIncomingInvitationUI {
        /**
         * Cancel the invitation UI for the given JID.
         * 
         * @param errorMsg
         *            Is null if the cancellation was due to a user action.
         * @param cancelLocation
         *            Is <code>REMOTE</code> if this message originated on the
         *            remote side or <code>LOCAL</code> if the message
         *            originated on the local side.
         */
        public void cancelWizard(JID jid, String errorMsg,
            CancelLocation cancelLocation);
    }

    /*
     * public interface IOutgoingInvitationUI { public boolean
     * confirmVersionConflict(VersionInfo versionInfo, JID peer);
     * 
     * public boolean confirmUnsupportedSaros(final JID currItem);
     * 
     * public boolean confirmProjectSave(final JID peer); }
     */
}
