package de.fu_berlin.inf.dpp.vcs;

import java.text.ParseException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.commands.UpdateResourcesCommand;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
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
            String result = baseResource.getRepository().toString();
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
            VCSProjectInformation info = fileList.getProjectInformation();
            loc = SVNRepositoryLocation.fromString(info.repositoryURL);
            // TODO Is there an API to construct the URL? (cleaner way than
            // String concatenation)
            SVNUrl url = new SVNUrl(info.repositoryURL + info.projectPath);

            ISVNRemoteFolder remote[] = { new RemoteFolder(loc, url,
                SVNRevision.HEAD) };
            IProject[] local = { SVNWorkspaceRoot.getProject(newProjectName) };
            // FIXME ndh: Use a CheckoutCommand instead. We should not depend on
            // org.tigris.subversion.subclipse.ui.
            final CheckoutAsProjectOperation checkoutAsProjectOperation = new CheckoutAsProjectOperation(
                null, remote, local);
            SVNRevision rev = SVNRevision.getRevision(info.baseRevision);
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
            String result = baseResource.getRepositoryRelativePath();
            return result;
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void update(IFile file, String targetRevision,
        IProgressMonitor monitor) {
        SVNRevision revision;
        try {
            revision = SVNRevision.getRevision(targetRevision);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        IResource resource[] = { file };
        SVNWorkspaceRoot root;
        try {
            SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
                .getProvider(file.getProject());
            root = provider.getSVNWorkspaceRoot();
        } catch (Exception e) {
            // class cast, null pointer
            e.printStackTrace();
            return;
        }
        UpdateResourcesCommand cmd = new UpdateResourcesCommand(root, resource,
            revision);
        try {
            cmd.run(monitor);
        } catch (SVNException e) {
            e.printStackTrace();
        }
        try {
            file.refreshLocal(IResource.DEPTH_ZERO, null);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public VCSProjectInformation getProjectInformation(IProject project) {
        VCSProjectInformation info = new VCSProjectInformation();
        info.repositoryURL = getRepositoryString(project);
        info.projectPath = getProjectPath(project);
        info.baseRevision = getRevisionString(project);
        return info;
    }
}
