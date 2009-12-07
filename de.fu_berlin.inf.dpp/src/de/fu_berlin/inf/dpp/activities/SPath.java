package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

/**
 * This class represents a document in the workspace, possibly with information
 * about a specific open editor for that document.
 */
public class SPath {
    /**
     * Global project ID.
     * 
     * @TODO Use a real project ID.
     */
    String projectID = "42";

    /** Project relative path. */
    IPath projectRelativePath;

    /**
     * Type of the editor (plain text, Java, XML, ...).
     * 
     * Maybe <code>null</code>, when the SPath addresses a document and not a
     * specific editor.
     * 
     * @TODO Change to something that can be used to identify different editor
     *       types in Eclipse.
     */
    String editorType = "txt";

    public SPath(IPath path) {
        this.projectRelativePath = path;
    }

    public SPathDataObject toSPathDataObject() {
        return new SPathDataObject(projectID, projectRelativePath, editorType);
    }

    public String getProjectID() {
        return projectID;
    }

    public IPath getProjectRelativePath() {
        return projectRelativePath;
    }

    public String getEditorType() {
        return editorType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((editorType == null) ? 0 : editorType.hashCode());
        result = prime
            * result
            + ((projectRelativePath == null) ? 0 : projectRelativePath
                .hashCode());
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
        SPath other = (SPath) obj;
        if (editorType == null) {
            if (other.editorType != null)
                return false;
        } else if (!editorType.equals(other.editorType))
            return false;
        if (projectRelativePath == null) {
            if (other.projectRelativePath != null)
                return false;
        } else if (!projectRelativePath.equals(other.projectRelativePath))
            return false;
        if (projectID == null) {
            if (other.projectID != null)
                return false;
        } else if (!projectID.equals(other.projectID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SPath [editorType="
            + editorType
            + ", path="
            + (projectRelativePath != null ? projectRelativePath
                .toPortableString() : "<no path>") + ", projectID=" + projectID
            + "]";
    }
}
