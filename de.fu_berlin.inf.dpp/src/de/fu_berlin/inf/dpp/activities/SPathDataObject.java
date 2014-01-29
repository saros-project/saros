package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;

/**
 * "Stupid" Data Object for transmitting an SPath. Serialized using XStream.
 */
@XStreamAlias("SPath")
public class SPathDataObject {

    @XStreamAlias("i")
    @XStreamAsAttribute
    protected String projectID;

    @XStreamAlias("p")
    @XStreamAsAttribute
    @XStreamConverter(IPathConverter.class)
    protected IPath path;

    @XStreamAlias("e")
    @XStreamAsAttribute
    protected String editorType;

    /**
     * Create a new SPathDataObject for project using the given global ID.
     * 
     * @param path
     *            may be <code>null</code> if this SPathDataObject represents
     *            "no editor".
     * @param projectID
     *            may be <code>null</code> if this SPathDataObject represents a
     *            resource path
     */
    public SPathDataObject(String projectID, IPath path, String editorType) {

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
     * Returns the project ID of the associated SPath.
     * 
     * @return The project ID of the associated SPath.
     * 
     * @deprecated An SPathDataObject only represents serialized data and so no
     *             logic should be performed on those objects.
     */
    @Deprecated
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
        result = prime * result + ObjectUtils.hashCode(editorType);
        result = prime * result + ObjectUtils.hashCode(path);
        result = prime * result + ObjectUtils.hashCode(projectID);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SPathDataObject))
            return false;

        SPathDataObject other = (SPathDataObject) obj;

        if (!ObjectUtils.equals(this.editorType, other.editorType))
            return false;
        if (!ObjectUtils.equals(this.path, other.path))
            return false;
        if (!ObjectUtils.equals(this.projectID, other.projectID))
            return false;

        return true;
    }
}
