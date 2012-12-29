/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A TransferDescription contains all necessary information for dealing with
 * FileTransfers to a buddy via Jingle, IBB or Chat.
 * 
 * Instances of this class may not be reused but should be treated as value
 * objects, otherwise serialization will fail.
 */
public class TransferDescription {

    private TransferDescription() {
        // NOP
    }

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

    private String type;

    private String namespace;

    private String sessionID;

    private JID recipient;

    private JID sender;

    private String archivePath;

    private long size;

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

        if (STREAM_DATA.equals(type)) {
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

    public static TransferDescription createCustomTransferDescription() {
        return new TransferDescription();
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

    public static byte[] toByteArray(TransferDescription description)
        throws IOException {
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();

        DataOutputStream out = new DataOutputStream(serialized);

        out.writeUTF(description.type != null ? description.type : "");
        out.writeUTF(description.namespace != null ? description.namespace : "");
        out.writeUTF(description.sessionID != null ? description.sessionID : "");
        out.writeUTF(description.archivePath != null ? description.archivePath
            : "");
        out.writeUTF(description.invitationID != null ? description.invitationID
            : "");
        out.writeUTF(description.testID != null ? description.testID : "");
        out.writeUTF(description.processID != null ? description.processID : "");

        out.writeUTF(description.recipient != null ? description.recipient
            .toString() : "");
        out.writeUTF(description.sender != null ? description.sender.toString()
            : "");

        out.writeLong(description.size);
        out.writeBoolean(description.compress);

        out.close();

        return serialized.toByteArray();
    }

    public static TransferDescription fromByteArray(byte[] data)
        throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

        TransferDescription description = new TransferDescription();

        description.type = in.readUTF();
        description.namespace = in.readUTF();
        description.sessionID = in.readUTF();
        description.archivePath = in.readUTF();
        description.invitationID = in.readUTF();
        description.testID = in.readUTF();
        description.processID = in.readUTF();

        description.recipient = new JID(in.readUTF());
        description.sender = new JID(in.readUTF());

        description.size = in.readLong();
        description.compress = in.readBoolean();

        return description;

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

    /**
     * Set the size of the object that is to be transferred (e.g. bytes, words,
     * units)
     */
    TransferDescription setSize(long size) {
        this.size = size;
        return this;
    }

    /**
     * Returns the size of the transferred object (in bytes or words or other
     * units)
     */
    public long getSize() {
        return this.size;
    }
}