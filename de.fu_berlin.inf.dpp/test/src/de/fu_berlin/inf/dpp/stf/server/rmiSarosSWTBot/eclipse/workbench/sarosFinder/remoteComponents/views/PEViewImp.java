package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private static transient PEViewImp pEViewImp;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();
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
        bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItemWithRegex(changeToRegex(projectName));
    }

    public void selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItemWithRegex(changeToRegex(nodes));
    }

    public void selectClass(String projectName, String pkg, String className)
        throws RemoteException {
        precondition();
        String[] nodes = getClassNodes(projectName, pkg, className);
        bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItemWithRegex(changeToRegex(nodes));
    }

    public void selectFolder(String... folderNodes) throws RemoteException {

        bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItemWithRegex(changeToRegex(folderNodes));
    }

    public void selectFile(String... fileNodes) throws RemoteException {

        bot().view(VIEW_PACKAGE_EXPLORER).bot_().tree()
            .selectTreeItemWithRegex(changeToRegex(fileNodes));
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        bot().openById(VIEW_PACKAGE_EXPLORER_ID);
        bot().view(VIEW_PACKAGE_EXPLORER).setFocus();
    }

}
