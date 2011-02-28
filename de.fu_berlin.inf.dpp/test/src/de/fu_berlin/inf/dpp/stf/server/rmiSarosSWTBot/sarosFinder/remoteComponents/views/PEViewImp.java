package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapperImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private STFBotView view;
    private STFBotTree tree;
    private static transient PEViewImp pEViewImp;
    private static OpenCImp openC;

    private static SarosContextMenuWrapperImp contextMenu;

    /**
     * {@link PEViewImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();
        openC = OpenCImp.getInstance();
        contextMenu = SarosContextMenuWrapperImp.getInstance();
        return pEViewImp;
    }

    public PEView setView(STFBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
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

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public SarosContextMenuWrapper tree() throws RemoteException {
        contextMenu.setTree(tree);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectJavaProject(String projectName)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(projectName)));
        contextMenu.setTreeItemType(treeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectProject(String projectName)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(projectName)));
        contextMenu.setTreeItemType(treeItemType.PROJECT);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(nodes)));
        contextMenu.setTreeItemType(treeItemType.PKG);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectClass(String projectName, String pkg,
        String className) throws RemoteException {

        String[] nodes = getClassNodes(projectName, pkg, className);
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(nodes)));
        contextMenu.setTreeItemType(treeItemType.CLASS);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectFolder(String... folderNodes)
        throws RemoteException {
        contextMenu.setTreeItem(tree
            .selectTreeItemWithRegex(changeToRegex(folderNodes)));
        contextMenu.setTreeItemType(treeItemType.FOLDER);
        return contextMenu;
    }

    public SarosContextMenuWrapper selectFile(String... fileNodes)
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
