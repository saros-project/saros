package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamCImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private static transient PEViewImp pEViewImp;
    private static OpenCImp openC;
    private static SarosCImp sarosC;
    private static TeamCImp teamC;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();
        openC = OpenCImp.getInstance();
        sarosC = SarosCImp.getInstance();
        teamC = TeamCImp.getInstance();

        return pEViewImp;
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
        precondition();
        bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItemWithRegex(changeToRegex(projectName));
    }

    public void selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItemWithRegex(changeToRegex(nodes));
    }

    public void selectClass(String projectName, String pkg, String className)
        throws RemoteException {
        precondition();
        String[] nodes = getClassNodes(projectName, pkg, className);
        bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItemWithRegex(changeToRegex(nodes));
    }

    public void selectFolder(String... folderNodes) throws RemoteException {

        bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItemWithRegex(changeToRegex(folderNodes));
    }

    public void selectFile(String... fileNodes) throws RemoteException {

        bot().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItemWithRegex(changeToRegex(fileNodes));
    }

    public OpenC open() throws RemoteException {
        return openC;
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

    protected void precondition() throws RemoteException {
        bot().openViewById(VIEW_PACKAGE_EXPLORER_ID);
        bot().view(VIEW_PACKAGE_EXPLORER).show();
    }

}
