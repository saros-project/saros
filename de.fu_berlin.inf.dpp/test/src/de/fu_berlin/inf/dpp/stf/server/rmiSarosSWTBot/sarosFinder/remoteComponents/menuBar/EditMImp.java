package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.util.FileUtils;

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

    public void deleteAllProjects(String viewTitle) throws RemoteException {
        precondition();
        STFBotTree tree = bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree();
        List<String> allTreeItems = tree.getSubtems();

        if (allTreeItems != null) {
            for (String item : allTreeItems) {
                tree.selectTreeItem(item).contextMenu(MENU_DELETE).click();
                STFBotShell shell = bot().shell(SHELL_DELETE_RESOURCE);

                shell.confirmWindowWithCheckBox(OK, true);
                bot().waitsUntilIsShellClosed(SHELL_DELETE_RESOURCE);
            }
        }
    }

    public void deleteProject() throws RemoteException {
        precondition();
        bot().menu(MENU_EDIT).menu(MENU_DELETE).click();
        bot().shell(SHELL_DELETE_RESOURCE).confirmWindowWithCheckBox(OK, true);
        bot().waitsUntilIsShellClosed(SHELL_DELETE_RESOURCE);
    }

    public void deleteAllItemsOfJavaProject(String viewTitle, String projectName)
        throws RemoteException {

        STFBotTreeItem treeItem = bot().view(viewTitle).bot_().tree()
            .selectTreeItem(projectName, SRC);
        for (String item : treeItem.getSubItems()) {
            bot().view(viewTitle).bot_().tree()
                .selectTreeItem(projectName, SRC, item).contextMenu(CM_DELETE)
                .click();

            bot().waitUntilShellOpen(CONFIRM_DELETE);
            bot().shell(CONFIRM_DELETE).activate();
            bot().shell(CONFIRM_DELETE).bot_().button(OK).click();
        }
    }

    public void deleteFile() throws RemoteException {
        precondition();
        bot().menu(MENU_EDIT).menu(MENU_DELETE).click();
        bot().waitUntilShellOpen(CONFIRM_DELETE);
        bot().shell(CONFIRM_DELETE).activate();
        bot().shell(CONFIRM_DELETE).bot_().button(OK).click();
        bot.sleep(300);
    }

    public void copyProject(String target) throws RemoteException {
        if (existsProjectNoGUI(target)) {
            throw new RemoteException("Can't copy project" + " to " + target
                + " , the target already exists.");
        }
        precondition();
        bot().menu(MENU_EDIT).menu(MENU_COPY).click();
        bot().menu(MENU_EDIT).menu(MENU_PASTE).click();
        STFBotShell shell = bot().shell(SHELL_COPY_PROJECT);
        shell.activate();
        shell.bot_().textWithLabel("Project name:").setText(target);
        shell.bot_().button(OK).click();
        bot().waitsUntilIsShellClosed(SHELL_COPY_PROJECT);
        bot.sleep(1000);
    }

    /**************************************************************
     * 
     * No GUI
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
        if (pkg.matches("[\\w*\\.]*\\w*")) {
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

    public void deleteAllProjectsNoGUI() throws RemoteException {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++) {
            try {
                FileUtils.delete(projects[i]);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete files ", e);
            }
        }
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    private void precondition() throws RemoteException {
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
                FileUtils.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete the resource", e);
            }
        }
    }

}
