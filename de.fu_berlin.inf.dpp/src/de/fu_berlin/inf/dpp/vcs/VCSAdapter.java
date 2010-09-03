package de.fu_berlin.inf.dpp.vcs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.FileList;

/**
 * Interface to an adapter for a Version Control System (Team Provider).
 * 
 * @author haferburg
 */
public interface VCSAdapter {
    /**
     * @param resource
     * @return True iff the resource is under VC.
     */
    public boolean isManaged(IResource resource);

    /**
     * @param resource
     * @return True iff the resource is in a project that's under VC.
     */
    public boolean isInManagedProject(IResource resource);

    /**
     * @param resource
     * @return The identifier of the resource's Team Provider.
     */
    public String getProviderID(IResource resource);

    /**
     * @param resource
     * @return The revision of the resource as a String, or null.
     */
    public String getRevisionString(IResource resource);

    /**
     * @param resource
     * @return The URL of the repository containing this resource as a String,
     *         or null.
     */
    public String getRepositoryString(IResource resource);

    /**
     * @param resource
     * @return The path of the resource relative to the repository, or null.
     */
    public String getProjectPath(IResource resource);

    /**
     * Checks out the project specified by the {@link FileList} as a new project
     * under the provided name.
     * 
     * @param newProjectName
     * @param fileList
     * @param monitor
     * @return The newly created project.
     */
    public IProject checkoutProject(String newProjectName, FileList fileList,
        SubMonitor monitor);

    /**
     * Updates the file to the specified revision.
     * 
     * @param file
     * @param targetRevision
     * @param monitor
     */
    public void update(IFile file, String targetRevision,
        IProgressMonitor monitor);

    /**
     * Switches the resource to the specified URL and revision.
     * 
     * @param monitor
     */
    public void switch_(IResource resource, String url, String revision,
        IProgressMonitor monitor);

    /**
     * Creates project specific VCS information.
     * 
     * @param project
     * @return
     */
    public VCSProjectInformation getProjectInformation(IProject project);
}