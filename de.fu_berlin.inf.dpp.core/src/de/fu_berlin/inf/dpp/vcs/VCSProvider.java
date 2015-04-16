package de.fu_berlin.inf.dpp.vcs;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.FileList;

// This interface is under development. It is designed to handle different VCS (e.g SVN, GIT, etc) on different IDE (Eclipse, IntelliJ).
public interface VCSProvider {

    /**
     * Returns the id of the provider.
     * 
     * @return id of the provider.
     */
    public String getID();

    /**
     * @param resource
     * @return <code>true</code> if the resource is under version control
     */
    public boolean isManaged(IResource resource);

    /**
     * @param resource
     * @return URL of the repository root of this resource as a String, or
     *         <code>null</code>
     */
    public String getRepositoryString(IResource resource);

    /**
     * Returns VCS specific information for the resource. For SVN, it returns
     * the actual revision of the file (last changed revision), uniquely
     * identifying the resource in the repository. E.g. if a file was last
     * changed in <b>revision 121</b>, <b>HEAD is 127</b>, and we do an
     * <b>"svn update -r 124"</b>, then this method returns <b>revision 121</b>.
     * We need this revision to detect changes: We get sync changed events only
     * if the actual revision of a resource changes.
     * 
     * @param resource
     * @return
     * @see #getCurrentResourceInfo
     */
    public VCSResourceInfo getResourceInfo(IResource resource);

    /**
     * Returns VCS specific information for the resource. For SVN, it returns
     * the revision that was used to get this file. E.g. if a file was last
     * changed in <b>revision 121, HEAD is 127</b>, and we do an
     * <b>"svn update -r 124"</b>, then this method returns <b>revision 124</b>.
     * We need this revision to replicate changes: If we used the actual
     * revision in commands, it's possible that the URL changes.
     * 
     * @param resource
     * @return
     * @see #getResourceInfo
     */
    public abstract VCSResourceInfo getCurrentResourceInfo(IResource resource);

    /**
     * @param resource
     * @return The URL of the remote resource in the repository, or null.
     */
    public String getUrl(IResource resource);

    /**
     * Connects the project to the directory in the repository.
     * 
     * @param project
     * @param repositoryRoot
     * @param directory
     * @param progress
     *            may be null.
     */
    public void connect(IProject project, String repositoryRoot,
        String directory, IProgressMonitor progress);

    /**
     * Disconnects the project from the repository.
     * 
     * @param project
     * @param deleteContent
     * @param progress
     *            may be null.
     */
    public void disconnect(IProject project, boolean deleteContent,
        IProgressMonitor progress);

    /**
     * Checks out the project specified by the {@link FileList} as a new project
     * under the provided name.
     * 
     * @param newProjectName
     * @param fileList
     * @param monitor
     * @return The newly created project.
     */
    // TODO make this method generic, replace the FileList parameter
    public IProject checkoutProject(String newProjectName, FileList fileList,
        IProgressMonitor monitor);

    /**
     * Reverts the local changes to the resource.
     * 
     * @param resource
     */
    public void revert(IResource resource);

    /**
     * Switches the resource to the specified URL and revision.
     * 
     * @param monitor
     */
    public void switch_(IResource resource, String url, String revision,
        IProgressMonitor monitor);

    /**
     * Updates the file to the specified revision.
     * 
     * @param resource
     * @param targetRevision
     * @param monitor
     */
    public void update(IResource resource, String targetRevision,
        IProgressMonitor monitor);
}
