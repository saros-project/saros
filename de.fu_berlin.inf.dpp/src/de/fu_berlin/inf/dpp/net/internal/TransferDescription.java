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
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.CausedIOException;
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
    }

    /**
     * File extensions which we take to be already compressed so that Saros does
     * not attempt to compress them again.
     */
    protected static final HashSet<String> compressedSet = new HashSet<String>();
    static {
        compressedSet.add("zip");
        compressedSet.add("jar");
        compressedSet.add("jpg");
        compressedSet.add("jpeg");
        compressedSet.add("png");
        compressedSet.add("gif");
        compressedSet.add("war");
        compressedSet.add("ear");
        compressedSet.add("gz");
        compressedSet.add("gzip");
        compressedSet.add("bz");
        compressedSet.add("bz2");
        compressedSet.add("tgz");
        compressedSet.add("pkg");
        compressedSet.add("7z");
    }

    public static class FileTransferType {
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
    }

    public String type;

    public String namespace;

    public String sessionID;

    public JID recipient;

    public JID sender;

    public String file_project_path;

    /**
     * Field used to indicate that the file is to be interpreted as being empty,
     * if the transfer method does not support files of length 0.
     */
    public boolean emptyFile = false;

    /**
     * Field used to indicate that the file is compressed already, like in
     * ARCHIVE_TRANSFER or RESOURCE_TRANSFER with a File like a jar, jpeg, ....
     */
    public boolean compressed;

    /**
     * This field is set to true if we are in an invitation process
     */
    public boolean invitation;

    /**
     * This field is set to true if we are in an invitation process
     */
    public boolean logToDebug = true;

    /**
     * The invitationID of this TransferDescription or null if this
     * TransferDescription is not used during an invitation.
     */
    public String invitationID = null;

    /**
     * This field is for internal use by BinaryChannel to identify an object.
     */
    public int objectid = -1;

    /**
     * If this TransferDescription is of type
     * {@link FileTransferType#CONNECTION_TEST} then testID is set to the ID of
     * the IQ packet to send in reply to receiving test data.
     * 
     * testID is null if this TransferDescription is not of type
     * {@link FileTransferType#CONNECTION_TEST}
     */
    protected String testID = null;

    /**
     * The processID of this TransferDescription or null if this
     * TransferDescription is not used during project exchange.
     */
    public String processID = null;

    @Override
    public String toString() {

        if (FileTransferType.ARCHIVE_TRANSFER.equals(type)) {
            return "Archive from " + Utils.prefix(getSender()) + " [SID="
                + sessionID + "]";
        } else if (FileTransferType.FILELIST_TRANSFER.equals(type)) {
            return "FileList from " + Utils.prefix(getSender()) + " [SID="
                + sessionID + "]";
        } else if (FileTransferType.STREAM_DATA.equals(type)) {
            return "Stream data from " + Utils.prefix(getSender())
                + ": stream= " + file_project_path + " [SID=" + sessionID + "]";
        } else if (FileTransferType.STREAM_META.equals(type)) {
            return "Stream metadata from " + Utils.prefix(getSender())
                + ": stream= " + file_project_path + " [SID=" + sessionID + "]";
        } else if (FileTransferType.CONNECTION_TEST.equals(type)) {
            return "Connection test from " + Utils.prefix(getSender());
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
        result.type = FileTransferType.FILELIST_TRANSFER;
        result.sessionID = sessionID;
        result.processID = processID;
        result.compressed = false;
        return result;
    }

    public static TransferDescription createTestTransferDescription(
        JID recipient, String testID, JID sender) {
        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.testID = testID;
        result.type = FileTransferType.CONNECTION_TEST;
        return result;
    }

    public static TransferDescription createArchiveTransferDescription(
        JID recipient2, JID jid, String sessionID, String invitationID) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient2;
        result.sender = jid;
        result.type = FileTransferType.ARCHIVE_TRANSFER;
        result.sessionID = sessionID;
        result.invitationID = invitationID;
        result.compressed = true;

        return result;
    }

    public static TransferDescription createStreamDataTransferDescription(
        JID recipient, JID sender, String sessionID, String streamPath) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.type = FileTransferType.STREAM_DATA;
        result.sessionID = sessionID;
        result.file_project_path = streamPath;
        result.compressed = true;
        result.logToDebug = false;

        return result;
    }

    public static TransferDescription createStreamMetaTransferDescription(
        JID recipient, JID sender, String streamPath, String sessionID) {
        TransferDescription result = createStreamDataTransferDescription(
            recipient, sender, sessionID, streamPath);

        result.type = FileTransferType.STREAM_META;

        return result;
    }

    public String toBase64() {

        byte[] bytes64 = Base64.encodeBase64(toByteArray());

        try {
            return new String(bytes64, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should not happen
            throw new RuntimeException(
                "Could not serialize: UTF-8 not available");
        }
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

    public JID getRecipient() {
        return recipient;
    }

    public static TransferDescription fromBase64(String description)
        throws IOException {

        byte[] dataOrg;
        try {
            dataOrg = Base64.decodeBase64(description.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            dataOrg = Base64.decodeBase64(description.getBytes());
        }

        ByteArrayInputStream is = new ByteArrayInputStream(dataOrg);

        ObjectInputStream os = null;
        try {
            os = new ObjectInputStream(is);
            try {
                return (TransferDescription) os.readObject();
            } catch (ClassNotFoundException e) {
                throw new CausedIOException("Invalid Object sent", e);
            }
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
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
                "Could not serialize: ObjectOutputStream failed");
        }
    }

    public JID getSender() {
        return sender;
    }

    public void setEmptyFile(boolean b) {
        emptyFile = true;
    }

    public boolean compressInDataTransferManager() {
        return !compressed;
    }

}