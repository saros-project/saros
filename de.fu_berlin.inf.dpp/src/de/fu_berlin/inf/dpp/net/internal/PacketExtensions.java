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
package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Holds various simple helper methods to create and parse simple Smack packet
 * extensions.
 * 
 * @author rdjemili
 */
public class PacketExtensions {
    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    // elements
    private static final String INVITATION = "invite";

    private static final String CANCEL_INVITATION = "cancelInvite";

    private static final String JOIN = "join";

    private static final String LEAVE = "leave";

    private static final String REQUEST_FOR_LIST = "requestList";

    private static final String REQUEST_FOR_ACTIVITY = "requestActivity";

    private static final String USER_LIST = "userList";

    private static final String DATATRANSFER = "DataTransfer";

    private static final String JINGLEERROR = "JingleError";

    private static final String FILE_CHECKSUM_ERROR = "FileChecksumError";

    private static final String JUPITER_TRANSFORMATION_ERROR = "JupiterTransformationError";

    // attributes
    public static final String DESCRIPTION = "description";

    public static final String PROJECTNAME = "description";

    public static final String ERROR = "error";

    public static final String DT_NAME = "DTNAME";

    public static final String DT_DESC = "DTDESC";

    public static final String DT_SPLIT = "DTSPLIT";

    public static final String DT_DATA = "DATA_BASE64";

    public static final String FILE_PATH = "filename";

    private static final String DOC_CHECKSUM = "DocChecksum";

    public static void hookExtensionProviders() {

        ProviderManager providermanager = ProviderManager.getInstance();
        providermanager.addExtensionProvider(ActivitiesPacketExtension.ELEMENT,
                PacketExtensions.NAMESPACE, new ActivitiesProvider());
        providermanager.addExtensionProvider(RequestPacketExtension.ELEMENT,
                RequestPacketExtension.NAMESPACE,
                new RequestExtensionProvider());
    }

    /**
     * Creates the packet extension for new invitations.
     * 
     * @param description
     *            an informal text that will be shown with the invitation.
     * @return the packet extension.
     */
    public static PacketExtension createInviteExtension(String projectName,
            String description) {
        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.INVITATION, PacketExtensions.NAMESPACE);
        extension.setValue(PacketExtensions.PROJECTNAME, projectName);
        extension.setValue(PacketExtensions.DESCRIPTION, description);

