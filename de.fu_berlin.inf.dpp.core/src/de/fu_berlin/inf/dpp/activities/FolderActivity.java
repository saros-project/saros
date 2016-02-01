package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("folderActivity")
public class FolderActivity extends AbstractResourceActivity implements
    IFileSystemModificationActivity {

    public static enum Type {
        CREATED, REMOVED
    }

    @XStreamAsAttribute
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
}
