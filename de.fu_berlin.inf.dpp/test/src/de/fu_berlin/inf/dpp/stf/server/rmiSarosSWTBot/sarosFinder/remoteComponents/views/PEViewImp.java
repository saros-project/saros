package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamCImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private STFBotTree tree;
    private static transient PEViewImp pEViewImp;

    private static SarosCImp sarosC;
    private static TeamCImp teamC;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();

        sarosC = SarosCImp.getInstance();
        teamC = TeamCImp.getInstance();

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
     * actions
     * 
     **********************************************/
    public void selectProject(String projectName) throws RemoteException {

        tree.selectTreeItemWithRegex(changeToRegex(projectName));
    }

    public void selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        tree.selectTreeItemWithRegex(changeToRegex(nodes));
    }

    public STFBotTreeItem selectClass(String projectName, String pkg,
        String className) throws RemoteException {

        String[] nodes = getClassNodes(projectName, pkg, className);
        return tree.selectTreeItemWithRegex(changeToRegex(nodes));
    }

    public void selectFolder(String... folderNodes) throws RemoteException {
        tree.selectTreeItemWithRegex(changeToRegex(folderNodes));
    }

    public STFBotTreeItem selectFile(String... fileNodes)
        throws RemoteException {
        return tree.selectTreeItemWithRegex(changeToRegex(fileNodes));
    }

    public SarosC saros() throws RemoteException {
        return sarosC;
    }

    public TeamC team() throws RemoteException {
        return teamC;
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

}
