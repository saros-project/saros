package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class RefactorMImp extends EclipsePart implements RefactorM {

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

    private final static String SHELL_MOVE = "Move";
    private final static String SHELL_RENAME_PACKAGE = "Rename Package";
    private final static String SHELL_RENAME_RESOURCE = "Rename Resource";
    private final static String SHELL_RENAME_COMPiIATION_UNIT = "Rename Compilation Unit";
    private final static String LABEL_NEW_NAME = "New name:";
    /* Context menu of a selected tree item on the package explorer view */

    private final static String REFACTOR = "Refactor";

    /* All the sub menus of the context menu "Refactor" */
    private final static String RENAME = "Rename...";
    private final static String MOVE = "Move...";
    protected final static String VIEWNAME = "Package Explorer";

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Refactor"
     * 
     **********************************************/

    public void moveClassTo(String sourceProject, String sourcePkg,
        String className, String targetProject, String targetPkg)
        throws RemoteException {
        precondition();

        String[] nodes = getClassNodes(sourceProject, sourcePkg, className);
        String[] contexts = { REFACTOR, MOVE };
        treeW.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(nodes));
        shellC.waitUntilShellActive(SHELL_MOVE);
        shellC.confirmShellWithTree(SHELL_MOVE, OK, targetProject, SRC,
            targetPkg);
        shellC.waitUntilShellClosed(SHELL_MOVE);
    }

    public void rename(String shellTitle, String confirmLabel, String newName,
        String[] nodes) throws RemoteException {
        precondition();

        String[] contexts = { REFACTOR, RENAME };
        treeW.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(nodes));
        shellC.activateShellWithText(shellTitle);
        bot.textWithLabel(LABEL_NEW_NAME).setText(newName);
        buttonW.waitUntilButtonEnabled(confirmLabel);
        bot.button(confirmLabel).click();
        shellC.waitUntilShellClosed(shellTitle);
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);
        String[] contexts = { REFACTOR, RENAME };
        treeW.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(nodes));

        String shellTitle = SHELL_RENAME_COMPiIATION_UNIT;
        shellC.activateShellWithText(shellTitle);
        bot.textWithLabel(LABEL_NEW_NAME).setText(newName);
        buttonW.waitUntilButtonEnabled(FINISH);
        bot.button(FINISH).click();
        /*
         * TODO Sometimes the window doesn't close when clicking on Finish, but
         * stays open with the warning 'class contains a main method blabla'. In
         * this case just click Finish again.
         * 
         * @Andreas, this problem dones't exist by me, so i get still the
         * exception: "button isn't enabled" by performing the following code. I
         * comment it out first, if my change doesn't work by you, please tell
         * me.
         */
        // bot.sleep(50);
        // if (shellC.isShellOpen(SHELL_RENAME_COMPiIATION_UNIT)) {
        // bot.button(FINISH).click();
        // }
        shellC.waitUntilShellClosed(shellTitle);
    }

    public void renameFile(String newName, String... nodes)
        throws RemoteException {
        rename(SHELL_RENAME_RESOURCE, OK, newName, nodes);
    }

    public void renameFolder(String newName, String... nodes)
        throws RemoteException {
        rename(SHELL_RENAME_RESOURCE, OK, newName, nodes);
    }

    public void renameJavaProject(String newName, String... nodes)
        throws RemoteException {
        rename("Rename Java Project", OK, newName, nodes);
    }

    public void renamePkg(String newName, String projectName, String pkg)
        throws RemoteException {
        String[] pkgNodes = getPkgNodes(projectName, pkg);
        rename(SHELL_RENAME_PACKAGE, OK, newName, pkgNodes);
    }

    protected void precondition() throws RemoteException {
        pEV.openPEView();
        pEV.setFocusOnPEView();
    }

}
