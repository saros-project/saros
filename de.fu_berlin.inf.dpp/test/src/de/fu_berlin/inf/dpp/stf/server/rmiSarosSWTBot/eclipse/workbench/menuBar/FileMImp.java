package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class FileMImp extends EclipseComponentImp implements FileM {

    private static transient FileMImp fileImp;

    /**
     * {@link FileMImp} is a singleton, but inheritance is possible.
     */
    public static FileMImp getInstance() {
        if (fileImp != null)
            return fileImp;
        fileImp = new FileMImp();
        return fileImp;
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
    public void newProject(String projectName) throws RemoteException {
        if (!existsProjectNoGUI(projectName)) {
            precondition();
            menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_PROJECT);
            confirmWizardNewProject(projectName);
        }
    }

    public void newJavaProject(String projectName) throws RemoteException {
        if (!existsProjectNoGUI(projectName)) {
            precondition();
            menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_JAVA_PROJECT);
            confirmWindowNewJavaProject(projectName);
        }
    }

    public void newFolder(String... folderNodes) throws RemoteException {
        precondition();
        if (!existsFolderNoGUI(folderNodes)) {
            try {
                menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_FOLDER);
                confirmShellNewFolder(folderNodes);
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void newPackage(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            if (!existsPkgNoGUI(projectName, pkg))
                try {
                    precondition();
                    menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_PACKAGE);
                    confirmWindowNewJavaPackage(projectName, pkg);
                } catch (WidgetNotFoundException e) {
                    final String cause = "error creating new package";
                    log.error(cause, e);
                    throw new RemoteException(cause, e);
                }
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void newFile(String... fileNodes) throws RemoteException {
        if (!existsFileNoGUI(getPath(fileNodes)))
            try {
                precondition();
                menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_FILE);
                confirmWindowNewFile(fileNodes);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new file.";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public void newClass(String projectName, String pkg, String className)
        throws RemoteException {
        if (!existsFileNoGUI(getClassPath(projectName, pkg, className))) {
            try {
                precondition();
                menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_CLASS);
                confirmWindowNewJavaClass(projectName, pkg, className);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new Java Class";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException {
        if (!existsFileNoGUI(getClassPath(projectName, pkg, className))) {
            precondition();
            menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_CLASS);
        }
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        bot.textWithLabel("Source folder:").setText(projectName + "/src");
        bot.textWithLabel("Package:").setText(pkg);
        bot.textWithLabel("Name:").setText(className);
        bot.button("Add...").click();
        shellW.waitUntilShellActive("Implemented Interfaces Selection");
        bot.shell("Implemented Interfaces Selection").activate();
        SWTBotText text = bot.textWithLabel("Choose interfaces:");
        bot.sleep(2000);
        text.setText("java.lang.Runnable");
        tableW.waitUntilTableHasRows(1);
        bot.button(OK).click();
        bot.shell(SHELL_NEW_JAVA_CLASS).activate();
        bot.checkBox("Inherited abstract methods").click();
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    public void newJavaProjectWithClasses(String projectName, String pkg,
        String... classNames) throws RemoteException {
        newJavaProject(projectName);
        for (String className : classNames) {
            newClass(projectName, pkg, className);
        }

    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/
    public boolean existsFile(String... nodes) throws RemoteException {
        workbench.activateWorkbench();
        precondition();
        SWTBotTree tree = treeW.getTreeInView(VIEW_PACKAGE_EXPLORER);
        return treeW.existsTreeItemWithRegexs(tree, nodes);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/
    protected void precondition() throws RemoteException {
        workbench.activateWorkbench();
    }

    protected void precondition(String viewTitle) throws RemoteException {
        precondition();
        viewW.openViewById(viewTitlesAndIDs.get(viewTitle));
        viewW.setFocusOnViewByTitle(viewTitle);
    }

    private void confirmWindowNewJavaClass(String projectName, String pkg,
        String className) {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        bot.textWithLabel("Source folder:").setText(projectName + "/src");
        bot.textWithLabel("Package:").setText(pkg);
        bot.textWithLabel("Name:").setText(className);
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private void confirmWizardNewProject(String projectName)
        throws RemoteException {
        shellW.confirmShellWithTree(SHELL_NEW_PROJECT, NEXT, NODE_GENERAL,
            NODE_PROJECT);
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        shellW.waitUntilShellClosed(SHELL_NEW_PROJECT);
        bot.sleep(50);
    }

    private void confirmWindowNewFile(String... fileNodes)
        throws RemoteException {

        shellW.activateShell(SHELL_NEW_FILE);
        textW.setTextInTextWithLabel(getPath(getParentNodes(fileNodes)),
            LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER);
        bot.textWithLabel(LABEL_FILE_NAME).setText(getLastNode(fileNodes));
        buttonW.waitUntilButtonEnabled(FINISH);
        buttonW.clickButton(FINISH);
        shellW.waitUntilShellClosed(SHELL_NEW_FILE);
    }

    private void confirmWindowNewJavaPackage(String projectName, String pkg)
        throws RemoteException {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_PACKAGE);
        shell.activate();
        bot.textWithLabel("Source folder:").setText((projectName + "/src"));
        bot.textWithLabel("Name:").setText(pkg);
        bot.button(FINISH).click();
        shellW.waitUntilShellClosed(SHELL_NEW_JAVA_PACKAGE);
    }

    private void confirmShellNewFolder(String... folderNodes)
        throws RemoteException {
        shellW.activateShell(SHELL_NEW_FOLDER);
        bot.text().setText(getPath(getParentNodes(folderNodes)));
        // textW.setTextInTextWithLabel(getPath(getParentNodes(folderNodes)),
        // LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER);
        bot.textWithLabel(LABEL_FOLDER_NAME).setText(getLastNode(folderNodes));
        buttonW.clickButton(FINISH);
        shellW.waitUntilShellClosed(SHELL_NEW_FOLDER);
    }

    private void confirmWindowNewJavaProject(String projectName)
        throws RemoteException {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_PROJECT);
        shell.activate();
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        shellW.waitUntilShellClosed(SHELL_NEW_JAVA_PROJECT);
    }

}
