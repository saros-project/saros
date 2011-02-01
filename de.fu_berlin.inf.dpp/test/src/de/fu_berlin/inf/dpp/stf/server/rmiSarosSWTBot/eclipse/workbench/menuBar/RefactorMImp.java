package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class RefactorMImp extends EclipseComponentImp implements RefactorM {

    private static transient RefactorMImp refactorImp;

    /**
     * {@link FileMImp} is a singleton, but inheritance is possible.
     */
    public static RefactorMImp getInstance() {
        if (refactorImp != null)
            return refactorImp;
        refactorImp = new RefactorMImp();
        return refactorImp;
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

    public void moveClassTo(String targetProject, String targetPkg)
        throws RemoteException {
        moveTo(SHELL_MOVE, OK, getPkgNodes(targetProject, targetPkg));
    }

    public void renameClass(String newName) throws RemoteException {
        rename(SHELL_RENAME_COMPiIATION_UNIT, FINISH, newName);
    }

    public void renameFile(String newName) throws RemoteException {
        rename(SHELL_RENAME_RESOURCE, OK, newName);
    }

    public void renameFolder(String newName) throws RemoteException {
        rename(SHELL_RENAME_RESOURCE, OK, newName);
    }

    public void renameJavaProject(String newName) throws RemoteException {
        rename("Rename Java Project", OK, newName);
    }

    public void renamePkg(String newName) throws RemoteException {
        rename(SHELL_RENAME_PACKAGE, OK, newName);
    }

    protected void precondition() throws RemoteException {
        workbenchC.activateWorkbench();
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/
    public void rename(String shellTitle, String buttonName, String newName)
        throws RemoteException {
        precondition();
        menuW.clickMenuWithTexts(MENU_REFACTOR, MENU_RENAME);
        shellC.activateShellWithText(shellTitle);
        bot.textWithLabel(LABEL_NEW_NAME).setText(newName);
        buttonW.waitUntilButtonEnabled(buttonName);
        bot.button(buttonName).click();
        shellC.waitUntilShellClosed(shellTitle);
    }

    public void moveTo(String shellTitle, String buttonName, String... nodes)
        throws RemoteException {
        precondition();
        menuW.clickMenuWithTexts(MENU_REFACTOR, MENU_MOVE);
        shellC.waitUntilShellActive(shellTitle);
        shellC.confirmShellWithTree(shellTitle, buttonName, nodes);
        shellC.waitUntilShellClosed(shellTitle);
    }

}
