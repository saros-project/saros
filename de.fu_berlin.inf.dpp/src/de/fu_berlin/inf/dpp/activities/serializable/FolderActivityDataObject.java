package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("folderActivity")
public class FolderActivityDataObject extends AbstractProjectActivityDataObject {

    @XStreamAsAttribute
    protected final Type type;

    public FolderActivityDataObject(JID source, Type type, SPathDataObject path) {
        super(source, path);

        this.type = type;
    }

    public Type getType() {
        return this.type;
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
        // TODO getPath() might be <code>null</code> --> possible NPE
        String thisProjectID = this.getPath().getProjectID();
        String otherProjectID = other.getPath().getProjectID();

        return thisProjectID.equals(otherProjectID)
            && other.getPath().getIPath().isPrefixOf(this.getPath().getIPath());
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
    public IActivity getActivity(ISarosSession sarosSession) {
        return new FolderActivity(sarosSession.getUser(getSource()), type,
            (getPath() != null ? getPath().toSPath(sarosSession) : null));
    }
}
