/*
 * DPP - Serious Distributed Pair Programming (c) Freie Universität Berlin -
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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.internal.extensions.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A humble interface that is responsible for network functionality. The idea
 * behind this interface is to only encapsulates the least possible amount of
 * functionality - the one that can't be easily tested.
 * 
 * @author rdjemili
 */
@Component(module = "net")
public interface ITransmitter {

    public static final int MAX_XMPP_MESSAGE_SIZE = 16378;

    /**
     * <p>
     * Sends the given {@link PacketExtension} to the given {@link JID}. The
     * recipient has to be in the session or the extension will not be sent.
     * </p>
     * 
     * <p>
     * If the extension's raw data (bytes) is longer than
     * {@value #MAX_XMPP_MESSAGE_SIZE} or if there is a peer-to-peer bytestream
     * to the recipient the extension will be sent using the bytestream. Else it
     * will be sent by chat.
     * </p>
     * 
     * <p>
     * Note: Does NOT ensure that peers receive messages in order because there
     * may be two completely different communication ways. See
     * {@link de.fu_berlin.inf.dpp.net.internal.ActivitySequencer} for details.
     * </p>
     * 
     * @param recipient
     * @param extension
     * @throws IOException
     *             if sending by bytestreams fails and the extension raw data is
     *             longer than {@value #MAX_XMPP_MESSAGE_SIZE}
     */
    public void sendToSessionUser(JID recipient, PacketExtension extension)
        throws IOException;

    /**
     * Sends the given {@link PacketExtension} to the given {@link JID} over the
     * currently established XMPP connection. There is <b>no</b> guarantee that
     * this message (extension) will arrive at the recipients side !
     * 
     * 
     * @param jid
     *            the recipient of the extension
     * @param extension
     *            the to send
     */
    public void sendMessageToUser(JID jid, PacketExtension extension);

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
     * 
     * @throws IOException
     *             if the operation fails because of a problem with the XMPP
     *             Connection.
     */
    public void sendFileLists(JID jid, String processID,
        List<FileList> fileLists) throws IOException;

    public void sendUserList(JID to, Collection<User> user);

    public boolean receiveUserListConfirmation(SarosPacketCollector collector,
        List<User> fromUsers, IProgressMonitor monitor)
        throws LocalCancellationException;

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
     * Sends an IQ {@linkplain Type#GET GET} query to the user with given
     * {@link JID} .
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

    public SarosPacketCollector getUserListConfirmationCollector();

    public void sendUserListRequest(JID peer);
}
