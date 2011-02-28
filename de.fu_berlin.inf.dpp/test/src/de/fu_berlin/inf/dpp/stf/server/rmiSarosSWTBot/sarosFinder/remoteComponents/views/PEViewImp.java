package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.ContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.ContextMenuWrapperImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenCImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private STFBotTree tree;
    private static transient PEViewImp pEViewImp;
    private static OpenCImp openC;

    private static ContextMenuWrapperImp contextMenu;

    /**
     * {@link PEViewImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();
        openC = OpenCImp.getInstance();

        contextMenu = ContextMenuWrapperImp.getInstance();

        return pEViewImp;
    }

    public PEView setWidget(STFBotTree tree) {
        this.tree = tree;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public OpenC open() throws RemoteException {
        openC.setView(this);
        return openC;
    }

    // public SarosC saros() throws RemoteException {
    // sarosC.setView(tree);
    // return sarosC;
    // }
    //
    // public TeamC team() throws RemoteException {
    // teamC.setView(tree);
    // return teamC;
    // }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public ContextMenuWrapper tree() throws RemoteException {
        contextMenu.setTree(tree);
        return contextMenu;
    }

    public ContextMenuWrapper selectJavaProject(String projectName)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(projectName)));
        contextMenu.setTreeItemType(treeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public ContextMenuWrapper selectProject(String projectName)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(projectName)));
        contextMenu.setTreeItemType(treeItemType.PROJECT);
        return contextMenu;
    }

    public ContextMenuWrapper selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(nodes)));
        contextMenu.setTreeItemType(treeItemType.PKG);
        return contextMenu;
    }

    public ContextMenuWrapper selectClass(String projectName, String pkg,
        String className) throws RemoteException {

        String[] nodes = getClassNodes(projectName, pkg, className);
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(nodes)));
        contextMenu.setTreeItemType(treeItemType.CLASS);
        return contextMenu;
    }

    public ContextMenuWrapper selectFolder(String... folderNodes)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(folderNodes)));
        contextMenu.setTreeItemType(treeItemType.FOLDER);
        return contextMenu;
    }

    public ContextMenuWrapper selectFile(String... fileNodes)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(fileNodes)));
        contextMenu.setTreeItemType(treeItemType.FILE);
        return contextMenu;
    }

    public String getTitle() throws RemoteException {
        return VIEW_PACKAGE_EXPLORER;
    }

}
