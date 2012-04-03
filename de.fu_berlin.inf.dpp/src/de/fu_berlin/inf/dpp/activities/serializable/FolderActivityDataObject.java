package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("folderActivity")
public class FolderActivityDataObject extends AbstractProjectActivityDataObject
    implements IResourceActivityDataObject {

    @XStreamAsAttribute
    protected final Type type;

    protected final SPathDataObject path;

    public FolderActivityDataObject(JID source, Type type, SPathDataObject path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    @Override
    public SPathDataObject getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    public SPathDataObject getOldPath() {
        return null;
    }

    /**
     * Tests whether this FolderActivity operates on a folder that is the child
     * of another in the same project.
     * 
     * @param other
     *            The other folder to be tested against.
     * @return <code>true</code> if <code>other</code> operates on a parent
     *         folder in the same project as this activity's folder.
     */
    public boolean isChildOf(FolderActivityDataObject other) {
        String thisProjectID = this.path.getProjectID();
        String otherProjectID = other.path.getProjectID();

        return thisProjectID.equals(otherProjectID)
            && other.getPath().getIPath().isPrefixOf(this.path.getIPath());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        FolderActivityDataObject other = (FolderActivityDataObject) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FolderActivityDataObject(type: " + type + ", path: " + path
            + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new FolderActivity(sarosSession.getUser(source), type,
            path.toSPath(sarosSession));
    }
}
