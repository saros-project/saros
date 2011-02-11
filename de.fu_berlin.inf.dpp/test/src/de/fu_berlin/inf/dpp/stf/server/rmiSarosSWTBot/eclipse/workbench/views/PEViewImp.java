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
     * actions
     * 
     **********************************************/
    public void selectProject(String projectName) throws RemoteException {
        precondition();
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(projectName));
    }

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

    public void selectFolder(String... folderNodes) throws RemoteException {
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(folderNodes));
    }

    public void selectFile(String... fileNodes) throws RemoteException {
        treeW.selectTreeItemWithRegexsInView(VIEW_PACKAGE_EXPLORER,
            changeToRegex(fileNodes));
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        view(VIEW_PACKAGE_EXPLORER).openById();
        view(VIEW_PACKAGE_EXPLORER).setViewTitle(VIEW_PACKAGE_EXPLORER);
        view(VIEW_PACKAGE_EXPLORER).setFocus();
    }

}
