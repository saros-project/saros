package de.fu_berlin.inf.dpp.ui.wizards.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.util.Utils;

public class EnterProjectNamePageUtils {

    private static final Logger log = Logger
        .getLogger(EnterProjectNamePageUtils.class);

    public static PreferenceUtils preferenceUtils;

    /**
     * Stores the unique project ID if a project is once shared in session. This
     * is done to propose the same project directly for partial sharing.
     */
    static List<String> alreadySharedProjectsIDs = new ArrayList<String>();

    public static class ScanRunner implements Runnable {

        public ScanRunner(FileList remoteFileList) {
            this.remoteFileList = remoteFileList;
        }

        FileList remoteFileList;

        IProject project = null;

        public void run() {

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                EditorAPI.getShell());
            try {
                dialog.run(true, true, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                        throws InterruptedException {

                        monitor
                            .beginTask(
                                Messages.EnterProjectNamePageUtils_monitar_scanning,
                                IProgressMonitor.UNKNOWN);
                        IProject project = EnterProjectNamePageUtils
                            .getLocalProject(ScanRunner.this.remoteFileList,
                                monitor);
                        monitor.done();

                        ScanRunner.this.project = project;
                    }

                });
            } catch (InvocationTargetException e) {
                log.error("An error occurred while scanning " //$NON-NLS-1$
                    + "for best matching project: ", e); //$NON-NLS-1$
                MessageDialog.openError(EditorAPI.getShell(),
                    Messages.EnterProjectNamePageUtils_scan_error,
                    MessageFormat.format(
                        Messages.EnterProjectNamePageUtils_scan_error_text,
                        e.getMessage()));
            } catch (InterruptedException e) {
                this.project = null;
            }
        }
    }

    /**
     * Run the scan for the best matching project as a blocking operation.
     * 
     */
    public static IProject getBestScanMatch(FileList remoteFileList) {

        ScanRunner runner = new ScanRunner(remoteFileList);

        Utils.runSafeSWTSync(log, runner);

        return runner.project;
    }

    /**
     * Return the best match among all project from workspace with the given
     * remote file list or null if no best match could be found or if the
     * operation was canceled by the user.
     * 
     * To be considered a match, projects have to match at least 80%.
     * 
     */
    public static IProject getLocalProject(FileList hostFileList,
        IProgressMonitor monitor) throws InterruptedException {

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        IProject bestMatch = null;

        // A match needs to be at least 80% for us to consider.
        int bestMatchScore = 80;

        for (int i = 0; i < projects.length; i++) {
            monitor.worked(1);

            if (monitor.isCanceled()) {
                if (bestMatch == null)
                    throw new InterruptedException();
                else
                    return bestMatch;
            }

            if (!projects[i].isOpen()) {
                continue;
            }

            int matchScore = hostFileList.computeMatch(projects[i]);

            if (matchScore > bestMatchScore) {
                bestMatchScore = matchScore;
                bestMatch = projects[i];
                if (matchScore == 100)
                    return bestMatch;
            }
        }

        return bestMatch;
    }

    /**
     * 
     * @param projectName
     * @return
     */
    public static IProject getProjectForName(String projectName) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

     /**
     * Tests, if the given projectname does not already exist in the current workspace.
     * In Addition the method also accepts an array of further reserved names.
     * 
     * @param projectName to test.
     * @param reservedNames Array of reserved project names. May be empty but not null.
     * @return true, if projectName does not exist in the current workspace and
     *               does not exist in the reservedNames
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
     * workspace and based on the array reservedNames.
     * The proposed projectname is unique. 
     * @see EnterProjectNamePageUtils#projectNameIsUnique
     * 
     * @param projectName Projectname which shall be checked.
     * @return a unique projectname based on "projectName". If "projectName" is
     * already unique, it will be returned without changes.
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
            && JoinSessionWizardUtils.existsProjects(remoteProjectNames)) {
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
}
