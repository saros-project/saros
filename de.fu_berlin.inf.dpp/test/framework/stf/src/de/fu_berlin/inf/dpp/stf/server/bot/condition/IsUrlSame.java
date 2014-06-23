package de.fu_berlin.inf.dpp.stf.server.bot.condition;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class IsUrlSame extends DefaultCondition {

    private String fullPath;
    private String url;

    IsUrlSame(String fullPath, String url) {
        this.fullPath = fullPath;
        this.url = url;
    }

    @Override
    public String getFailureMessage() {
        String message = "Expected URL of \"" + fullPath + "\" to become \""
            + url + "\", but ";
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
        VCSResourceInfo resourceInfo = vcs.getResourceInfo(resource);
        if (resourceInfo == null)
            return message + "the resourceInfo is null.";
        else
            return message + "the URL is \"" + resourceInfo.getURL() + "\".";
    }

    @Override
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
        VCSResourceInfo resourceInfo = vcs.getResourceInfo(resource);
        boolean result = resourceInfo != null && resourceInfo.getURL() != null
            && resourceInfo.getURL().equals(url);
        return result;
    }
}
