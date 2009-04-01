package de.fu_berlin.inf.dpp.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.util.Util;

public class JoinSessionWizardUtils {

    private static final Logger log = Logger
        .getLogger(JoinSessionWizardUtils.class.getName());

    public static class ScanRunner implements Runnable {

        public ScanRunner(IIncomingInvitationProcess invitationProcess) {
            this.invitationProcess = invitationProcess;
        }

        IIncomingInvitationProcess invitationProcess;

        IProject project;

        public void run() {

            /*
             * FIXME InvocationTargetException and Interrupted Exceptions are
             * incorrectly handled
             */
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(EditorAPI
                .getShell());
            try {
                dialog.run(true, false, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) {

                        monitor.beginTask("Scanning workspace projects ... ",
                            IProgressMonitor.UNKNOWN);
                        ScanRunner.this.project = JoinSessionWizardUtils
                            .getLocalProject(ScanRunner.this.invitationProcess
                                .getRemoteFileList(), monitor);
                        monitor.done();
                    }

                });
            } catch (InvocationTargetException e) {
                log.error("Internal Error:"
                    + " InvocationTargetException not properly handeled!", e);
            } catch (InterruptedException e) {
                log.error("Internal Error:"
                    + " InterruptedException not properly handeled!", e);
            }
        }
    }

    /**
     * Run the scan for the best matching project as a blocking operation.
     */
    public static IProject getBestScanMatch(
        IIncomingInvitationProcess invitationProcess) {

        ScanRunner runner = new ScanRunner(invitationProcess);

        Util.runSafeSWTSync(log, runner);

        return runner.project;
    }

    public static int getMatch(FileList remoteFileList, IProject project) {
        try {
            return remoteFileList.match(new FileList(project));
        } catch (CoreException e) {
            log.debug("Couldn't calculate match for project " + project, e);

            return -1;
        }
    }

    /**
     * Return the best match among all project from workspace with the given
     * remote file list or null if no best match could be found.
     * 
     * To be considered a match, projects have to match at least 80%.
     */
    public static IProject getLocalProject(FileList remoteFileList,
        IProgressMonitor monitor) {

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        IProject bestMatch = null;

        // A match needs to be at least 80% for us to consider.
        int bestMatchScore = 80;

        for (int i = 0; i < projects.length; i++) {
            monitor.worked(1);
            if (!projects[i].isOpen()) {
                continue;
            }

            int matchScore = JoinSessionWizardUtils.getMatch(remoteFileList,
                projects[i]);

            if (matchScore > bestMatchScore) {
                bestMatchScore = matchScore;
                bestMatch = projects[i];
            }
        }

        return bestMatch;
    }

    public static boolean projectIsUnique(String name) {

        // Then check with all the projects
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        return JoinSessionWizardUtils.projectIsUnique(name, projects);
    }

    public static IProject getProjectForName(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    public static boolean projectIsUnique(String name, IProject[] projects) {

        // Use File to compare so the comparison is case-sensitive depending on
        // the underlying platform
        File newProjectName = new File(name);

        for (IProject project : projects) {
            if (new File(project.getName()).equals(newProjectName)) {
                return false;
            }
        }
        return true;
    }

    public static String findProjectNameProposal(String projectName) {

        // Start with the projects name
        String projectProposal = projectName;

        // Then check with all the projects
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        if (JoinSessionWizardUtils.projectIsUnique(projectProposal, projects)) {
            return projectProposal;

        } else {
            // Name is already in use!
            Pattern p = Pattern.compile("^(.*)(\\d+)$");
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
            while (!JoinSessionWizardUtils.projectIsUnique(projectProposal
                + " " + i, projects)) {
                i++;
            }

            return projectProposal + " " + i;
        }
    }

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
