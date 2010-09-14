package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.annotations.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * "Stupid" Data Object for transmitting an SPath. Serialized using XStream.
 */
@XStreamAlias("SPath")
public class SPathDataObject {

    @XStreamAsAttribute
    protected String projectID;

    @XStreamAsAttribute
    protected IPath path;

    @XStreamAsAttribute
    protected String editorType;

    /**
     * Create a new SPathDataObject for project using the given global ID.
     * 
     * Path may be null if this SPathDataObject represents "no editor".
     * 
     * editorType may be null if this SPathDataObject represents a resource
     * path.
     */
    public SPathDataObject(String projectID, @Nullable IPath path,
        @Nullable String editorType) {

        this.projectID = projectID;
        this.path = path;
        this.editorType = editorType;
    }

    /**
     * Attach this SPathDataObject to the given ISarosSession. This will map
     * projectIDs of the SPathDataObject to actual IProjects.
     */
    public SPath toSPath(ISarosSession sarosSession) {

        IProject project = sarosSession.getProject(projectID);
        if (project == null)
            throw new IllegalArgumentException(
                "SPathDataObject cannot be connected to SarosSession because its ID is unknown: "
                    + projectID);

        return new SPath(project, path);
    }

    /**
     * Returns the IPath associated with this SPath.
     * 
     * @return The IPath associated with this SPath.
     */
    public IPath getIPath() {
        return path;
    }

    /**
     * Returns the project ID of the associated SPath.
     * 
     * @return The project ID of the associated SPath.
     */
    public String getProjectID() {
        return projectID;
    }

    @Override
    public String toString() {
        return "SPathDataObject [editorType=" + editorType + ", path="
            + (path != null ? path.toPortableString() : null) + ", projectID="
            + projectID + "]";
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
