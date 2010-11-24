package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;

public class IsReversionSame extends DefaultCondition {

    private String fullPath;
    private String reversionID;

    IsReversionSame(String fullPath, String reversionID) {
        this.fullPath = fullPath;
        this.reversionID = reversionID;
    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs != null)
            return vcs.getRevisionString(resource).equals(reversionID);
        return false;
    }
}
