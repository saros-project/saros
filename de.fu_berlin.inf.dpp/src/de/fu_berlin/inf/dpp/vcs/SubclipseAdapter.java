package de.fu_berlin.inf.dpp.vcs;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNCoreConstants;
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

import de.fu_berlin.inf.dpp.FileList;

/**
 * Adapter for Subclipse 1.6.
 * 
 * @author ahaferburg
 */
class SubclipseAdapter implements VCSAdapter {
    private static final Logger log = Logger.getLogger(SubclipseAdapter.class);

    // TODO Is it safe to assume that this won't change in the future?
    static final String identifier = "org.tigris.subversion.subclipse.core.svnnature";
    RepositoryProvider provider;

    public boolean isInManagedProject(IResource resource) {
        boolean underVCS;
        IProject project = resource.getProject();
        underVCS = RepositoryProvider.isShared(project);
        if (!underVCS)
            return false;

        return identifier.equals(getProviderID(resource));
    }

    public String getProviderID(IResource resource) {
        IProject project = resource.getProject();
        RepositoryProvider provider = RepositoryProvider.getProvider(project);
        String vcsIdentifier = provider.getID();

        return vcsIdentifier;
    }

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

    public String getRevisionString(IResource resource) {
        ISVNLocalResource svnResource = SVNWorkspaceRoot
            .getSVNResourceFor(resource);
        SVNRevision revision;
        try {
            if (!svnResource.isManaged())
                return null;
            revision = svnResource.getRevision();
        } catch (SVNException e) {
            e.printStackTrace();
            return null;
        }
        if (revision == null)
            return null;
        return revision.toString();
    }

    // public String getIdentifierString() {
    // return "svn";
    // }

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
            String result = repository.toString();
            return result;
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public IProject checkoutProject(String newProjectName, FileList fileList,
        SubMonitor monitor) {
        ISVNRepositoryLocation loc;
        IProject result = null;
        try {
            VCSResourceInformation info = fileList.getProjectInformation();
            loc = SVNRepositoryLocation.fromString(info.repositoryRoot);
            // TODO Is there an API to construct the URL? (cleaner way than
            // String concatenation)
            SVNUrl url = new SVNUrl(info.repositoryRoot + info.path);

            ISVNRemoteFolder remote[] = { new RemoteFolder(loc, url,
                SVNRevision.HEAD) };
            IProject[] local = { SVNWorkspaceRoot.getProject(newProjectName) };
            // FIXME ndh: Use a CheckoutCommand instead. We should not depend on
            // org.tigris.subversion.subclipse.ui.
            final CheckoutAsProjectOperation checkoutAsProjectOperation = new CheckoutAsProjectOperation(
                null, remote, local);
            SVNRevision rev = SVNRevision.getRevision(info.revision);
            checkoutAsProjectOperation.setSvnRevision(rev);
            checkoutAsProjectOperation.run(monitor);
            result = local[0];
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Should we really be the ones doing this?
        try {
            if (result != null)
                result.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public String getProjectPath(IResource resource) {
        ISVNLocalResource svnResource = SVNWorkspaceRoot
            .getSVNResourceFor(resource);

        try {
            ISVNRemoteResource baseResource = svnResource.getBaseResource();
            if (baseResource == null)
                return null;
            String result = baseResource.getRepositoryRelativePath();
            return result;
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void update(IResource resource, String targetRevision,
        IProgressMonitor monitor) {
        SVNRevision revision;
        try {
            revision = SVNRevision.getRevision(targetRevision);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
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
        cmd.setDepth(ISVNCoreConstants.DEPTH_INFINITY);
        try {
            cmd.run(monitor);
        } catch (SVNException e) {
            e.printStackTrace();
        }
        try {
            resource.refreshLocal(IResource.DEPTH_ZERO, null);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public VCSResourceInformation getResourceInformation(IResource resource) {
        VCSResourceInformation info = new VCSResourceInformation();
        info.repositoryRoot = getRepositoryString(resource);
        info.path = getProjectPath(resource);
        info.revision = getRevisionString(resource);
        return info;
    }

    public void switch_(IResource resource, String url, String revisionString,
        IProgressMonitor monitor) {
        if (resource == null)
            return;
        SVNRevision revision;
        try {
            revision = SVNRevision.getRevision(revisionString);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
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
        SVNUrl svnURL;
        try {
            svnURL = new SVNUrl(url);
        } catch (MalformedURLException e1) {
            log.debug("", e1);
            return;
        }
        SwitchToUrlCommand cmd = new SwitchToUrlCommand(root, resource, svnURL,
            revision);
        try {
            cmd.run(monitor);
        } catch (SVNException e) {
            e.printStackTrace();
        }
    }

    public void connect(IProject project, String url) {
        // cf
        // org.tigris.subversion.subclipse.ui.wizards.sharing.SharingWizard#performFinish()
        if (hasLocalCache(project)) {
            // FIXME ndh We need to check first if the remote folder is the
            // right one. Even if we were connected to a repo before, it might
            // not be the one in the URL. If it isn't, purge the local folder
            // first, then share the project.
            try {
                SVNWorkspaceRoot.setSharing(project, null);
            } catch (TeamException e) {
                // We should never get here since we check for a remote
                // counter-part in hasSVNFolder().
            }
        } else {
            try {
                ISVNRepositoryLocation location = SVNRepositoryLocation
                    .fromString(url);
                SVNWorkspaceRoot.shareProject(location, project, ".",
                    "not used", false, null);
                project.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (SVNException e) {
                log.debug("", e);
                throw new NotImplementedException("Repository not known");
            } catch (TeamException e) {
                // We can't get here, all TeamExceptions are wrapped in
                // SVNExceptions.
            } catch (CoreException e) {
                log.debug("Error refreshing project", e);
            }
        }
    }

    public boolean hasLocalCache(IProject project) {
        // cf org.tigris.subversion.subclipse.ui.wizards.sharing.SharingWizard
        boolean isSVNFolder = false;
        try {
            LocalResourceStatus projectStatus = SVNWorkspaceRoot
                .peekResourceStatusFor(project);
            isSVNFolder = (projectStatus != null) && projectStatus.hasRemote();
        } catch (final SVNException e) {
            // TODO When would this happen? Subclipse wraps any
            // SVNClientException with SVNException.
            log.error("Something went wrong, but it's undocumented", e);
        }
        return isSVNFolder;
    }

    public void disconnect(IProject project, boolean deleteContent) {
        // cf org.tigris.subversion.subclipse.ui.actions.UnmanageAction
        try {
            ISVNLocalFolder folder = SVNWorkspaceRoot.getSVNFolderFor(project);
            try {
                if (deleteContent) {
                    NullProgressMonitor monitor = new NullProgressMonitor();
                    folder.unmanage(monitor);
                    project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                }
            } catch (SVNException e) {
                // TODO When would this happen? Subclipse wraps any
                // CoreException with SVNException.
                log.error("Something went wrong, but it's undocumented", e);
            } catch (CoreException e) {
                log.debug("Error refreshing project", e);
            } finally {
                // We want to remove the nature even if the unmanage operation
                // fails
                RepositoryProvider.unmap(project);
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
}
