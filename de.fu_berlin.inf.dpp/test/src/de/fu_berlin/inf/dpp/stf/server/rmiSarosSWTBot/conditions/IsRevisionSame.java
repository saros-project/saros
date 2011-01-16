package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;

public class IsRevisionSame extends DefaultCondition {

    private String fullPath;
    private String expectedRevision;

    IsRevisionSame(String fullPath, String revisionID) {
        this.fullPath = fullPath;
        this.expectedRevision = revisionID;
    }

    public String getFailureMessage() {
        String message = "Expected revision of \"" + fullPath
            + "\" to become \"" + expectedRevision + "\", but ";
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null) {
            return message + "the resource wasn't found.";
        }
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null) {
            return message + "the resource isn't managed.";
        }
        String revisionString = vcs.getRevisionString(resource);
        return message + "the revision is \"" + revisionString + "\".";
    }

    public boolean test() throws Exception {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null) {
            return false;
        }
        String revisionString = vcs.getRevisionString(resource);
        return revisionString != null && revisionString.equals(expectedRevision);
    }
}
