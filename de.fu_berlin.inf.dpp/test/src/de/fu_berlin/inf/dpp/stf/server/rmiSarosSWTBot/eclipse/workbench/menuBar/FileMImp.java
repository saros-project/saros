package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

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
            confirmShellNewJavaProject(projectName);
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
        if (pkg.matches(PKG_REGEX)) {
            if (!existsPkgNoGUI(projectName, pkg))
                try {
                    precondition();
                    menuW.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_PACKAGE);
                    confirmShellNewJavaPackage(projectName, pkg);
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
                confirmShellNewFile(fileNodes);
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
                confirmShellNewJavaClass(projectName, pkg, className);
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
            shellW.activateShell(SHELL_NEW_JAVA_CLASS);
            bot.textWithLabel(LABEL_SOURCE_FOLDER).setText(
                projectName + "/" + SRC);
            bot.textWithLabel(LABEL_PACKAGE).setText(pkg);
            bot.textWithLabel(LABEL_NAME).setText(className);
            bot.button("Add...").click();
            shellW.activateShellAndWait("Implemented Interfaces Selection");
            bot.textWithLabel("Choose interfaces:").setText(
                "java.lang.Runnable");
            tableW.waitUntilTableHasRows(1);
            bot.button(OK).click();
            bot.shell(SHELL_NEW_JAVA_CLASS).activate();
            bot.checkBox("Inherited abstract methods").click();
            bot.button(FINISH).click();
            shellW.waitsUntilIsShellClosed(SHELL_NEW_JAVA_CLASS);
        }
    }

    public void newJavaProjectWithClasses(String projectName, String pkg,
        String... classNames) throws RemoteException {
        newJavaProject(projectName);
        for (String className : classNames) {
            newClass(projectName, pkg, className);
        }

    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/
    protected void precondition() throws RemoteException {
        workbench.activateWorkbench();
    }

    private void confirmShellNewJavaClass(String projectName, String pkg,
        String className) throws RemoteException {
        shellW.activateShell(SHELL_NEW_JAVA_CLASS);
        bot.textWithLabel(LABEL_SOURCE_FOLDER).setText(projectName + "/" + SRC);
        bot.textWithLabel(LABEL_PACKAGE).setText(pkg);
        bot.textWithLabel(LABEL_NAME).setText(className);
        bot.button(FINISH).click();
        shellW.waitsUntilIsShellClosed(SHELL_NEW_JAVA_CLASS);
    }

    private void confirmWizardNewProject(String projectName)
        throws RemoteException {
        shellW.confirmShellWithTree(SHELL_NEW_PROJECT, NEXT, NODE_GENERAL,
            NODE_PROJECT);
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        buttonW.clickButton(FINISH);
        shellW.waitsUntilIsShellClosed(SHELL_NEW_PROJECT);
        bot.sleep(50);
    }

    private void confirmShellNewFile(String... fileNodes)
        throws RemoteException {
        shellW.activateShell(SHELL_NEW_FILE);
        textW.setTextInTextWithLabel(getPath(getParentNodes(fileNodes)),
            LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER);
        bot.textWithLabel(LABEL_FILE_NAME).setText(getLastNode(fileNodes));
        buttonW.waitUntilButtonEnabled(FINISH);
        buttonW.clickButton(FINISH);
        shellW.waitsUntilIsShellClosed(SHELL_NEW_FILE);
    }

    private void confirmShellNewJavaPackage(String projectName, String pkg)
        throws RemoteException {
        shellW.activateShell(SHELL_NEW_JAVA_PACKAGE);
        bot.textWithLabel(LABEL_SOURCE_FOLDER).setText(
            (projectName + "/" + SRC));
        bot.textWithLabel(LABEL_NAME).setText(pkg);
        buttonW.clickButton(FINISH);
        shellW.waitsUntilIsShellClosed(SHELL_NEW_JAVA_PACKAGE);
    }

    private void confirmShellNewFolder(String... folderNodes)
        throws RemoteException {
        shellW.activateShell(SHELL_NEW_FOLDER);
        textW.setTextInTextWithLabel(getPath(getParentNodes(folderNodes)),
            LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER);
        textW.setTextInTextWithLabel(getLastNode(folderNodes),
            LABEL_FOLDER_NAME);
        buttonW.clickButton(FINISH);
        shellW.waitsUntilIsShellClosed(SHELL_NEW_FOLDER);
    }

    private void confirmShellNewJavaProject(String projectName)
        throws RemoteException {
        shellW.activateShell(SHELL_NEW_JAVA_PROJECT);
        textW.setTextInTextWithLabel(projectName, LABEL_PROJECT_NAME);
        buttonW.clickButton(FINISH);
        shellW.waitsUntilIsShellClosed(SHELL_NEW_JAVA_PROJECT);
    }
}
