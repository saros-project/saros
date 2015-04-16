package de.fu_berlin.inf.dpp.vcs;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.ParseException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.commands.SwitchToUrlCommand;
import org.tigris.subversion.subclipse.core.commands.UpdateResourcesCommand;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.operations.CheckoutAsProjectOperation;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import de.fu_berlin.inf.dpp.activities.VCSActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.project.ProjectDeltaVisitor;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Adapter for Subclipse 1.6.
 */
class SubclipseAdapter extends VCSAdapter {
    static final String identifier = "org.tigris.subversion.subclipse.core.svnnature";

    protected static final Logger log = Logger
        .getLogger(SubclipseAdapter.class);

    public SubclipseAdapter(RepositoryProviderType provider) {
        super(provider);
    }

    @Override
    public boolean isInManagedProject(IResource resource) {
        boolean underVCS;
        IProject project = resource.getProject();
        underVCS = RepositoryProvider.isShared(project);
        if (!underVCS)
            return false;

        return identifier.equals(getProviderID(resource));
    }

    @Override
    public boolean isManaged(IResource resource) {
        ISVNLocalResource svnResource = SVNWorkspaceRoot
            .getSVNResourceFor(resource);
        if (svnResource == null)
            return false;
        boolean result = false;
        try {
            result = svnResource.isManaged();
        } catch (SVNException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public VCSActivity getSwitchActivity(ISarosSession sarosSession,
        IResource resource) {
        VCSResourceInfo info = getResourceInfo(resource);
        String url = info.getURL();
        String revision = getCurrentRevisionString(resource);
        return VCSActivity.switch_(sarosSession,
            ResourceAdapterFactory.create(resource), url, revision);
    }

    @Override
    public VCSActivity getUpdateActivity(ISarosSession sarosSession,
        IResource resource) {
        String revision = getCurrentRevisionString(resource);
        return VCSActivity.update(sarosSession,
            ResourceAdapterFactory.create(resource), revision);
    }

    public String getCurrentRevisionString(IResource resource) {
        if (!isManaged(resource))
            return null;
        if (!resource.exists())
            return null;
        try {
            ISVNLocalResource svnResourceFor = SVNWorkspaceRoot
                .getSVNResourceFor(resource);
            final SVNRevision revision = svnResourceFor.getRevision();
            if (revision != null)
                return revision.toString();
        } catch (SVNException e) {
            log.error("Error retrieving revision for " + resource, e);
        }
        return null;
    }

    @Override
    public String getRepositoryString(IResource resource) {
        ISVNLocalResource svnResource = SVNWorkspaceRoot
            .getSVNResourceFor(resource);
        try {
            ISVNRemoteResource baseResource = svnResource.getBaseResource();
            if (baseResource == null)
                return null;
            ISVNRepositoryLocation repository = baseResource.getRepository();
            if (repository == null)
                return null;
            String result = repository.getRepositoryRoot().toString();
            return result;
        } catch (SVNException e) {
            log.debug("Undocumented exception", e);
        }

        return null;
    }

    @Override
    public IProject checkoutProject(String newProjectName, FileList fileList,
        IProgressMonitor monitor) {
        ISVNRepositoryLocation loc;
        IProject result = null;

        try {
            VCSResourceInfo info = fileList.getProjectInfo();
            loc = SVNRepositoryLocation
                .fromString(fileList.getRepositoryRoot());
            SVNUrl url = new SVNUrl(info.getURL());

            ISVNRemoteFolder remote[] = { new RemoteFolder(loc, url,
                SVNRevision.HEAD) };
            IProject[] local = { SVNWorkspaceRoot.getProject(newProjectName) };
            // FIXME ndh: Use a CheckoutCommand instead. We should not depend on
            // org.tigris.subversion.subclipse.ui.
            final CheckoutAsProjectOperation checkoutAsProjectOperation = new CheckoutAsProjectOperation(
                null, remote, local);

            SVNRevision revision = getRevision(info.getRevision());
            if (revision != null)
                checkoutAsProjectOperation.setSvnRevision(revision);

            checkoutAsProjectOperation.run(monitor);
            result = local[0];
        } catch (SVNException e1) {
            log.debug("Undocumented exception", e1);
        } catch (MalformedURLException e1) {
            log.debug("Could not parse SVN URL", e1);
        } catch (InvocationTargetException e) {
            undocumentedException(e);
        } catch (InterruptedException e1) {
            log.debug("CheckoutAsProjectOperation interrupted", e1);
        }

        // TODO Should we really be the ones doing this? Here?
        try {
            if (result != null)
                result.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        } catch (CoreException e) {
            log.error("Refresh failed", e);
        } catch (OperationCanceledException e) {
            log.debug("Operation has been canceled during SVN checkout");
        }

        return result;
    }

    @Override
    public String getUrl(IResource resource) {
        ISVNLocalResource svnResource = SVNWorkspaceRoot
            .getSVNResourceFor(resource);

        try {
            ISVNRemoteResource baseResource = svnResource.getBaseResource();
            if (baseResource == null)
                return null;
            String result = baseResource.getUrl().toString();
            return result;
        } catch (SVNException e) {
            undocumentedException(e);
        }
        return null;
    }

    @Override
    public void update(IResource resource, String revisionString,
        IProgressMonitor monitor) {
        if (isAddedToVersionControl(revisionString)) {
            addToVersionControl(resource);
            return;
        }

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        String taskName = "Updating " + resource.getName() + " to revision "
            + revisionString;
        // TODO Why doesn't this work? The caption of the dialog still reads
        // "Operation in progress".
        monitor.beginTask(taskName, 1);

        SVNRevision revision = getRevision(revisionString);
        if (revision == null)
            return;
        SVNWorkspaceRoot root;
        try {
            SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
                .getProvider(resource.getProject());
            root = provider.getSVNWorkspaceRoot();
        } catch (Exception e) {
            // class cast, null pointer
            e.printStackTrace();
            return;
        }
        IResource resources[] = { resource };
        UpdateResourcesCommand cmd = new UpdateResourcesCommand(root,
            resources, revision);
        try {
            cmd.run(monitor);
            monitor.worked(1);
        } catch (SVNException e) {
            log.error("SVN Update failed.", e);
        } finally {
            monitor.done();
        }
        try {
            resource.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            log.error("Refresh failed", e);
        } catch (OperationCanceledException e) {
            log.debug("Operation has been canceled during SVN checkout");
        }
    }

    @Override
    public void revert(IResource resource) {
        if (!isManaged(resource))
            return;
        ISVNLocalResource svnResource = SVNWorkspaceRoot
            .getSVNResourceFor(resource);
        try {
            svnResource.revert();
        } catch (SVNException e) {
            log.error("Couldn't revert " + resource.toString(), e);
        }
        try {
            resource.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            log.error("Refresh failed", e);
        }
    }

    private void addToVersionControl(IResource resource) {
        // TODO
    }

    /**
     * When adding a resource, SVN seems to (sometimes?) assign it the revision
     * "0". This method is used to prevent an update or switch of a resource
     * when it was merely added to version control.
     */
    private boolean isAddedToVersionControl(String revisionString) {
        return revisionString == null || revisionString.equals("0");
    }

    @Override
    public VCSResourceInfo getResourceInfo(IResource resource) {
        return new VCSResourceInfo(getUrl(resource),
            getRevisionString(resource));
    }

    @Override
    public VCSResourceInfo getCurrentResourceInfo(IResource resource) {
        return new VCSResourceInfo(getUrl(resource),
            getCurrentRevisionString(resource));
    }

    @Override
    public void switch_(IResource resource, String url, String revisionString,
        IProgressMonitor monitor) {
        if (resource == null)
            return;
        if (isAddedToVersionControl(revisionString)) {
            addToVersionControl(resource);
            return;
        }
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        String taskName = "Switching " + resource.getName();
        monitor.beginTask(taskName, 1);
        SVNRevision revision = getRevision(revisionString);
        if (revision == null)
            return;
        SVNWorkspaceRoot root;
        SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
            .getProvider(resource.getProject());
        root = provider.getSVNWorkspaceRoot();
        SVNUrl svnURL;
        try {
            svnURL = new SVNUrl(url);
        } catch (MalformedURLException e) {
            log.debug("URL malformed", e);
            return;
        }
        SwitchToUrlCommand cmd = new SwitchToUrlCommand(root, resource, svnURL,
            revision);
        try {
            cmd.run(monitor);
            monitor.worked(1);
        } catch (SVNException e) {
            log.error("SVN Switch failed.", e);
        } finally {
            monitor.done();
        }
    }

    protected SVNRevision getRevision(String revisionString) {
        try {
            return SVNRevision.getRevision(revisionString);
        } catch (ParseException e) {
            log.debug("Could not parse SVN revision", e);
            return null;
        }
    }

    @Override
    public void connect(IProject project, String repositoryRoot,
        String directory, IProgressMonitor monitor) {
        // cf
        // org.tigris.subversion.subclipse.ui.wizards.sharing.SharingWizard#performFinish()
        if (hasLocalCache(project)) {
            /*
             * FIXME ndh We need to check first if the remote folder is the
             * right one. Even if the project was connected to a repo before, it
             * might not be the one we want now. If it isn't, purge the local
             * folder first, then share the project.
             */
            try {
                SVNWorkspaceRoot.setSharing(project, monitor);
            } catch (TeamException e) {
                // We should never get here since we check for a remote
                // counter-part in hasLocalCache().
            }
        } else {
            monitor.beginTask("Connecting  " + project.getName(), 1);
            try {
                ISVNRepositoryLocation location = SVNRepositoryLocation
                    .fromString(repositoryRoot);
                SVNWorkspaceRoot.shareProject(location, project, directory,
                    "not used", false, SubMonitor.convert(monitor));
                project.refreshLocal(IResource.DEPTH_INFINITE, null);
                monitor.worked(1);
            } catch (SVNException e) {
                log.debug("", e);
                throw new NotImplementedException("Repository not known");
            } catch (TeamException e) {
                // We can't get here, TeamExceptions are wrapped in
                // SVNExceptions.
                assert false;
            } catch (CoreException e) {
                log.debug("Error refreshing project", e);
            } finally {
                monitor.done();
            }
        }
    }

    @Override
    public boolean hasLocalCache(IProject project) {
        // cf org.tigris.subversion.subclipse.ui.wizards.sharing.SharingWizard
        boolean isSVNFolder = false;
        try {
            LocalResourceStatus projectStatus = SVNWorkspaceRoot
                .peekResourceStatusFor(project);
            isSVNFolder = (projectStatus != null) && projectStatus.hasRemote();
        } catch (final SVNException e) {
            undocumentedException(e);
        }
        return isSVNFolder;
    }

    @Override
    public void disconnect(IProject project, boolean deleteContent,
        IProgressMonitor monitor) {
        // cf org.tigris.subversion.subclipse.ui.actions.UnmanageAction
        try {
            ISVNLocalFolder folder = SVNWorkspaceRoot.getSVNFolderFor(project);
            try {
                monitor = monitor == null ? new NullProgressMonitor() : monitor;
                monitor.beginTask("Disconnecting " + project.getName()
                    + " from SVN", 1);
                if (deleteContent) {
                    folder.unmanage(monitor);
                    project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                }
                monitor.worked(1);
            } catch (SVNException e) {
                undocumentedException(e);
            } catch (CoreException e) {
                log.debug("Error refreshing project", e);
            } finally {
                // We want to remove the nature even if the unmanage operation
                // fails
                RepositoryProvider.unmap(project);
                monitor.done();
            }
        } catch (TeamException e) {
            log.error("The project " + project.getName() + " wasn't managed.",
                e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public ProjectDeltaVisitor getProjectDeltaVisitor(
        EditorManager editorManager, ISarosSession sarosSession,
        SharedProject sharedProject) {
        return new SubclipseProjectDeltaVisitor(editorManager, sarosSession,
            sharedProject);
    }

    /*
     * --------------------------------------------------------------------------
     * VCSProvider intf. impl.
     */

    @Override
    public String getID() {
        return identifier;
    }
}
