package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.util.FileUtil;

public class EditMImp extends EclipseComponentImp implements EditM {

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

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void deleteAllProjects() throws RemoteException {
        precondition();
        SWTBotTreeItem[] allTreeItems = treeW.getTreeInView(
            VIEW_PACKAGE_EXPLORER).getAllItems();
        if (allTreeItems != null) {
            for (SWTBotTreeItem item : allTreeItems) {
                item.contextMenu(MENU_DELETE).click();
                shellW.confirmWindowWithCheckBox(SHELL_DELETE_RESOURCE, OK,
                    true);
                shellW.waitUntilShellClosed(SHELL_DELETE_RESOURCE);
            }
        }
    }

    public void deleteProject() throws RemoteException {
        precondition();
        menuW.clickMenuWithTexts(MENU_EDIT, MENU_DELETE);
        shellW.confirmWindowWithCheckBox(SHELL_DELETE_RESOURCE, OK, true);
        shellW.waitUntilShellClosed(SHELL_DELETE_RESOURCE);
    }

    public void deleteFile() throws RemoteException {
        precondition();
        menuW.clickMenuWithTexts(MENU_EDIT, MENU_DELETE);
        shellW.confirmShellDelete(OK);
    }

    public void copyProject(String target) throws RemoteException {
        if (fileM.existsProject(target)) {
            throw new RemoteException("Can't copy project" + " to " + target
                + " , the target already exists.");
        }
        precondition();
        menuW.clickMenuWithTexts(MENU_EDIT, MENU_COPY);
        menuW.clickMenuWithTexts(MENU_EDIT, MENU_PASTE);
        shellW.activateShell("Copy Project");
        textW.setTextInTextWithLabel(target, "Project name:");
        buttonW.clickButton(OK);
        shellW.waitUntilShellClosed("Copy Project");
        bot.sleep(1000);
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    public void deleteProjectNoGUI(String projectName) throws RemoteException {
        IPath path = new Path(projectName);
        deleteNoGUI(path);
    }

    public void deleteFolderNoGUI(String... folderNodes) throws RemoteException {
        IPath path = new Path(getPath(folderNodes));
        deleteNoGUI(path);
    }

    public void deletePkgNoGUI(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            IPath path = new Path(getPkgPath(projectName, pkg));
            deleteNoGUI(path);
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void deleteClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        deleteNoGUI(path);
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        workbench.activateWorkbench();
    }

    private void deleteNoGUI(IPath path) {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource == null) {
            log.debug(" Can't find resource");
            return;
        }
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete the resource", e);
            }
        }
    }

}
