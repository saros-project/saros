package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.ContextMenuWrapper;

public class PEViewImp extends ViewsImp implements PEView {
    private static transient PEViewImp pEViewImp;

    private STFBotView view;
    private STFBotTree tree;
    private STFBotTreeItem treeItem;

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
        setViewWithTree(view);
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

    private void setViewWithTree(STFBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        treeItem = null;
    }

    private void initContextMenuWrapper(STFBotTreeItem treeItem,
        TreeItemType type) {
        this.treeItem = treeItem;
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(treeItem);
        contextMenu.setTreeItemType(type);
    }

}
