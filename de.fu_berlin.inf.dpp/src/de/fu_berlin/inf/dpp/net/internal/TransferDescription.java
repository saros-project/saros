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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;

public class TransferDescription implements Serializable {

    private static final long serialVersionUID = -4063208452619555716L;

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
        ARCHIVE_TRANSFER
    }

    public FileTransferType type;

    public JID recipient;

    public JID sender;

    public int timestamp;

    public String file_project_path;

    @Override
    public String toString() {

        switch (type) {
        case ARCHIVE_TRANSFER:
            return "Archive from " + getSender();
        case FILELIST_TRANSFER:
            return "FileList from " + getSender();
        case RESOURCE_TRANSFER:
            return "Resource from " + getSender() + ": " + file_project_path;
        default:
            return "Not a valid FileTransferType";
        }
    }

    public static TransferDescription createFileListTransferDescription(
        JID recipient, JID sender) {
        TransferDescription result = new TransferDescription();
        result.sender = sender;
        result.recipient = recipient;
        result.type = FileTransferType.FILELIST_TRANSFER;
        return result;
    }

    public static TransferDescription createFileTransferDescription(
        JID recipient, JID sender, IPath path, int timestamp) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient;
        result.sender = sender;
        result.type = FileTransferType.RESOURCE_TRANSFER;
        result.file_project_path = path.toPortableString();
        result.timestamp = timestamp;
        return result;
    }

    public static TransferDescription createArchiveTransferDescription(
        JID recipient2, JID jid) {

        TransferDescription result = new TransferDescription();
        result.recipient = recipient2;
        result.sender = jid;
        result.type = FileTransferType.ARCHIVE_TRANSFER;

        return result;
    }

    public String toBase64() {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ObjectOutputStream object = null;
        try {
            object = new ObjectOutputStream(os);
            object.writeObject(this);
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException("Could not serialize");
        } finally {
            IOUtils.closeQuietly(object);
            IOUtils.closeQuietly(os);
        }

        byte[] bytes64 = Base64.encodeBase64(os.toByteArray());

        try {
            return new String(bytes64, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes64);
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
                throw new IOException("Invalid Object sent", e);
            }
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

    }

    public JID getSender() {
        return sender;
    }
}