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

    private String type;

    private String namespace;

    private JID recipient;

    private JID sender;

    private long size;

    /**
     * Field used to indicate that the payload may be compressed.
     */
    private boolean compress;

    @Override
    public String toString() {
        return "Bytestream transfer. type=" + type + " namespace=" + namespace;
    }

    public static TransferDescription createCustomTransferDescription() {
        return new TransferDescription();
    }

    public static byte[] toByteArray(TransferDescription description)
        throws IOException {
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();

        DataOutputStream out = new DataOutputStream(serialized);

        out.writeUTF(description.type != null ? description.type : "");
        out.writeUTF(description.namespace != null ? description.namespace : "");

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