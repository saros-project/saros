package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;

public class IsInSVN extends DefaultCondition {

    private String projectName;

    IsInSVN(String projectName) {

        this.projectName = projectName;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }
}