        return extension;
    }

    /**
     * Creates the packet extension for canceling invitations.
     * 
     * @param error
     *            an user-readable text that contains the reason for the
     *            cancellation. Can be <code>null</code>.
     * @return the packet extension.
     */
    public static PacketExtension createCancelInviteExtension(String error) {
        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.CANCEL_INVITATION, PacketExtensions.NAMESPACE);

        if ((error != null) && (error.length() > 0)) {
            extension.setValue(PacketExtensions.ERROR, error);
        }
        return extension;
    }

    public static PacketExtension createRequestForActivityExtension(
            int timestamp, boolean andup) {

        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.REQUEST_FOR_ACTIVITY,
                PacketExtensions.NAMESPACE);
        extension.setValue("ID", (new Integer(timestamp)).toString());

        if (andup) {
            extension.setValue("ANDUP", "true");
        }

        return extension;
    }

    public static PacketExtension createDataTransferExtension(String name,
            String desc, int index, int count, String data) {

        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.DATATRANSFER, PacketExtensions.NAMESPACE);
        extension.setValue(PacketExtensions.DT_NAME, name);
        extension.setValue(PacketExtensions.DT_DESC, desc);
        extension.setValue(PacketExtensions.DT_DATA, data);

        String split = index + "/" + count;
        extension.setValue(PacketExtensions.DT_SPLIT, split);

        return extension;
    }

    public static PacketExtension createRequestForFileListExtension() {
        return PacketExtensions
                .createExtension(PacketExtensions.REQUEST_FOR_LIST);
    }

    public static PacketExtension createJoinExtension() {
        return PacketExtensions.createExtension(PacketExtensions.JOIN);
    }

    public static PacketExtension createLeaveExtension() {
        return PacketExtensions.createExtension(PacketExtensions.LEAVE);
    }

    public static PacketExtension createChecksumErrorExtension(IPath path) {
        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.FILE_CHECKSUM_ERROR,
                PacketExtensions.NAMESPACE);
        extension.setValue(PacketExtensions.FILE_PATH, path.toOSString());

        return extension;
    }

    public static PacketExtension createChecksumsExtension(
            Collection<DocumentChecksum> checksums) {
        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.DOC_CHECKSUM, PacketExtensions.NAMESPACE);

        extension.setValue("quantity", Integer.toString(checksums.size()));

        int i = 1;
        for (DocumentChecksum checksum : checksums) {
            extension.setValue("path" + Integer.toString(i), checksum.getPath()
                    .toPortableString());
            extension.setValue("length" + Integer.toString(i), Integer
                    .toString(checksum.getLength()));
            extension.setValue("hash" + Integer.toString(i), Integer
                    .toString(checksum.getHash()));
            i++;
        }

        return extension;
    }

    public static PacketExtension createJupiterErrorExtension(IPath path) {
        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.JUPITER_TRANSFORMATION_ERROR,
                PacketExtensions.NAMESPACE);
        extension.setValue(PacketExtensions.FILE_PATH, path.toOSString());

        return extension;
    }

    public static PacketExtension createUserListExtension(List<User> list) {
        DefaultPacketExtension extension = new DefaultPacketExtension(
                PacketExtensions.USER_LIST, PacketExtensions.NAMESPACE);

        int count = 0;
        for (User participant : list) {
            JID jid = participant.getJid();
            String id = "User" + count;
            String role = "UserRole" + count;
            String color = "UserColor" + count;
            extension.setValue(id, jid.toString());
            extension.setValue(role, participant.getUserRole().toString());
            extension.setValue(color, participant.getColorID() + "");
            count++;
        }

        return extension;
    }

    /**
     * Tries to create an default packet extension from given message. The
     * invite extension has a description field.
     */
    public static DefaultPacketExtension getInviteExtension(Message message) {
        return PacketExtensions.getExtension(PacketExtensions.INVITATION,
                message);
    }

    /**
     * Tries to create an default packet extension from given message. The
     * cancel extension can have a error field.
     */
    public static DefaultPacketExtension getCancelInviteExtension(
            Message message) {
        return PacketExtensions.getExtension(
                PacketExtensions.CANCEL_INVITATION, message);
    }

    public static DefaultPacketExtension getJoinExtension(Message message) {
        return PacketExtensions.getExtension(PacketExtensions.JOIN, message);
    }

    public static DefaultPacketExtension getLeaveExtension(Message message) {
        return PacketExtensions.getExtension(PacketExtensions.LEAVE, message);
    }

    public static DefaultPacketExtension getJupiterErrorExtension(
            Message message) {
        return PacketExtensions.getExtension(
                PacketExtensions.JUPITER_TRANSFORMATION_ERROR, message);
    }

    public static DefaultPacketExtension getChecksumErrorExtension(
            Message message) {
        return PacketExtensions.getExtension(
                PacketExtensions.FILE_CHECKSUM_ERROR, message);
    }

    public static DefaultPacketExtension getChecksumExtension(Message message) {
        return PacketExtensions.getExtension(PacketExtensions.DOC_CHECKSUM,
                message);
    }

    public static DefaultPacketExtension getUserlistExtension(Message message) {
        return PacketExtensions.getExtension(PacketExtensions.USER_LIST,
                message);
    }

    public static DefaultPacketExtension getRequestActivityExtension(
            Message message) {
        return PacketExtensions.getExtension(
                PacketExtensions.REQUEST_FOR_ACTIVITY, message);
    }

    public static DefaultPacketExtension getRequestExtension(Message message) {
        return PacketExtensions.getExtension(PacketExtensions.REQUEST_FOR_LIST,
                message);
    }

    public static DefaultPacketExtension getDataTransferExtension(
            Message message) {
        return PacketExtensions.getExtension(PacketExtensions.DATATRANSFER,
                message);
    }

    public static ActivitiesPacketExtension getActvitiesExtension(
            Message message) {
        return (ActivitiesPacketExtension) message.getExtension(
                ActivitiesPacketExtension.ELEMENT, PacketExtensions.NAMESPACE);
    }

    public static RequestPacketExtension getJupiterRequestExtension(
            Message message) {
        return (RequestPacketExtension) message.getExtension(
                RequestPacketExtension.ELEMENT, PacketExtensions.NAMESPACE);
    }

    private static DefaultPacketExtension createExtension(String element) {
        DefaultPacketExtension extension = new DefaultPacketExtension(element,
                PacketExtensions.NAMESPACE);
        extension.setValue(element, "");
        return extension;
    }

    private static DefaultPacketExtension getExtension(String element,
            Message message) {
        return (DefaultPacketExtension) message.getExtension(element,
                PacketExtensions.NAMESPACE);
    }

}
