package de.fu_berlin.inf.dpp.vcs;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ProjectDeltaVisitor;
import de.fu_berlin.inf.dpp.project.SharedProject;

/**
 * Interface to an adapter for a Version Control System (Team Provider).
 * 
 * @author haferburg
 */
// TODO Maybe change to VCProject implements IAdaptable? Make connect static.
public abstract class VCSAdapter {
    protected static final Logger log = Logger.getLogger(VCSAdapter.class);

    protected RepositoryProviderType provider;

    public VCSAdapter(RepositoryProviderType provider) {
        this.provider = provider;
    }

    /**
     * @param resource
     * @return True iff the resource is under VC.
     */
    public abstract boolean isManaged(IResource resource);

    /**
     * @param resource
     * @return True iff the resource is in a project that's under VC.
     */
    public abstract boolean isInManagedProject(IResource resource);

    /**
     * @param resource
     * @return The identifier of the resource's Team Provider.
     */
    public String getProviderID(IResource resource) {
        IProject project = resource.getProject();
        RepositoryProvider provider = RepositoryProvider.getProvider(project);
        String vcsIdentifier = provider.getID();

        return vcsIdentifier;
    }

    /**
     * @param resource
     * @return The revision of the resource as a String, or null.
     */
    public String getRevisionString(IResource resource) {
        Subscriber subscriber = provider.getSubscriber();
        SyncInfo syncInfo;
        try {
            syncInfo = subscriber.getSyncInfo(resource);
        } catch (TeamException e) {
            undocumentedException(e);
            return null;
        }
        if (syncInfo == null)
            return null;
        return syncInfo.getLocalContentIdentifier();
    }

    /**
     * @param resource
     * @return The URL of the repository root of this resource as a String, or
     *         null.
     */
    public abstract String getRepositoryString(IResource resource);

    /**
     * @param resource
     * @return The URL of the remote resource in the repository, or null.
     */
    public abstract String getUrl(IResource resource);

    /**
     * Checks out the project specified by the {@link FileList} as a new project
     * under the provided name.
     * 
     * @param newProjectName
     * @param fileList
     * @param monitor
     * @return The newly created project.
     */
    public abstract IProject checkoutProject(String newProjectName,
        FileList fileList, SubMonitor monitor);

    /**
     * Updates the file to the specified revision.
     * 
     * @param resource
     * @param targetRevision
     * @param monitor
     *            must not be null.
     */
    public abstract void update(IResource resource, String targetRevision,
        IProgressMonitor monitor);

    /**
     * Switches the resource to the specified URL and revision.
     * 
     * @param monitor
     */
    public abstract void switch_(IResource resource, String url,
        String revision, IProgressMonitor monitor);

    /**
     * Reverts the local changes to the resource.
     * 
     * @param monitor
     */
    public abstract void revert(IResource resource, SubMonitor monitor);

    /**
     * Returns VCS specific information for the resource. For SVN, it returns
     * the actual revision of the file (last changed revision), uniquely
     * identifying the resource in the repository. E.g. if a file was last
     * changed in revision 121, HEAD is 127, and we do an "svn update -r 124",
     * then this method returns revision 121. We need this revision to detect
     * changes: We get sync changed events only if the actual revision of a
     * resource changes.
     * 
     * @param resource
     * @return
     */
    public abstract VCSResourceInfo getResourceInfo(IResource resource);

    /**
     * Returns VCS specific information for the resource. For SVN, it returns
     * the revision that was used to get this file. E.g. if a file was last
     * changed in revision 121, HEAD is 127, and we do an "svn update -r 124",
     * then this method returns revision 124. We need this revision to replicate
     * changes: If we used the actual revision in commands, it's possible that
     * the URL changes.
     * 
     * @param resource
     * @return
     */
    public abstract VCSResourceInfo getCurrentResourceInfo(IResource resource);

    /**
     * Connects the project to the directory in the repository.
     * 
     * @param project
     * @param repositoryRoot
     * @param directory
     * @param progress
     *            may be null.
     */
    public abstract void connect(IProject project, String repositoryRoot,
        String directory, IProgressMonitor progress);

    /**
     * Disconnects the project from the repository.
     * 
     * @param project
     * @param deleteContent
     * @param progress
     *            may be null.
     */
    public abstract void disconnect(IProject project, boolean deleteContent,
        IProgressMonitor progress);

    /**
     * Returns true if there is a folder like e.g. SVN's .svn for the project.
     * Such a folder might exists even when the project is not currently
     * connected to the Team provider.
     */
    public abstract boolean hasLocalCache(IProject project);

    /** It is unclear under which circumstances this exception is thrown. */
    protected void undocumentedException(Exception e) {
        log.error("Undocumented exception", e);
    }

    /**
     * Determine and instantiate the corresponding {@link VCSAdapter} for the
     * provided identifier.<br>
     * 
     * @param identifier
     * @return
     * @see RepositoryProvider#getID()
     */
    public static VCSAdapter getAdapter(String identifier) {
        if (identifier == null)
            return null;
        RepositoryProviderType provider = RepositoryProviderType
            .getProviderType(identifier);
        try {
            if (identifier.equals(SubclipseAdapter.identifier)) {
                return new SubclipseAdapter(provider);
            }
        } catch (NoClassDefFoundError e) {
            // TODO Should we inform the user?
            log.warn("Could not find a VCS adapter for \"" + identifier + "\".");
        }
        return null;
    }

    public abstract VCSActivity getUpdateActivity(ISarosSession sarosSession,
        IResource resource);

    public abstract VCSActivity getSwitchActivity(ISarosSession sarosSession,
        IResource resource);

    /**
     * Determine the repository provider of the project and return the
     * corresponding {@link VCSAdapter}. The method will return
     * <code>null</code> if the project is not under version control, or if no
     * <code>VCSAdapter</code> was found for the repository provider used.
     * 
     * @param project
     * @return
     */
    public static VCSAdapter getAdapter(IProject project) {
        boolean underVCS;
        underVCS = RepositoryProvider.isShared(project);
        if (!underVCS)
            return null;

        RepositoryProvider provider = RepositoryProvider.getProvider(project);
        final VCSAdapter adapter = getAdapter(provider.getID());
        return adapter;
    }

    public ProjectDeltaVisitor getProjectDeltaVisitor(
        EditorManager editorManager, ISarosSession sarosSession,
        SharedProject sharedProject) {
        return new ProjectDeltaVisitor(editorManager, sarosSession,
            sharedProject);
    }
}