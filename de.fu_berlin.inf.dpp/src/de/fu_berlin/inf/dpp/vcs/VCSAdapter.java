package de.fu_berlin.inf.dpp.vcs;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;

import de.fu_berlin.inf.dpp.activities.VCSActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.monitoring.ProgressMonitorAdapterFactory;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.project.ProjectDeltaVisitor;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Eclipse Adapter for accessing the Version Control System (Team Provider).
 * <p>
 * This adapter is capable of handling Eclipse
 * {@linkplain org.eclipse.core.resources.IResource resources} and virtual DPP
 * {@linkplain de.fu_berlin.inf.dpp.filesystem.IResource resources}
 * simultaneously.
 * <p>
 * Implementation Note: Clients <b>should</b> only implement the
 * <code>abstract</code> methods rather than implementing the
 * {@linkplain VCSProvider} interface.
 * 
 * @author haferburg
 */
// TODO Maybe change to VCProject implements IAdaptable? Make connect static.
public abstract class VCSAdapter implements VCSProvider {
    private static final Logger log = Logger.getLogger(VCSAdapter.class);

    protected RepositoryProviderType provider;

    public VCSAdapter(RepositoryProviderType provider) {
        this.provider = provider;
    }

    /**
     * @see #isManaged(de.fu_berlin.inf.dpp.filesystem.IResource)
     */

    public abstract boolean isManaged(IResource resource);

    /**
     * @param resource
     * @return<code>true</code> if the resource is in a project that's under VC.
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
     * @see #getRepositoryString(de.fu_berlin.inf.dpp.filesystem.IResource)
     */
    public abstract String getRepositoryString(IResource resource);

    /**
     * @param resource
     * @return The URL of the remote resource in the repository, or null.
     */
    public abstract String getUrl(IResource resource);

    /**
     * @see #getResourceInfo(de.fu_berlin.inf.dpp.filesystem.IResource)
     */
    public abstract VCSResourceInfo getResourceInfo(IResource resource);

    /**
     * @see #getCurrentResourceInfo(de.fu_berlin.inf.dpp.filesystem.IResource)
     */
    public abstract VCSResourceInfo getCurrentResourceInfo(IResource resource);

    /**
     * @see #checkoutProject(String, FileList,
     *      de.fu_berlin.inf.dpp.monitoring.IProgressMonitor)
     */
    public abstract IProject checkoutProject(String newProjectName,
        FileList fileList, IProgressMonitor monitor);

    /**
     * @see #connect(de.fu_berlin.inf.dpp.filesystem.IProject, String, String,
     *      de.fu_berlin.inf.dpp.monitoring.IProgressMonitor)
     */
    public abstract void connect(IProject project, String repositoryRoot,
        String directory, IProgressMonitor progress);

    /**
     * @see #disconnect(de.fu_berlin.inf.dpp.filesystem.IProject, boolean,
     *      de.fu_berlin.inf.dpp.monitoring.IProgressMonitor)
     */
    public abstract void disconnect(IProject project, boolean deleteContent,
        IProgressMonitor progress);

    /**
     * @see #revert(de.fu_berlin.inf.dpp.filesystem.IResource)
     */
    public abstract void revert(IResource resource);

    /**
     * @see #switch_(de.fu_berlin.inf.dpp.filesystem.IResource, String, String,
     *      de.fu_berlin.inf.dpp.monitoring.IProgressMonitor)
     */
    public abstract void switch_(IResource resource, String url,
        String revision, IProgressMonitor monitor);

    /**
     * @see #update(de.fu_berlin.inf.dpp.filesystem.IResource, String,
     *      de.fu_berlin.inf.dpp.monitoring.IProgressMonitor)
     */
    public abstract void update(IResource resource, String targetRevision,
        IProgressMonitor monitor);

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

    /*
     * --------------------------------------------------------------------------
     * partial VCSProvider interface implementation
     */

    @Override
    public boolean isManaged(de.fu_berlin.inf.dpp.filesystem.IResource resource) {
        return isManaged(ResourceAdapterFactory.convertBack(resource));
    }

    @Override
    public String getRepositoryString(
        de.fu_berlin.inf.dpp.filesystem.IResource resource) {
        return getRepositoryString(ResourceAdapterFactory.convertBack(resource));
    }

    @Override
    public VCSResourceInfo getResourceInfo(
        de.fu_berlin.inf.dpp.filesystem.IResource resource) {
        return getResourceInfo(ResourceAdapterFactory.convertBack(resource));
    }

    @Override
    public VCSResourceInfo getCurrentResourceInfo(
        de.fu_berlin.inf.dpp.filesystem.IResource resource) {
        return getCurrentResourceInfo(ResourceAdapterFactory
            .convertBack(resource));
    }

    @Override
    public String getUrl(de.fu_berlin.inf.dpp.filesystem.IResource resource) {
        return getUrl(ResourceAdapterFactory.convertBack(resource));
    }

    @Override
    public de.fu_berlin.inf.dpp.filesystem.IProject checkoutProject(
        String newProjectName, FileList fileList,
        de.fu_berlin.inf.dpp.monitoring.IProgressMonitor monitor) {
        return ResourceAdapterFactory.create(checkoutProject(newProjectName,
            fileList, ProgressMonitorAdapterFactory.convert(monitor)));
    }

    @Override
    public void connect(de.fu_berlin.inf.dpp.filesystem.IProject project,
        String repositoryRoot, String directory,
        de.fu_berlin.inf.dpp.monitoring.IProgressMonitor progress) {
        connect((IProject) ResourceAdapterFactory.convertBack(project),
            repositoryRoot, directory,
            ProgressMonitorAdapterFactory.convert(progress));
    }

    @Override
    public void disconnect(de.fu_berlin.inf.dpp.filesystem.IProject project,
        boolean deleteContent,
        de.fu_berlin.inf.dpp.monitoring.IProgressMonitor progress) {
        disconnect((IProject) ResourceAdapterFactory.convertBack(project),
            deleteContent, ProgressMonitorAdapterFactory.convert(progress));
    }

    @Override
    public void revert(de.fu_berlin.inf.dpp.filesystem.IResource resource) {
        revert(ResourceAdapterFactory.convertBack(resource));
    }

    @Override
    public void switch_(de.fu_berlin.inf.dpp.filesystem.IResource resource,
        String url, String revision,
        de.fu_berlin.inf.dpp.monitoring.IProgressMonitor monitor) {
        switch_(ResourceAdapterFactory.convertBack(resource), url, revision,
            ProgressMonitorAdapterFactory.convert(monitor));
    }

    @Override
    public void update(de.fu_berlin.inf.dpp.filesystem.IResource resource,
        String targetRevision,
        de.fu_berlin.inf.dpp.monitoring.IProgressMonitor monitor) {
        update(ResourceAdapterFactory.convertBack(resource), targetRevision,
            ProgressMonitorAdapterFactory.convert(monitor));
    }
}