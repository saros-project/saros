package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.project.ISharedProject;

@XStreamAlias("SPath")
public class SPathDataObject {

    @XStreamAsAttribute
    protected String projectID;

    @XStreamAsAttribute
    protected IPath path;

    @XStreamAsAttribute
    protected String editorType;

    public SPathDataObject(String projectID, IPath path, String editorType) {
        this.projectID = projectID;
        this.path = path;
        this.editorType = editorType;
    }

    public SPath toSPath(ISharedProject sharedProject) {
        return new SPath(path);
    }

    @Override
    public String toString() {
        return "SPathDataObject [editorType=" + editorType + ", path="
            + path.toPortableString() + ", projectID=" + projectID + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((editorType == null) ? 0 : editorType.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result
            + ((projectID == null) ? 0 : projectID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SPathDataObject other = (SPathDataObject) obj;
        if (editorType == null) {
            if (other.editorType != null)
                return false;
        } else if (!editorType.equals(other.editorType))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (projectID == null) {
            if (other.projectID != null)
                return false;
        } else if (!projectID.equals(other.projectID))
            return false;
        return true;
    }
}
