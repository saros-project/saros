package de.fu_berlin.inf.dpp.ui.wizards.utils;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

public class JoinSessionWizardUtils {

    static final Logger log = Logger.getLogger(JoinSessionWizardUtils.class);

    /**
     * @param projectName
     * @return <code><b>true</b></code> if a project with the given Name exists
     *         in the workspace
     */
    public static boolean existsProjects(String projectName) {
        // Start with the projects name
        File proposedName = new File(projectName);

        // Then check with all the projects
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        for (IProject project : workspace.getRoot().getProjects()) {
            if (new File(project.getName()).equals(proposedName))
                return true;
        }
        return false;
    }
}
