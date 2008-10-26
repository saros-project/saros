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
package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A listener for SharedProject life-cycle related events.
 * 
 * @author rdjemili
 */
public interface ISessionListener {

    /**
     * Is fired when the local user is invited to a session. Use
     * {@link SessionManager#acceptSessionInvitation(JID, String, org.eclipse.core.resources.IProject)}
     * to accept the invitation.
     * 
     * @param process
     *            the invitation process which represents the invitation.
     */
    public void invitationReceived(IIncomingInvitationProcess invitation);

    /**
     * Is fired when a session ended. Reasons for this can be that the session
     * was closed or that the user left by himself.
     * 
     * @param session
     *            the shared project that has just been left. Is never
     *            <code>null</code>.
     */
    public void sessionEnded(ISharedProject session);

    /**
     * Is fired when a new session started.
     * 
     * @param session
     *            the shared project that has been created. Is never
     *            <code>null</code>.
     * 
     */
    public void sessionStarted(ISharedProject session);
}
