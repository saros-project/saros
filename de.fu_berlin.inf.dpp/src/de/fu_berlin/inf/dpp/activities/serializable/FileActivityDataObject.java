package de.fu_berlin.inf.dpp.activities.serializable;

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

    protected SPathDataObject oldPath;

    @XStreamAsAttribute
    protected Purpose purpose;

    protected byte[] data;

    protected Long checksum;

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
        Purpose purpose, Long checksum) {
        super(source, newPath);

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
        this.oldPath = oldPath;
        this.data = data;
        this.purpose = purpose;
        this.checksum = checksum;
    }

    public SPathDataObject getOldPath() {
        return this.oldPath;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        if (type == Type.Moved)
            return "FileActivityDataObject(type: Moved, old path: "
                + this.oldPath + ", new path: " + this.path + ")";
        return "FileActivityDataObject(type: " + this.type + ", path: "
            + this.path + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new FileActivity(sarosSession.getUser(source), type,
            path.toSPath(sarosSession),
            (oldPath != null ? oldPath.toSPath(sarosSession) : null), data,
            purpose, checksum);
    }
}
