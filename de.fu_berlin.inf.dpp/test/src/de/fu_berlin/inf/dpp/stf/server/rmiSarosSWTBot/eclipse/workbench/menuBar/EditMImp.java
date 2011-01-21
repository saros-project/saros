package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;
import de.fu_berlin.inf.dpp.util.FileUtil;

public class EditMImp extends EclipsePart implements EditM {

    private static transient EditMImp editImp;

    /**
     * {@link FileMImp} is a singleton, but inheritance is possible.
     */
    public static EditMImp getInstance() {
        if (editImp != null)
            return editImp;
        editImp = new EditMImp();
        return editImp;
    }

    protected final static String VIEWNAME = "Package Explorer";
    private final static String SHELL_DELETE_RESOURCE = "Delete Resources";
    private final static String DELETE = "Delete";

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Delete"
     * 
     **********************************************/

    public void deleteProject(String projectName) throws RemoteException {
        IPath path = new Path(projectName);
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource == null) {
            log.debug("File " + projectName + " not found for deletion");
            return;
        }
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete file " + projectName, e);
            }
        }
    }

    public void deleteAllProjectsWithGUI() throws RemoteException {
        precondition();
        SWTBotTreeItem[] allTreeItems = treeW.getTreeInView(VIEWNAME)
            .getAllItems();
        if (allTreeItems != null) {
            for (SWTBotTreeItem item : allTreeItems) {
                item.contextMenu(DELETE).click();
                shellC.confirmWindowWithCheckBox(SHELL_DELETE_RESOURCE, OK,
                    true);
                shellC.waitUntilShellClosed(SHELL_DELETE_RESOURCE);
            }
        }
    }

    public void deleteProjectWithGUI(String projectName) throws RemoteException {
        precondition();
        treeW.clickContextMenuOfTreeItemInView(VIEWNAME, DELETE, projectName);
        shellC.confirmWindowWithCheckBox(SHELL_DELETE_RESOURCE, OK, true);
        shellC.waitUntilShellClosed(SHELL_DELETE_RESOURCE);
    }

    public void deleteFolder(String... folderNodes) throws RemoteException {
        String folderpath = getPath(folderNodes);
        IPath path = new Path(getPath(folderNodes));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete folder " + folderpath, e);
            }
        }
    }

    public void deletePkg(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            IPath path = new Path(getPkgPath(projectName, pkg));
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
                .getRoot();
            IResource resource = root.findMember(path);
            if (resource.isAccessible()) {
                try {
                    FileUtil.delete(resource);
                    root.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    log.debug("Couldn't delete file " + projectName, e);
                }
            }
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void deleteFile(String... nodes) throws RemoteException {
        precondition();
        treeW.clickContextMenuOfTreeItemInView(VIEWNAME, DELETE, nodes);
        shellC.confirmShellDelete(OK);
    }

    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);

            } catch (CoreException e) {
                log.debug("Couldn't delete file " + className + ".java", e);
            }
        }
    }

    protected void precondition() throws RemoteException {
        pEV.openPEView();
        pEV.setFocusOnPEView();
    }

}
