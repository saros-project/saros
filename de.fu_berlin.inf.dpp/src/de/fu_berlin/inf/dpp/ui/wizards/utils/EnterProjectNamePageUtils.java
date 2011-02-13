package de.fu_berlin.inf.dpp.ui.wizards.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import de.fu_berlin.inf.dpp.util.Utils;

public class EnterProjectNamePageUtils {

    private static final Logger log = Logger
        .getLogger(EnterProjectNamePageUtils.class);

    public static PreferenceUtils preferenceUtils;

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

                        monitor.beginTask("Scanning workspace projects ... ",
                            IProgressMonitor.UNKNOWN);
                        IProject project = EnterProjectNamePageUtils
                            .getLocalProject(ScanRunner.this.remoteFileList,
                                monitor);
                        monitor.done();

                        ScanRunner.this.project = project;
                    }

                });
            } catch (InvocationTargetException e) {
                log.error("An error occurred while scanning "
                    + "for best matching project: ", e);
                MessageDialog.openError(EditorAPI.getShell(),
                    "An Error occurred in Saros",
                    "An error occurred while scanning "
                        + "for best matching project: " + e.getMessage());
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
     * @param name
     * @return
     */
    public static boolean projectIsUnique(String name) {

        // Then check with all the projects
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        return EnterProjectNamePageUtils.projectIsUnique(name, projects);
    }

    /**
     * 
     * @param name
     * @return
     */
    public static IProject getProjectForName(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    /**
     * 
     * @param name
     * @param projects
     * @return
     */
    public static boolean projectIsUnique(String name, IProject... projects) {

        if (name == null)
            throw new IllegalArgumentException("Illegal project name given");

        if (new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()
            .toFile(), name).exists()) {
            if (!getProjectForName(name).exists()) {
                log.warn("Eclipse does not think there is a project "
                    + "already for the given name " + name
                    + " but on the file system there is");
            }
            return false;
        }

        // Use File to compare so the comparison is case-sensitive depending on
        // the underlying platform
        File newProjectName = new File(name);
        //
        // if (ResourcesPlugin.getWorkspace().getRoot().getFolder(new
        // Path(name))
        // .exists())
        // return false;

        for (IProject project : projects) {
            if (new File(project.getName()).equals(newProjectName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param projectName
     * @return
     */
    public static String findProjectNameProposal(String projectName) {

        // Start with the projects name
        String projectProposal = projectName;

        // Then check with all the projects
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        if (EnterProjectNamePageUtils
            .projectIsUnique(projectProposal, projects)) {
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
            while (!EnterProjectNamePageUtils.projectIsUnique(projectProposal
                + " " + i, projects)) {
                i++;
            }

            return projectProposal + " " + i;
        }
    }

    /**
     * This method decides whether the given Project should be automatically
     * updated
     * 
     * @param projectName
     * @return <code>true</code> if automatically reuse of existing project is
     *         selected in preferences and the a project with the given name
     *         exists
     */
    public static boolean autoUpdateProject(String projectName) {
        if (preferenceUtils == null) {
            log.warn("preferenceUtils is null");
            return false;
        }
        if (preferenceUtils.isAutoReuseExisting()
            && JoinSessionWizardUtils.existsProjects(projectName)) {
            return true;
        } else {
            return false;
        }
    }
}
