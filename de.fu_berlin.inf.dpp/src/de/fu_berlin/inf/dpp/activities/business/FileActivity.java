package de.fu_berlin.inf.dpp.activities.business;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.FileUtils;

public class FileActivity extends AbstractActivity implements IResourceActivity {

    /**
     * Enumeration used to distinguish file activities which are caused as part
     * of a consistency recovery and those used as regular activities.
     */
    public static enum Purpose {
        ACTIVITY, RECOVERY, NEEDS_BASED_SYNC;
    }

    public static enum Type {
        /** The file was created or modified, but the path stayed the same. */
        CREATED,
        /** The file was deleted. */
        REMOVED,
        /** The path of the file changed. The content might have changed, too. */
        MOVED
    }

    protected final Type type;
    protected final SPath newPath;
    protected final SPath oldPath;
    protected final Purpose purpose;
    protected final byte[] data;
    protected final Long checksum;

    /**
     * Utility method for creating a FileActivity of type {@link Type#CREATED}
     * for a given path.
     * 
     * This method will make a snapshot copy of the file at this point in time.
     * 
     * @param path
     *            The path of the file to copy the data from.
     * @throws IOException
     *             If an error occurs while reading the file.
     */
    public static FileActivity created(User source, SPath path, Purpose purpose)
        throws IOException {

        IFile file = path.getFile();
        Long checksum = FileUtils.checksum(file);
        byte[] content = FileUtils.getLocalFileContent(file);

        return new FileActivity(source, Type.CREATED, path, null, content,
            purpose, checksum);
    }

    /**
     * Builder for moving files (type {@link Type#MOVED}).
     * 
     * @param source
     *            JID of the origin user
     * 
     * @param destPath
     *            path where the file moved to
     * @param sourcePath
     *            path where the file moved from
     * @param contentChange
     *            if true, a snapshot copy is made of the file at the
     *            destination path and sent as part of the Activity.
     * @throws IOException
     *             the new content of the file could not be read
     */
    public static FileActivity moved(User source, SPath destPath,
        SPath sourcePath, boolean contentChange) throws IOException {

        byte[] content = null;
        if (contentChange) {
            content = FileUtils.getLocalFileContent(destPath.getFile());
        }
        return new FileActivity(source, Type.MOVED, destPath, sourcePath,
            content, Purpose.ACTIVITY, null);
    }

    /**
     * Builder for removing files (type {@link Type#REMOVED})
     * 
     * @param path
     *            the path of the file to remove
     */
    public static FileActivity removed(User source, SPath path, Purpose purpose) {

        return new FileActivity(source, Type.REMOVED, path, null, null,
            purpose, null);
    }

    /**
     * Generic constructor for {@link FileActivity}s
     * 
     * @param source
     *            the user who is the source (originator) of this Activity
     * @param newPath
     *            where to save the data (if {@link Type#CREATED}), destination
     *            of a move (if {@link Type#MOVED}), file to remove (if
     *            {@link Type#REMOVED})
     * @param oldPath
     *            if type is {@link Type#MOVED}, the path from where the file
     *            was moved (<code>null</code> otherwise)
     * @param data
     *            data of the file to be created (only valid for
     *            {@link Type#CREATED} and {@link Type#MOVED})
     */
    public FileActivity(User source, Type type, SPath newPath, SPath oldPath,
        byte[] data, Purpose purpose, Long checksum) {

        super(source);

        if (type == null || purpose == null)
            throw new IllegalArgumentException();

        switch (type) {
        case CREATED:
            if (data == null || oldPath != null)
                throw new IllegalArgumentException();
            break;
        case REMOVED:
            if (data != null || oldPath != null)
                throw new IllegalArgumentException();
            break;
        case MOVED:
            if (newPath == null || oldPath == null)
                throw new IllegalArgumentException();
            break;
        }

        this.type = type;
        this.newPath = newPath;
        this.oldPath = oldPath;
        this.data = data;
        this.purpose = purpose;
        this.checksum = checksum;
    }

    @Override
    public SPath getPath() {
        return this.newPath;
    }

    /**
     * Returns the old/source path in case this Activity represents a moving of
     * files.
     */
    public SPath getOldPath() {
        return this.oldPath;
    }

    public Type getType() {
        return this.type;
    }

    /**
     * @return the contents of this file for incoming file creation Activities (
     *         if {@link #getType()} == {@link Type#CREATED}; <code>null</code>
     *         otherwise.
     */
    public byte[] getContents() {
        return this.data;
    }

    @Override
    public String toString() {
        if (type == Type.MOVED)
            return "FileActivity(type: Moved, old path: " + this.oldPath
                + ", new path: " + this.newPath + ")";
        return "FileActivity(type: " + this.type + ", path: " + this.newPath
            + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + ((oldPath == null) ? 0 : oldPath.hashCode());
        result = prime * result + ((newPath == null) ? 0 : newPath.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileActivity other = (FileActivity) obj;
        if (oldPath == null) {
            if (other.oldPath != null)
                return false;
        } else if (!oldPath.equals(other.oldPath))
            return false;
        if (newPath == null) {
            if (other.newPath != null)
                return false;
        } else if (!newPath.equals(other.newPath))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public boolean isRecovery() {
        return Purpose.RECOVERY.equals(purpose);
    }

    public boolean isNeedBased() {
        return Purpose.NEEDS_BASED_SYNC.equals(purpose);
    }

    public long getChecksum() {
        return checksum;
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new FileActivityDataObject(source.getJID(), type,
            newPath.toSPathDataObject(sarosSession),
            (oldPath != null ? oldPath.toSPathDataObject(sarosSession) : null),
            data, purpose, checksum);
    }
}
