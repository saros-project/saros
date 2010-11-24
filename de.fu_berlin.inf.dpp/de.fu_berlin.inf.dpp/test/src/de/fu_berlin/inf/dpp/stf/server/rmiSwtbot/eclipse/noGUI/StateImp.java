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

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.util.FileUtil;

public class StateImp extends EclipseComponent implements State {

    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException {
        waitUntil(SarosConditions.isClassContentsSame(this, projectName, pkg,
            className, otherClassContent));
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

    /**
     * get the content of the class file, which is saved.
     */
    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return helperPart.ConvertStreamToString(file.getContents());
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
