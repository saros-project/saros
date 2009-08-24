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
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.CausedIOException;

/**
 * A TransferDescription contains all necessary information for dealing with
 * FileTransfers to a remote user via Jingle, IBB or Chat.
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

    public static enum FileTransferType {
        /**
         * Transfer of a FileList
         */
        FILELIST_TRANSFER,
        /**
         * Transfer of a single File/Resource
         */
        RESOURCE_TRANSFER,
        /**
         * Transfer of several resources in a ZIP-File
         */
        ARCHIVE_TRANSFER,
        /**
         * Transfer of an ActivityExtension as XML that was serialized using
         * UTF-8 and GZIPped
         */
        ACTIVITY_TRANSFER
    }

    public FileTransferType type;

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

    @Override
    public String toString() {

        switch (type) {
        case ARCHIVE_TRANSFER:
            return "Archive from " + getSender() + " [SID=" + sessionID + "]";
        case FILELIST_TRANSFER:
            return "FileList from " + getSender() + " [SID=" + sessionID + "]";
        case RESOURCE_TRANSFER:
            return "Resource from " + getSender() + ": " + file_project_path
                + " [SID=" + sessionID + "]";
        case ACTIVITY_TRANSFER:
            return "Activity from " + getSender() + ": [SID=" + sessionID + "]";
        default:
            return "Not a valid FileTransferType";
        }
    }

    public static TransferDescription createFileListTransferDescription(
        JID recipient, JID sender, String sessionID) {
        TransferDescription result = new TransferDescription();
        result.sender = sender;
        result.recipient = recipient;
        result.type = FileTransferType.FILELIST_TRANSFER;
        result.sessionID = sessionID;
        result.compressed = false;
        return result;
    }

    public static TransferDescription createFileTransferDescription(
        JID recipient, JID sender, IPath path, String sessionID) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.type = FileTransferType.RESOURCE_TRANSFER;
        result.file_project_path = path.toPortableString();
        result.sessionID = sessionID;

        // Only compress if not a file-type which is already commonly compressed
        result.compressed = compressedSet.contains(path.getFileExtension());

        return result;
    }

    public static TransferDescription createArchiveTransferDescription(
        JID recipient2, JID jid, String sessionID) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient2;
        result.sender = jid;
        result.type = FileTransferType.ARCHIVE_TRANSFER;
        result.sessionID = sessionID;
        result.compressed = true;

        return result;
    }

    public static TransferDescription createActivityTransferDescription(
        JID recipient, JID sender, String sessionID) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.type = FileTransferType.ACTIVITY_TRANSFER;
        result.sessionID = sessionID;
        result.compressed = false;

        return result;
    }

    public String toBase64() {

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
                "Could not serialize: ObjectOutputStream failed");
        }

        byte[] bytes64 = Base64.encodeBase64(os.toByteArray());

        try {
            return new String(bytes64, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should not happen
            throw new RuntimeException(
                "Could not serialize: UTF-8 not available");
        }
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