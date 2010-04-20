package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;

@XStreamAlias("folderActivity")
public class FolderActivityDataObject extends AbstractActivityDataObject
    implements IResourceActivityDataObject {

    @XStreamAsAttribute
    protected final Type type;

    protected final SPathDataObject path;

    public FolderActivityDataObject(JID source, Type type, SPathDataObject path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    public SPathDataObject getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    public SPathDataObject getOldPath() {
        return null;
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

    public IActivity getActivity(ISharedProject sharedProject) {
        return new FolderActivity(sharedProject.getUser(source), type, path
            .toSPath(sharedProject));
    }
}
