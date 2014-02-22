package de.fu_berlin.inf.dpp.activities.serializable;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("fileActivity")
public class FileActivityDataObject extends AbstractProjectActivityDataObject {

    @XStreamAsAttribute
    protected Type type;

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

        super(source, newPath);

        this.type = type;
        this.oldPath = oldPath;
        this.data = data;
        this.purpose = purpose;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        if (type == Type.MOVED)
            return "FileActivityDO(type: Moved, old path: " + oldPath
                + ", new path: " + getPath() + ")";

        return "FileActivityDO(type: " + type + ", path: " + getPath() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + ObjectUtils.hashCode(oldPath);
        result = prime * result + ObjectUtils.hashCode(type);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof FileActivityDataObject))
            return false;

        FileActivityDataObject other = (FileActivityDataObject) obj;

        if (!ObjectUtils.equals(this.type, other.type))
            return false;
        if (!ObjectUtils.equals(this.oldPath, other.oldPath))
            return false;
        if (!Arrays.equals(data, other.data))
            return false;

        return true;
    }

    public boolean isRecovery() {
        return Purpose.RECOVERY.equals(purpose);
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        SPathDataObject newPath = getPath();
        return new FileActivity(sarosSession.getUser(source), type,
            (newPath != null ? newPath.toSPath(sarosSession, pathFactory)
                : null), (oldPath != null ? oldPath.toSPath(sarosSession,
                pathFactory) : null), data, purpose);
    }
}
