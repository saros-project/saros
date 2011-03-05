package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.ContextMenuWrapper;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class PEViewImp extends ViewsImp implements PEView {
    private static transient PEViewImp pEViewImp;

    private STFBotView view;
    private STFBotTree tree;

    /**
     * {@link PEViewImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();
        return pEViewImp;
    }

    public PEView setView(STFBotView view) throws RemoteException {
        this.view = view;
        tree = this.view.bot().tree();
        return this;
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

    public ContextMenuWrapper tree() throws RemoteException {
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(null);
        contextMenu.setTreeItemType(null);
        return contextMenu;
    }

    public ContextMenuWrapper selectSrc(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName), SRC),
            TreeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public ContextMenuWrapper selectJavaProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName)),
            TreeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public ContextMenuWrapper selectProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName)),
            TreeItemType.PROJECT);
        return contextMenu;
    }

    public ContextMenuWrapper selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(nodes)),
            TreeItemType.PKG);

        return contextMenu;
    }

    public ContextMenuWrapper selectClass(String projectName, String pkg,
        String className) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(nodes)),
            TreeItemType.CLASS);

        return contextMenu;
    }

    public ContextMenuWrapper selectFolder(String... folderNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(folderNodes)),
            TreeItemType.FOLDER);

        return contextMenu;
    }

    public ContextMenuWrapper selectFile(String... fileNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(fileNodes)),
            TreeItemType.FILE);

        return contextMenu;
    }

    public String getTitle() throws RemoteException {
        return VIEW_PACKAGE_EXPLORER;
    }

    public boolean isProjectManagedBySVN(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    public String getRevision(String fullPath) throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + fullPath
                + "\" not found.");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        VCSResourceInfo info = vcs.getCurrentResourceInfo(resource);
        String result = info != null ? info.revision : null;
        return result;
    }

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource not found at \"" + fullPath
                + "\"");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        bot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            bot().waitUntil(
                SarosConditions.isResourceExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            bot().waitUntil(
                SarosConditions
                    .isResourceNotExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        bot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        bot().waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        bot().waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        bot().waitUntilShellIsClosed(SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isUrlSame(fullPath, url));
    }

    private void initContextMenuWrapper(STFBotTreeItem treeItem,
        TreeItemType type) {
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(treeItem);
        contextMenu.setTreeItemType(type);
    }

}
