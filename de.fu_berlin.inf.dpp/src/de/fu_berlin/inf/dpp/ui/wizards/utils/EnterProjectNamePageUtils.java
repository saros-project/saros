package de.fu_berlin.inf.dpp.ui.wizards.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class EnterProjectNamePageUtils {

    /**
     * Stores the unique project ID if a project is once shared in session. This
     * is done to propose the same project directly for partial sharing.
     */

    // FIXME the list should be cleared when the session is stopped
    static List<String> alreadySharedProjectIDs = new ArrayList<String>();

    /**
     * 
     * @param projectName
     * @return
     */
    public static IProject getProjectForName(String projectName) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

    /**
     * Tests if the given project name does not already exist in the current
     * workspace.
     * 
     * @param projectName
     *            the name of the project
     * @param reservedProjectNames
     *            further project names that are already reserved even if the
     *            projects do not exist
     * @return <code>true</code> if the project name does not exist in the
     *         current workspace and does not exist in the reserved project
     *         names, <code>false</code> if the project name is an empty string
     *         or the project already exists in the current workspace
     */
    public static boolean projectNameIsUnique(String projectName,
        String... reservedProjectNames) {

        if (projectName == null)
            throw new NullPointerException("projectName is null");

        if (projectName.length() == 0)
            return false;

        Set<IProject> projects = new HashSet<IProject>(
            Arrays.asList(ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()));

        for (String reservedProjectName : reservedProjectNames) {
            projects.add(ResourcesPlugin.getWorkspace().getRoot()
                .getProject(reservedProjectName));
        }

        return !projects.contains(ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName));
    }

    /**
     * Proposes a project name based on the existing project names in the
     * current workspace. The proposed project name is unique.
     * 
     * @see EnterProjectNamePageUtils#projectNameIsUnique
     * 
     * @param projectName
     *            project name which shall be checked
     * @param reservedProjectNames
     *            additional project names that should not be returned when
     *            finding a name proposal for a project
     * @return a unique project name based on the original project name
     */
    public static String findProjectNameProposal(String projectName,
        String... reservedProjectNames) {

        int idx;

        for (idx = projectName.length() - 1; idx >= 0
            && Character.isDigit(projectName.charAt(idx)); idx--) {
            // NOP
        }

        String newProjectName;

        if (idx < 0)
            newProjectName = "";
        else
            newProjectName = projectName.substring(0, idx + 1);

        if (idx == projectName.length() - 1)
            idx = 2;
        else {
            try {
                idx = Integer.valueOf(projectName.substring(idx + 1));
            } catch (NumberFormatException e) {
                idx = 2;
            }
        }

        projectName = newProjectName;

        while (!projectNameIsUnique(projectName, reservedProjectNames)) {
            projectName = newProjectName + idx;
            idx++;
        }

        return projectName;

    }

    /**
     * This method decides whether the given project should be automatically
     * updated.
     * 
     * @param projectName
     * 
     * @return <code>true</code> if the project is already partial shared,
     *         <code>false</code> otherwise
     */
    public static boolean autoUpdateProject(String projectID, String projectName) {

        if (alreadySharedProjectIDs.contains(projectID)) {
            return true;
        } else {
            alreadySharedProjectIDs.add(projectID);
            return false;
        }
    }
}
