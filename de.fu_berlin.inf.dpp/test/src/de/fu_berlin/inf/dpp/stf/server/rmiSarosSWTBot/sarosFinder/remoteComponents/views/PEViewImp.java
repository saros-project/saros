package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapperImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private STFBotView view;
    private STFBotTree tree;
    private STFBotTreeItem treeItem;
    private static transient PEViewImp pEViewImp;

    private static SarosContextMenuWrapperImp contextMenu;

    /**
     * {@link PEViewImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();

        contextMenu = SarosContextMenuWrapperImp.getInstance();
        return pEViewImp;
    }

    public PEView setView(STFBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        treeItem = null;
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

    public SarosContextMenuWrapper tree() throws RemoteException {
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(null);
        contextMenu.setTreeItemType(null);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectSrc(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName), SRC),
            TreeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectJavaProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName)),
            TreeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName)),
            TreeItemType.PROJECT);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(nodes)),
            TreeItemType.PKG);

        return contextMenu;
    }

    public SarosContextMenuWrapper selectClass(String projectName, String pkg,
        String className) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(nodes)),
            TreeItemType.CLASS);

        return contextMenu;
    }

    public SarosContextMenuWrapper selectFolder(String... folderNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(folderNodes)),
            TreeItemType.FOLDER);

        return contextMenu;
    }

    public SarosContextMenuWrapper selectFile(String... fileNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(fileNodes)),
            TreeItemType.FILE);

        return contextMenu;
    }

    public String getTitle() throws RemoteException {
        return VIEW_PACKAGE_EXPLORER;
    }

    private void initContextMenuWrapper(STFBotTreeItem treeItem,
        TreeItemType type) {
        this.treeItem = treeItem;
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(treeItem);
        contextMenu.setTreeItemType(type);
    }

}
