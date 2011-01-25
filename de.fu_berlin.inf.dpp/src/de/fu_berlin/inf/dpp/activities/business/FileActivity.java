package de.fu_berlin.inf.dpp.activities.business;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class FileActivity extends AbstractActivity implements IResourceActivity {

    /**
     * Enumeration used to distinguish file activities which are caused as part
     * of a consistency recovery and those used as regular activities.
     */
    public static enum Purpose {
        ACTIVITY, RECOVERY;
    }

    public static enum Type {
        /** The file was created or modified, but the path stayed the same. */
        Created,
        /** The file was deleted. */
        Removed,
        /** The path of the file changed. The content might have changed, too. */
        Moved
    }

    protected final Type type;
    protected final SPath newPath;
    protected final SPath oldPath;
    protected final Purpose purpose;
    protected final byte[] data;

    /**
     * Utility method for creating a file activity of type == Created for a
     * given path.
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

        // TODO Use Eclipse Method of getting the contents of a file:
        // IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        //
        // IFile file = (IFile) project.findMember(path);
        // IPath npath = file.getProjectRelativePath();
        //
        // InputStream in = null;
        // byte[] content = null;
        // try {
        // in = file.getContents();
        // content = IOUtils.toByteArray(in);
        // } catch (CoreException e) {
        // log.warn(".created() can not get the content of "
        // + npath.toOSString());
        // } finally {
        // IOUtils.closeQuietly(in);
        // }
        //
        // return new FileActivity(source, Type.Created, npath, null,
        // content,
        // purpose);

        File f = new File(path.getFile().getLocation().toOSString());

        byte[] content = FileUtils.readFileToByteArray(f);

        return new FileActivity(source, Type.Created, path, null, content,
            purpose);
    }

    /**
     * Builder for moving files.
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
     *            destination path and sent as part of the activityDataObject.
     * @throws IOException
     *             the new content of the file could not be read
     */
    public static FileActivity moved(User source, SPath destPath,
        SPath sourcePath, boolean contentChange) throws IOException {

        byte[] content = null;
        if (contentChange) {
            // TODO File should not be read using JDK methods but must use
            // Eclipse!
            File file = new File(destPath.getFile().getLocation().toOSString());
            content = FileUtils.readFileToByteArray(file);
        }
        return new FileActivity(source, Type.Moved, destPath, sourcePath,
            content, Purpose.ACTIVITY);
    }

    /**
     * Builder for removing files
     * 
     * @param path
     *            the path of the file to remove
     */
    public static FileActivity removed(User source, SPath path, Purpose purpose) {

        return new FileActivity(source, Type.Removed, path, null, null, purpose);
    }

    /**
     * Generic constructor for {@link FileActivity}s
     * 
     * @param source
     *            JID the user who is the source (originator) of this
     *            activityDataObject
     * @param newPath
     *            where to save the data, destination of a move, file to to
     *            remove depending on type
     * @param oldPath
     *            if type == Moved, the path from where the file was moved (null
     *            otherwise)
     * @param data
     *            data of the file to be created (only valid for creating and
     *            moving)
     */
    public FileActivity(User source, Type type, SPath newPath, SPath oldPath,
        byte[] data, Purpose purpose) {

        super(source);

        if (type == null || purpose == null)
            throw new IllegalArgumentException();

        switch (type) {
        case Created:
            if (data == null || oldPath != null)
                throw new IllegalArgumentException();
            break;
        case Removed:
            if (data != null || oldPath != null)
                throw new IllegalArgumentException();
            break;
        case Moved:
            if (newPath == null || oldPath == null)
                throw new IllegalArgumentException();
            break;
        }

        this.type = type;
        this.newPath = newPath;
        this.oldPath = oldPath;
        this.data = data;
        this.purpose = purpose;
    }

    public SPath getPath() {
        return this.newPath;
    }

    /**
     * Returns the old/source path in case this activityDataObject represents a
     * moving of files.
     */
    public SPath getOldPath() {
        return this.oldPath;
    }

    public Type getType() {
        return this.type;
    }

    /**
     * @return the contents of this file for incoming file creation
     *         activityDataObjects ( {@link #getType()} == {@link Type#Created}.
     *         <code>null</code> otherwise.
     */
    public byte[] getContents() {
        return this.data;
    }

    @Override
    public String toString() {
        if (type == Type.Moved)
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

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public boolean isRecovery() {
        return Purpose.RECOVERY.equals(purpose);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new FileActivityDataObject(source.getJID(), type,
            newPath.toSPathDataObject(sarosSession),
            (oldPath != null ? oldPath.toSPathDataObject(sarosSession) : null),
            data, purpose);
    }
}
