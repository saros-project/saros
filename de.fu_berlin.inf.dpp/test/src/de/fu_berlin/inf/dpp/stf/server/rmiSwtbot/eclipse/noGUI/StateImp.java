package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.util.FileUtil;

public class StateImp extends EclipseComponent implements State {

    public void waitUntilFileContentSame(String otherClassContent,
        String... fileNodes) throws RemoteException {
        waitUntil(SarosConditions.isFileContentsSame(this, otherClassContent,
            fileNodes));
    }

    public void waitUntilClassContentsSame(final String projectName,
        final String pkg, final String className, final String otherClassContent)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getClassContent(projectName, pkg, className).equals(
                    otherClassContent);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });
    }

    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return helperPart.ConvertStreamToString(file.getContents());
    }

    public String getFileContent(String... fileNodes) throws RemoteException,
        IOException, CoreException {
        IPath path = new Path(getPath(fileNodes));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return helperPart.ConvertStreamToString(file.getContents());
    }

    public void deleteAllProjects() throws RemoteException {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++) {
            try {
                FileUtil.delete(projects[i]);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete files ", e);
            }
        }
    }
}
