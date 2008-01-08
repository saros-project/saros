/*
 * DPP - Serious Distributed Pair Programming (c) Freie Universit�t Berlin -
 * Fachbereich Mathematik und Informatik - 2006 (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package de.fu_berlin.inf.dpp.net;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * An humble interface that is responsible for network functionality. The idea
 * behind this interface is to only capsulates the least possible amount of
 * functionality - the one that can't be easily tested.
 * 
 * @author rdjemili
 */
public interface ITransmitter {

	/* ---------- invitations --------- */

	// TODO move aggregation of pending invitation to sessionManager
	/**
	 * Adds given invitation to the list of pending invitations.
	 * 
	 * @param invitation
	 *            the invitation that is to be added.
	 */
	public void addInvitationProcess(IInvitationProcess invitation);

	/**
	 * Removes given invitation from the list of pending invitations.
	 * 
	 * @param invitation
	 *            the invitation that is to be removed.
	 */
	public void removeInvitationProcess(IInvitationProcess invitation);

	/**
	 * Sends an invitation message for given shared project to given user.
	 * 
	 * @param sharedProject
	 *            the shared project to which the user should be invitited to.
	 * @param jid
	 *            the Jabber ID of the user that is to be invited.
	 * @param description
	 *            a informal description text that can be provided with the
	 *            invitation. Can not be <code>null</code>.
	 */
	public void sendInviteMessage(ISharedProject sharedProject, JID jid, String description);

	/**
	 * Sends an cancellation message that tells the receiver that the invitation
	 * is canceled.
	 * 
	 * @param jid
	 *            the Jabber ID of the receipient.
	 * @param errorMsg
	 *            the reason why the invitation was canceled or
	 *            <code>null</code>.
	 */
	public void sendCancelInvitationMessage(JID jid, String errorMsg);

	/* ---------- files --------- */

	/**
	 * Sends given file list to given Jabber user. This methods blocks until the
	 * file transfer is done or failed.
	 * 
	 * @param jid
	 *            the Jabber ID of the user to which the file list is to be
	 *            sent.
	 * @param fileList
	 *            the file list that is to be sent.
	 * @throws XMPPException
	 *             is thrown if there is some problem with the XMPP file
	 *             transfer.
	 */
	public void sendFileList(JID jid, FileList fileList) throws XMPPException;

	/**
	 * Sends a request-for-file-list-message to given user.
	 * 
	 * @param recipient
	 *            the Jabber ID of the recipient.
	 */
	public void sendRequestForFileListMessage(JID recipient);

	/**
	 * Sends given file to given recipient.
	 * 
	 * @param recipient
	 *            the Jabber ID of the recipient.
	 * @param path
	 *            the project-relative path of the resource that is to be sent.
	 * @param callback
	 *            an callback for the file transfer state. Can be
	 *            <code>null</code>.
	 */
	public void sendFile(JID recipient, IPath path, IFileTransferCallback callback);

	/**
	 * Sends given file to given recipient with given timestamp.
	 * 
	 * @param recipient
	 *            the Jabber ID of the recipient.
	 * @param path
	 *            the project-relative path of the resource that is to be sent.
	 * @param timestamp
	 *            the time that will be associated with this activity.
	 * @param callback
	 *            an callback for the file transfer state. Can be
	 *            <code>null</code>.
	 */
	public void sendFile(JID recipient, IPath path, int timestamp, IFileTransferCallback callback);

	/**
	 * Sends queued file transfers.
	 */
	public void sendRemainingFiles();

	/**
	 * Sends queued messages.
	 */	
	public void sendRemainingMessages();
	
	/**
	 * Sends a list of users to given recipient 
	 * @param to
	 * 			Receipient of this list
	 * @param participants
	 * 			List of Users, of current shared project participants 
	 */
	public void sendUserListTo(JID to, List<User> participants);
	
	/**
	 * Sets my XMPP connection to the given connection - for changing the current connection (like after reconnect).
	 * 
	 * @param connection
	 *            the new XMPPConnection 
	 */
	public void setXMPPConnection(XMPPConnection connection);

	/**
	 * Sends a request for activities to all users.
	 * 
	 * @param sharedProject
	 *            the shared project
	 * @param timestamp
	 *            the timestamp of the requested activity
	 * @param andup
	 *            boolean, if all activities after the requested one are requested too 
	 */	
	public void sendRequestForActivity(ISharedProject sharedProject, int timestamp, boolean andup);
	
	
	/* ---------- etc --------- */

	/**
	 * Sends a join message to the participants of given shared project. See
	 * {@link IInvitationProcess} for more information when this is supposed be
	 * sent.
	 * 
	 * @param sharedProject
	 *            the shared project that this join message refers to.
	 */
	public void sendJoinMessage(ISharedProject sharedProject);

	/**
	 * Sends a leave message to the participants of given shared project. See
	 * {@link IInvitationProcess} for more information when this is supposed be
	 * sent.
	 * 
	 * @param sharedProject
	 *            the shared project that this join message refers to.
	 */
	public void sendLeaveMessage(ISharedProject sharedProject);

	/**
	 * Sends given list of activities with given timestamp to the participants
	 * of given shared project.
	 * 
	 * @param sharedProject
	 *            the shared project the activities refer to.
	 * @param activities
	 *            a list of timed activities.
	 */
	public void sendActivities(ISharedProject sharedProject, List<TimedActivity> activities);
}
