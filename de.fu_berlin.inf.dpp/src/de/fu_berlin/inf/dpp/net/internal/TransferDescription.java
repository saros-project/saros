/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A TransferDescription contains all necessary information for dealing with
 * FileTransfers to a buddy via Jingle, IBB or Chat.
 * 
 * Instances of this class may not be reused but should be treated as value
 * objects, otherwise serialization will fail.
 */
public class TransferDescription implements Serializable {

    private static final long serialVersionUID = -3775431613174873948L;

    protected TransferDescription() {
        // prevent access to this class except through helper methods
        // maybe you should had have make this ctor private ...
    }

    /**
     * Transfer of a FileList
     */
    public static final String FILELIST_TRANSFER = "filelist-transfer";
    /**
     * Transfer of several resources in a ZIP-File
     */
    public static final String ARCHIVE_TRANSFER = "archive-transfer";
    /**
     * data in a stream
     */
    public static final String STREAM_DATA = "stream-data";
    /**
     * meta data for a stream
     */
    public static final String STREAM_META = "stream-meta";
    /**
     * test data for connection tests
     */
    public static final String CONNECTION_TEST = "connection-test";

    /**
     * This field is for internal use by BinaryChannel to identify an object.
     */

    private int id;

    private String type;

    private String namespace;

    private String sessionID;

    private JID recipient;

    private JID sender;

    private String archivePath;

    /**
     * Field used to indicate that the file is compressed already, like in
     * ARCHIVE_TRANSFER or RESOURCE_TRANSFER with a File like a jar, jpeg, ....
     */
    private boolean compress;

    /**
     * The invitationID of this TransferDescription or null if this
     * TransferDescription is not used during an invitation.
     */
    private String invitationID;

    /**
     * If this TransferDescription is of type
     * {@link TransferDescription#CONNECTION_TEST} then testID is set to the ID
     * of the IQ packet to send in reply to receiving test data.
     * 
     * testID is null if this TransferDescription is not of type
     * {@link TransferDescription#CONNECTION_TEST}
     */
    private String testID;

    /**
     * The processID of this TransferDescription or null if this
     * TransferDescription is not used during project exchange.
     */
    private String processID;

    @Override
    public String toString() {

        if (ARCHIVE_TRANSFER.equals(type)) {
            return "Archive from " + Utils.prefix(sender) + " [SID="
                + sessionID + "]";
        } else if (FILELIST_TRANSFER.equals(type)) {
            return "FileList from " + Utils.prefix(sender) + " [SID="
                + sessionID + "]";
        } else if (STREAM_DATA.equals(type)) {
            return "Stream data from " + Utils.prefix(sender) + ": stream= "
                + archivePath + " [SID=" + sessionID + "]";
        } else if (STREAM_META.equals(type)) {
            return "Stream metadata from " + Utils.prefix(sender)
                + ": stream= " + archivePath + " [SID=" + sessionID + "]";
        } else if (CONNECTION_TEST.equals(type)) {
            return "Connection test from " + Utils.prefix(sender);
        } else {
            StringBuilder sb = new StringBuilder("Bytestream transfer. type="
                + type + " namespace=" + namespace);

            if (sessionID != null)
                sb.append(" [SID=" + sessionID + "]");

            return sb.toString();
        }
    }

    public static TransferDescription createFileListTransferDescription(
        JID recipient, JID sender, String sessionID, String processID) {
        TransferDescription result = new TransferDescription();
        result.sender = sender;
        result.recipient = recipient;
        result.type = FILELIST_TRANSFER;
        result.sessionID = sessionID;
        result.processID = processID;
        result.compress = false;
        return result;
    }

    public static TransferDescription createTestTransferDescription(
        JID recipient, String testID, JID sender) {
        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.testID = testID;
        result.type = CONNECTION_TEST;
        result.compress = false;
        return result;
    }

    public static TransferDescription createArchiveTransferDescription(
        JID recipient, JID sender, String sessionID, String invitationID) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.type = ARCHIVE_TRANSFER;
        result.sessionID = sessionID;
        result.invitationID = invitationID;
        result.compress = false;

        return result;
    }

    public static TransferDescription createStreamDataTransferDescription(
        JID recipient, JID sender, String sessionID, String streamPath) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.type = STREAM_DATA;
        result.sessionID = sessionID;
        result.archivePath = streamPath;
        result.compress = true;

        return result;
    }

    public static TransferDescription createStreamMetaTransferDescription(
        JID recipient, JID sender, String streamPath, String sessionID) {
        TransferDescription result = createStreamDataTransferDescription(
            recipient, sender, sessionID, streamPath);

        result.type = STREAM_META;

        return result;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ObjectOutputStream object = null;
        try {
            object = new ObjectOutputStream(os);
            object.writeObject(this);
            object.close();
            os.close();
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(
                "Could not serialize: ObjectOutputStream failed: " + e);
        }
        return os.toByteArray();
    }

    public static TransferDescription fromByteArray(byte[] data)
        throws ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ObjectInputStream object = null;
        try {
            object = new ObjectInputStream(in);
            Object o = object.readObject();
            object.close();
            in.close();
            return (TransferDescription) o;
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(
                "Could not deserialize the transfer description object: "
                    + e.getMessage());
        }
    }

    TransferDescription setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    TransferDescription setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    TransferDescription setRecipient(JID recipient) {
        this.recipient = recipient;
        return this;
    }

    public JID getRecipient() {
        return recipient;
    }

    TransferDescription setSender(JID sender) {
        this.sender = sender;
        return this;
    }

    public JID getSender() {
        return sender;
    }

    TransferDescription setID(int id) {
        this.id = id;
        return this;
    }

    public int getID() {
        return id;
    }

    TransferDescription setTestID(String testID) {
        this.testID = testID;
        return this;
    }

    public String getTestID() {
        return testID;
    }

    TransferDescription setSessionID(String sessionID) {
        this.sessionID = sessionID;
        return this;
    }

    public String getSessionID() {
        return sessionID;
    }

    TransferDescription setProcessID(String processID) {
        this.processID = processID;
        return this;
    }

    public String getProcessID() {
        return processID;
    }

    TransferDescription setInvitationID(String invitationID) {
        this.invitationID = invitationID;
        return this;
    }

    public String getInvitationID() {
        return invitationID;
    }

    TransferDescription setArchivePath(String archivePath) {
        this.archivePath = archivePath;
        return this;
    }

    public String getArchivePath() {
        return archivePath;
    }

    TransferDescription setCompressContent(boolean compress) {
        this.compress = compress;
        return this;
    }

    public boolean compressContent() {
        return compress;
    }

}