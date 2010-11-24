package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Objects of this class point to a document in the *workspace*, possibly with
 * information about a specific open editor for that document.
 * 
 * An SPath consists of an IProject reference, IPath reference and editorType
 * identifier.
 * 
 * @immutable SPaths are value objects and thus immutable.
 */
public class SPath {

    /**
     * The project relative path of the resource or editor this SPath
     * represents.
     */
    protected IPath projectRelativePath;

    /**
     * The local IProject in which the document is contained which this SPath
     * represents
     */
    protected IProject project;

    /**
     * Type of the editor (plain text, Java, XML, ...).
     * 
     * Maybe <code>null</code>, when the SPath addresses a document and not a
     * specific editor.
     * 
     * @TODO Change to something that can be used to identify different editor
     *       types in Eclipse.
     */
    protected String editorType = "txt";

    /**
     * Default constructor, initializing this SPath as a reference to the
     * resource or editor identified by the given path in the given project.
     * 
     * A Null path is allowed for representing no editor
     */
    public SPath(IProject project, @Nullable IPath path) {
        if (project == null)
            throw new IllegalArgumentException(
                "SPath must be initialized with an IProject");

        this.project = project;
        this.projectRelativePath = path;
    }

    /**
     * Convenience constructor, which retrieves path and project from the given
     * resource
     */
    public SPath(IResource resource) {
        this(resource.getProject(), resource.getProjectRelativePath());
    }

    /**
     * Turns this SPath into an SPathDataObject representing it globally.
     */
    public SPathDataObject toSPathDataObject(ISarosSession sarosSession) {

        String id = sarosSession.getProjectID(project);
        if (id == null)
            throw new IllegalArgumentException(
                "Trying to send a SPath which refers to a file in project which is not shared: "
                    + this);

        return new SPathDataObject(id, projectRelativePath, editorType);
    }

    /**
     * Returns the project relative path of the resource or editor represented
     * by this SPath.
     * 
     * @return May return null if this SPath is used to represent no editor.
     */
    public IPath getProjectRelativePath() {
        return projectRelativePath;
    }

    /**
     * Return the identifier of the editor which this SPath references. This
     * identifier should be used to select one of several editors displaying the
     * same resource.
     * 
     * TODO Make use of this information
     */
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
        result = prime * result + ((project == null) ? 0 : project.hashCode());
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
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SPath [editorType="
            + editorType
            + ", path="
            + (projectRelativePath != null ? projectRelativePath
                .toPortableString() : "<no path>") + ", project="
            + project.getName() + "]";
    }

    /**
     * Returns the IFile represented by this SPath.
     * 
     * @return the IFile contained in the associated IProject for the given
     *         project relative path
     * 
     * @convenience This method is using a straight forward implementation
     */
    public IFile getFile() {
        return project.getFile(projectRelativePath);
    }

    /**
     * Returns the IResource represented by this SPath.
     */
    public IResource getResource() {
        return project.findMember(projectRelativePath);
    }

    /**
     * Returns the project in which the referenced resource or editor is
     * located.
     */
    public IProject getProject() {
        return project;
    }

    /**
     * Convenience method for getting the full (workspace-relative) path of the
     * file/editor identified by this SPath.
     */
    public IPath getFullPath() {
        final IPath fullProjectPath = project.getFullPath();
        return fullProjectPath.append(projectRelativePath);
    }
}
