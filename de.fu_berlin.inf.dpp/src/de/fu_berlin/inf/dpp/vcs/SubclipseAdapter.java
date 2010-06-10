package de.fu_berlin.inf.dpp.vcs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.operations.CheckoutAsProjectOperation;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import de.fu_berlin.inf.dpp.FileList;

/**
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
            loc = SVNRepositoryLocation.fromString(fileList.vcsRepository);
            // TODO Is there an API to construct the URL? (cleaner way than
            // String concatenation)
            SVNUrl url = new SVNUrl(fileList.vcsRepository
                + fileList.vcsProjectRoot);

            ISVNRemoteFolder remote[] = { new RemoteFolder(loc, url,
                SVNRevision.HEAD) };
            IProject[] local = { SVNWorkspaceRoot.getProject(newProjectName) };
            // FIXME ndh: Use a CheckoutCommand instead. We should not depend on
            // org.tigris.subversion.subclipse.ui.
            final CheckoutAsProjectOperation checkoutAsProjectOperation = new CheckoutAsProjectOperation(
                null, remote, local);
            SVNRevision rev = SVNRevision.getRevision(fileList.vcsBaseRevision);
            checkoutAsProjectOperation.setSvnRevision(rev);
            checkoutAsProjectOperation.run(monitor);
            result = local[0];
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FIXME ndh: Iterate over files, update to different revision.
        // for (IPath path : fileList.getPaths()) {
        // if (fileList.getVCSRevision(path) == null)
        // continue;
        // SVNWorkspaceRoot root = null;
        // UpdateResourcesCommand update = new UpdateResourcesCommand(root,
        // resources, revision);
        // }
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
}
