package de.fu_berlin.inf.dpp.vcs;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * @author ahaferburg
 */
public class SubversionAdapter {
    RepositoryProvider provider;

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
}
