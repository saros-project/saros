package de.fu_berlin.inf.dpp.activities.serializable;

import java.util.Arrays;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("fileActivity")
public class FileActivityDataObject extends AbstractProjectActivityDataObject
    implements IResourceActivityDataObject {

    @XStreamAsAttribute
    protected Type type;

    protected SPathDataObject newPath;

    protected SPathDataObject oldPath;

    @XStreamAsAttribute
    protected Purpose purpose;

    protected byte[] data;

    /**
     * Generic constructor for {@link FileActivityDataObject}s
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
    public FileActivityDataObject(JID source, Type type,
        SPathDataObject newPath, SPathDataObject oldPath, byte[] data,
        Purpose purpose) {
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

    @Override
    public SPathDataObject getPath() {
        return this.newPath;
    }

    public SPathDataObject getOldPath() {
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
            return "FileActivityDataObject(type: Moved, old path: "
                + this.oldPath + ", new path: " + this.newPath + ")";
        return "FileActivityDataObject(type: " + this.type + ", path: "
            + this.newPath + ")";
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
        FileActivityDataObject other = (FileActivityDataObject) obj;
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

    public boolean isRecovery() {
        return Purpose.RECOVERY.equals(purpose);
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new FileActivity(sarosSession.getUser(source), type,
            newPath.toSPath(sarosSession),
            (oldPath != null ? oldPath.toSPath(sarosSession) : null), data,
            purpose);
    }
}
