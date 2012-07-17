package de.fu_berlin.inf.dpp.ui.wizards.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;

public class EnterProjectNamePageUtils {

    private static final Logger log = Logger
        .getLogger(EnterProjectNamePageUtils.class);

    public static PreferenceUtils preferenceUtils;

    /**
     * Stores the unique project ID if a project is once shared in session. This
     * is done to propose the same project directly for partial sharing.
     */
    static List<String> alreadySharedProjectsIDs = new ArrayList<String>();

    /**
     * 
     * @param projectName
     * @return
     */
    public static IProject getProjectForName(String projectName) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

    /**
     * Tests, if the given projectname does not already exist in the current
     * workspace. In Addition the method also accepts an array of further
     * reserved names.
     * 
     * @param projectName
     *            to test.
     * @param reservedNames
     *            Array of reserved project names. May be empty but not null.
     * @return true, if projectName does not exist in the current workspace and
     *         does not exist in the reservedNames
     */
    public static boolean projectNameIsUnique(String projectName,
        String... reservedNames) {

        if (projectName == null)
            throw new IllegalArgumentException("Illegal project name given");

        if (new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()
            .toFile(), projectName).exists()) {
            if (!getProjectForName(projectName).exists()) {
                log.warn("Eclipse does not think there is a project "
                    + "already for the given name " + projectName
                    + " but on the file system there is");
            }
            return false;
        }

        // Use File to compare so the comparison is case-sensitive depending on
        // the underlying platform
        File newProjectName = new File(projectName);

        for (String reserved : reservedNames) {
            if (new File(reserved).equals(newProjectName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Proposes a projectname based on the existing projectnames in the current
     * workspace and based on the array reservedNames. The proposed projectname
     * is unique.
     * 
     * @see EnterProjectNamePageUtils#projectNameIsUnique
     * 
     * @param projectName
     *            Projectname which shall be checked.
     * @return a unique projectname based on "projectName". If "projectName" is
     *         already unique, it will be returned without changes.
     */
    public static String findProjectNameProposal(String projectName,
        String... reservedNames) {

        // Start with the projects name
        String projectProposal = projectName;

        // Then check with all the projects
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        // Make String-Array from project names
        String[] projectNames = new String[projects.length
            + reservedNames.length];
        for (int i = 0; i < projects.length; i++) {
            projectNames[i] = projects[i].getName();
        }
        for (int i = projects.length; i < projects.length
            + reservedNames.length; i++) {
            projectNames[i] = reservedNames[i - projects.length];
        }

        if (EnterProjectNamePageUtils.projectNameIsUnique(projectProposal,
            projectNames)) {
            return projectProposal;

        } else {
            // Name is already in use!
            Pattern p = Pattern.compile("^(.+?)(\\d+)$");
            Matcher m = p.matcher(projectProposal);

            int i;
            // Check whether the name ends in a number or not
            if (m.find()) {
                projectProposal = m.group(1).trim();
                i = Integer.parseInt(m.group(2));
            } else {
                i = 2;
            }

            // Then find the next available number
            while (!EnterProjectNamePageUtils.projectNameIsUnique(
                projectProposal + " " + i, projectNames)) {
                i++;
            }

            return projectProposal + " " + i;
        }
    }

    /**
     * This method decides whether the given Project should be automatically
     * updated
     * 
     * @param remoteProjectNames
     * 
     * @return <code>true</code> if automatically reuse of existing project is
     *         selected in preferences and the a project with the given name
     *         exists
     */
    public static boolean autoUpdateProject(String id, String remoteProjectNames) {
        if (preferenceUtils == null) {
            log.warn("preferenceUtils is null"); //$NON-NLS-1$
            return false;
        }
        if (preferenceUtils.isAutoReuseExisting()
            && existsProjects(remoteProjectNames)) {
            return true;
        } else if (alreadySharedProjectsIDs.contains(id)) {
            return true;
        } else {
            alreadySharedProjectsIDs.add(id);
            return false;
        }
    }

    public static void setPreferenceUtils(PreferenceUtils preferenceUtils) {
        EnterProjectNamePageUtils.preferenceUtils = preferenceUtils;
    }

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
