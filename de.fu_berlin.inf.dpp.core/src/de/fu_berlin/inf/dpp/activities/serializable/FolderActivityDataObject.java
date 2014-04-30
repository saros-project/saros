package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;

@XStreamAlias("folderActivity")
public class FolderActivityDataObject extends AbstractProjectActivityDataObject {

    @XStreamAsAttribute
    protected final Type type;

    public FolderActivityDataObject(JID source, Type type, SPath path) {
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
        if (!(obj instanceof FolderActivityDataObject))
            return false;

        FolderActivityDataObject other = (FolderActivityDataObject) obj;

        if (!ObjectUtils.equals(this.type, other.type))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "FolderActivityDO(type: " + type + ", path: " + getPath() + ")";
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new FolderActivity(sarosSession.getUser(getSource()), type,
            getPath());
    }
}
