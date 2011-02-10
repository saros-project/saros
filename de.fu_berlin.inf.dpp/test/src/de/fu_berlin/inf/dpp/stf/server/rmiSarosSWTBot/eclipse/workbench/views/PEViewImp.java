package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.TableImp;

public class PEViewImp extends EclipseComponentImp implements PEView {

    private static transient PEViewImp pEViewImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
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
     * States
     * 
     **********************************************/
    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEW_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return viewW.isViewActive(VIEW_REMOTE_SCREEN);
    }

    public void selectProject(String projectName) throws RemoteException {
        precondition();
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(projectName));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(nodes));
    }

    public void selectClass(String projectName, String pkg, String className)
        throws RemoteException {
        precondition();
        String[] nodes = getClassNodes(projectName, pkg, className);
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(nodes));

    }

    public void selectFolder(String... pathToFolder) throws RemoteException {
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(pathToFolder));
    }

    public void selectFile(String... pathToFile) throws RemoteException {
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(pathToFile));
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        viewW.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        viewW.setFocusOnViewByTitle(VIEW_PACKAGE_EXPLORER);
    }

}
