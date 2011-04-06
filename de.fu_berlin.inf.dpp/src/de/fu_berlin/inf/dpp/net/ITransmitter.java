/*
 * DPP - Serious Distributed Pair Programming (c) Freie Universitaet Berlin -
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.UserListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider.XStreamPacketExtension;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * An humble interface that is responsible for network functionality. The idea
 * behind this interface is to only encapsulates the least possible amount of
 * functionality - the one that can't be easily tested.
 * 
 * @author rdjemili
 */
@Component(module = "net")
public interface ITransmitter {

    public static final long INVITATION_ACKNOWLEDGEMENT_TIMEOUT = 3000;

    /* ---------- invitations --------- */

    /**
     * Sends an invitation message for given shared project to given user.
     * 
     * @param projectID
     *            the ID of the IProject to which the user should be invited to.
     * @param jid
     *            the JID of the user that is to be invited.
     * @param description
     *            a informal description text that can be provided with the
     *            invitation. Can not be <code>null</code>.
     * @param comPrefs
     *            TODO
     */
    public void sendInvitation(String projectID, JID jid, String description,
        int colorID, VersionInfo versionInfo, String invitationID,
        DateTime sessionStart, boolean doStream, MUCSessionPreferences comPrefs);

    /**
     * Sends an cancellation message that tells the receiver that the invitation
     * is canceled.
     * 
     * @param jid
     *            the JID of the recipient.
     * @param errorMsg
     *            the reason why the invitation was canceled or
     *            <code>null</code>.
     */
    public void sendCancelInvitationMessage(JID jid, String errorMsg);

    /* ---------- files --------- */

    /**
     * Sends given file list to given XMPP user. This methods blocks until the
     * file transfer is done or failed.
     * 
     * @param jid
     *            the JID of the user to which the file list is to be sent.
     * 
     * @param fileLists
     *            the file lists that are to be sent.
     * 
     * @param monitor
     *            a monitor to which progress will be reported and which is
     *            queried for cancellation.
     * 
     * @throws SarosCancellationException
     *             if the operation was canceled via the given progress monitor
     *             an LocalCancellationException is thrown. If the operation was
     *             canceled via the monitor and the exception is not received,
     *             the operation completed successfully, before noticing the
     *             cancellation.
     * 
     *             if the operation was canceled by the recipient (remotely) a
     *             RemoteCancellationException is thrown
     * 
     * @throws IOException
     *             if the operation fails because of a problem with the XMPP
     *             Connection.
     */
    public void sendFileLists(JID jid, String processID,
        List<FileList> fileLists, SubMonitor monitor) throws IOException,
        SarosCancellationException;

    /**
     * 
     * @param invitationID
     *            ID of the ivitation process
     * @param monitor
     *            a monitor to which progress will be reported and which is
     *            queried for cancellation.
     * @return whether a invitation acknowledgment got received in
     *         {@link #INVITATION_ACKNOWLEDGEMENT_TIMEOUT} or not
     * @throws LocalCancellationException
     */
    public boolean receivedInvitationAcknowledgment(String invitationID,
        SubMonitor monitor) throws LocalCancellationException;

    /**
     * @throws IOException
     *             If the operation fails because of a problem with the XMPP
     *             Connection.
     * @throws LocalCancellationException
     */
    public DefaultInvitationInfo receiveFileListRequest(
        SarosPacketCollector collector, String invitationID, SubMonitor monitor)
        throws IOException, LocalCancellationException;

    /**
     * @param archiveCollector
     * @blocking If forceWait is true.
     * 
     * @throws IOException
     *             If the operation fails because of a problem with the XMPP
     *             Connection.
     */
    public FileList receiveFileList(SarosPacketCollector archiveCollector,
        SubMonitor monitor, boolean forceWait)
        throws SarosCancellationException, IOException;

    /**
     * 
     * @param processID
     * @param peer
     *            TODO
     * @blocking If forceWait is true.
     * @throws IOException
     *             If the operation fails because of a problem with the XMPP
     *             Connection.
     */
    public List<FileList> receiveFileLists(String processID, JID peer,
        SubMonitor monitor, boolean forceWait)
        throws SarosCancellationException, IOException;

    /**
     * 
     * @param processID
     *            ID to separate the project exchanging processes from one
     *            another
     * @param peer
     *            {@link JID} of the peer receiving from
     * @param forceWait
     * @return The archive as an {@link InputStream}
     */
    public InputStream receiveArchive(String processID, JID peer,
        SubMonitor monitor, boolean forceWait) throws IOException,
        SarosCancellationException;

    /**
     * a generic receive method
     * 
     * @param collector
     *            - {@link SarosPacketCollector} knows what to collect
     * @param timeout
     *            - how long do we wait
     * @param forceWait
     * @return
     * @throws LocalCancellationException
     * @throws IOException
     */
    public Packet receive(SubMonitor monitor, SarosPacketCollector collector,
        long timeout, boolean forceWait) throws LocalCancellationException,
        IOException;

    // FIXME Add Javadoc. Why is an invitationID needed?
    public void sendUserList(JID to, String invitationID, Collection<User> user);

    public boolean receiveUserListConfirmation(SarosPacketCollector collector,
        List<User> fromUsers, SubMonitor monitor)
        throws LocalCancellationException;

