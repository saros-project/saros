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
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

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
     * @param resourcePath
     *            full path of the resource, e.g.
     *            Foo_Saros/src/my/pkg/myClass.java.
     *            Foo_Saros/myFolder/myFile.xml.
     */
    public boolean isResourceExist(String resourcePath) throws RemoteException {
        IPath path = new Path(resourcePath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
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

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
