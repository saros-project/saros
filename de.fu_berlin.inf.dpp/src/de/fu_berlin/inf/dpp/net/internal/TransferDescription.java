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
 * FileTransfers to a user via Socks5 or IBB.
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

    private String type;

    private String namespace;

    private String extensionVersion;

    private String sessionID;

    private JID recipient;

    private JID sender;

    private String archivePath;

    private long size;

    /**
     * Field used to indicate that the payload may be compressed.
     */
    private boolean compress;

    @Override
    public String toString() {

        if (STREAM_DATA.equals(type)) {
            return "Stream data from " + Utils.prefix(sender) + ": stream= "
                + archivePath + " [SID=" + sessionID + "]";
        } else if (STREAM_META.equals(type)) {
            return "Stream metadata from " + Utils.prefix(sender)
                + ": stream= " + archivePath + " [SID=" + sessionID + "]";
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
        out.writeUTF(description.extensionVersion != null ? description.extensionVersion
            : "");
        out.writeUTF(description.sessionID != null ? description.sessionID : "");
        out.writeUTF(description.archivePath != null ? description.archivePath
            : "");

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
        description.extensionVersion = in.readUTF();
        description.sessionID = in.readUTF();
        description.archivePath = in.readUTF();

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

    public String getExtensionVersion() {
        return extensionVersion;
    }

    TransferDescription setExtensionVersion(String extensionVersion) {
        this.extensionVersion = extensionVersion;
        return this;
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

    TransferDescription setSessionID(String sessionID) {
        this.sessionID = sessionID;
        return this;
    }

    public String getSessionID() {
        return sessionID;
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