    /**
     * Sends a request-for-file-list-message to given user.
     * 
     * @param recipient
     *            the JID of the recipient.
     */
    public void sendFileListRequest(JID recipient, String invitationID);

    /**
     * Sends given archive file to given recipient.
     * 
     * This is a blocking method.
     * 
     * @param recipient
     *            the JID of the recipient.
     * @param archive
     *            the project-relative path of the resource that is to be sent.
     * @param monitor
     *            a monitor to which progress will be reported and which is
     *            queried for cancellation.
     * 
     * @throws IOException
     *             If the file could not be read or an error occurred while
     *             sending or a technical error happened.
     * @throws SarosCancellationException
     *             if the operation was canceled via the given progress monitor
     *             an LocalCancellationException is thrown. If the operation was
     *             canceled via the monitor and the exception is not received,
     *             the operation completed successfully, before noticing the
     *             cancellation.
     * 
     *             if the operation was canceled by the recipient a
     *             RemoteCancellationException is thrown
     * 
     * @blocking Blocks until the transfer is complete.
     */
    public void sendProjectArchive(JID recipient, String invitationID,
        File archive, SubMonitor monitor) throws IOException,
        SarosCancellationException;

    /**
     * Sends queued file transfers.
     */
    public void sendRemainingFiles();

    /**
     * Sends queued messages.
     */
    public void sendRemainingMessages();

    /**
     * Sends a request for activityDataObjects to all users.
     * 
     * TODO SS MR Dependency Violation - ITransmitter should not need a shared
     * project
     * 
     * @param sarosSession
     *            the Saros session
     * @param requestedSequenceNumbers
     *            a map containing the sequence number to be requested as a
     *            value and the user to request them from as key
     * @param andUp
     *            true if all activityDataObjects after the requested one are
     *            requested too, false if only the activityDataObject with the
     *            requestedSequenceNumber is requested
     */
    public void sendRequestForActivity(ISarosSession sarosSession,
        Map<JID, Integer> requestedSequenceNumbers, boolean andUp);

    /* ---------- etc --------- */

    /**
     * Sends a leave message to the participants of given Saros session. See
     * {@link InvitationProcess} for more information when this is supposed be
     * sent.
     * 
     * @param sarosSession
     *            the Saros session that this join message refers to.
     */
    public void sendLeaveMessage(ISarosSession sarosSession);

    /**
     * Sends given list of TimedActivities to the given recipient.
     * 
     * This list MUST not contain any {@link FileActivityDataObject}s where
     * {@link FileActivityDataObject#getType()} ==
     * {@link FileActivity.Type#Created} as binary data is not supported in
     * messages bodies.
     * 
     * @param recipient
     *            The JID of the user who is to receive the given list of timed
     *            activityDataObjects.
     * @param timedActivities
     *            The list of timed activityDataObjects to send to the user.
     * 
     * @throws IllegalArgumentException
     *             if the recipient is null, the recipient equals the local user
     *             as returned by {@link Saros#getMyJID()} or the given list is
     *             null or contains no activityDataObjects.
     * 
     * @throws AssertionError
     *             if the given list of timed activityDataObjects contains
     *             FileActivities of type created AND the application is run
     *             using asserts.
     */
    public void sendTimedActivities(JID recipient,
        List<TimedActivityDataObject> timedActivities);

    /**
     * Sends a query, a {@link IQ.Type} GET, to the user with given {@link JID}.
     * 
     * Example using provider:
     * <p>
     * <code>XStreamExtensionProvider<VersionInfo> versionProvider = new
     * XStreamExtensionProvider<VersionInfo>( "sarosVersion", VersionInfo.class,
     * Version.class, Compatibility.class);<br>
     * sendQuery(jid, versionProvider, 5000);
     * </code>
     * </p>
     * In this example this sends a request to the user with jid and waits 5
     * seconds for an answer. If it arrives in time, a payload of type T (in
     * this case VersionInfo) will be returned, else the result is null.
     */
    public <T> T sendQuery(JID jid, XStreamExtensionProvider<T> provider,
        T payload, long timeout);

    public SarosPacketCollector getInvitationCollector(String invitationID,
        String filelistTransfer);

    /**
     * @param to
     *            peer that invited this user and where to send the
     *            acknowledgment to
     * @param invitationID
     *            the ID of the invitation
     */
    public void sendInvitationAcknowledgement(JID to, String invitationID);

    public void receiveInvitationCompleteConfirmation(SubMonitor monitor,
        SarosPacketCollector collector) throws LocalCancellationException,
        IOException;

    public SarosPacketCollector getFileListRequestCollector(String invitationID);

    public SarosPacketCollector getUserListRequestCollector(
        String invitationID,
        UserListRequestExtensionProvider userListRequestExtProv);

    public SarosPacketCollector getInvitationCompleteCollector(
        String invitationID);

    public SarosPacketCollector getUserListConfirmationCollector();

    public void sendInvitationCompleteConfirmation(JID to, String invitationID);

    public void sendCancelSharingProjectMessage(JID peer, String errorMsg);

    public void sendMessageToUser(JID peer,
        XStreamPacketExtension<DefaultInvitationInfo> create);
}
