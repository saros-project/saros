package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class FolderActivity extends AbstractActivity implements
    IResourceActivity {

    public static enum Type {
        CREATED, REMOVED
    }

    protected final Type type;
    protected final SPath path;

    public FolderActivity(User source, Type type, SPath path) {
        super(source);

        this.type = type;
        this.path = path;
    }

    @Override
    public SPath getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(path);
        result = prime * result + ObjectUtils.hashCode(type);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof FolderActivity))
            return false;

        FolderActivity other = (FolderActivity) obj;

        if (this.type != other.type)
            return false;
        if (!ObjectUtils.equals(this.path, other.path))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "FolderActivity(type: " + type + ", path: " + path + ")";
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new FolderActivityDataObject(getSource().getJID(), type,
            (path != null ? path.toSPathDataObject(sarosSession) : null));
    }

}
