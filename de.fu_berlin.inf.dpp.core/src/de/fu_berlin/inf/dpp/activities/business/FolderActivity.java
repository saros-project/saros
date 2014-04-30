package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.session.User;

public class FolderActivity extends AbstractResourceActivity {

    public static enum Type {
        CREATED, REMOVED
    }

    protected final Type type;

    public FolderActivity(User source, Type type, SPath path) {
        super(source, path);

        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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

        return true;
    }

    @Override
    public String toString() {
        return "FolderActivity(type: " + type + ", path: " + getPath() + ")";
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject() {
        return new FolderActivityDataObject(getSource(), type, getPath());
    }

}
