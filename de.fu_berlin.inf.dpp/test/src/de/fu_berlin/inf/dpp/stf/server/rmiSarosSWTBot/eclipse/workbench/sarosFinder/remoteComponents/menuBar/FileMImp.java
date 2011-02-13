package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;

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
            stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_PROJECT);
            confirmWizardNewProject(projectName);
        }
    }

    public void newJavaProject(String projectName) throws RemoteException {
        if (!existsProjectNoGUI(projectName)) {
            precondition();
            stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_JAVA_PROJECT);
            confirmShellNewJavaProject(projectName);
        }
    }

    public void newFolder(String... folderNodes) throws RemoteException {
        precondition();
        if (!existsFolderNoGUI(folderNodes)) {
            try {
                stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_FOLDER);
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
                    stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW,
                        MENU_PACKAGE);
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
                stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_FILE);
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
                stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_CLASS);
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
            stfMenu.clickMenuWithTexts(MENU_FILE, MENU_NEW, MENU_CLASS);
            bot().shell(SHELL_NEW_JAVA_CLASS).activate();
            bot.textWithLabel(LABEL_SOURCE_FOLDER).setText(
                projectName + "/" + SRC);
            bot.textWithLabel(LABEL_PACKAGE).setText(pkg);
            bot.textWithLabel(LABEL_NAME).setText(className);
            bot.button("Add...").click();
            bot().shell("Implemented Interfaces Selection").activateAndWait();
            bot.textWithLabel("Choose interfaces:").setText(
                "java.lang.Runnable");
            stfTable.waitUntilTableHasRows(1);
            bot.button(OK).click();
            bot().shell(SHELL_NEW_JAVA_CLASS).activate();
            bot.checkBox("Inherited abstract methods").click();
            bot.button(FINISH).click();
            bot().waitsUntilIsShellClosed(SHELL_NEW_JAVA_CLASS);
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
        bot().shell(SHELL_NEW_JAVA_CLASS).activate();
        bot.textWithLabel(LABEL_SOURCE_FOLDER).setText(projectName + "/" + SRC);
        bot.textWithLabel(LABEL_PACKAGE).setText(pkg);
        bot.textWithLabel(LABEL_NAME).setText(className);
        bot.button(FINISH).click();
        bot().waitsUntilIsShellClosed(SHELL_NEW_JAVA_CLASS);
    }

    private void confirmWizardNewProject(String projectName)
        throws RemoteException {
        bot().shell(SHELL_NEW_PROJECT).confirmShellWithTree(NEXT, NODE_GENERAL,
            NODE_PROJECT);
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        bot().waitsUntilIsShellClosed(SHELL_NEW_PROJECT);
        // bot.sleep(50);
    }

    private void confirmShellNewFile(String... fileNodes)
        throws RemoteException {
        bot().shell(SHELL_NEW_FILE).activate();
        stfText.setTextInTextWithLabel(getPath(getParentNodes(fileNodes)),
            LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER);
        bot.textWithLabel(LABEL_FILE_NAME).setText(getLastNode(fileNodes));
        bot().shell(SHELL_NEW_FILE).bot_().button(FINISH).waitUntilIsEnabled();
        bot.button(FINISH).click();
        bot().waitsUntilIsShellClosed(SHELL_NEW_FILE);
    }

    private void confirmShellNewJavaPackage(String projectName, String pkg)
        throws RemoteException {
        bot().shell(SHELL_NEW_JAVA_PACKAGE).activate();
        bot.textWithLabel(LABEL_SOURCE_FOLDER).setText(
            (projectName + "/" + SRC));
        bot.textWithLabel(LABEL_NAME).setText(pkg);
        bot.button(FINISH).click();
        if (bot().isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT))
            bot().waitsUntilIsShellClosed(SHELL_CREATE_NEW_XMPP_ACCOUNT);
    }

    private void confirmShellNewFolder(String... folderNodes)
        throws RemoteException {
        bot().shell(SHELL_NEW_FOLDER).activate();
        stfText.setTextInTextWithLabel(getPath(getParentNodes(folderNodes)),
            LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER);
        stfText.setTextInTextWithLabel(getLastNode(folderNodes),
            LABEL_FOLDER_NAME);
        bot.button(FINISH).click();
        bot().waitsUntilIsShellClosed(SHELL_NEW_FOLDER);
    }

    private void confirmShellNewJavaProject(String projectName)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_JAVA_PROJECT);
        shell.activate();
        stfText.setTextInTextWithLabel(projectName, LABEL_PROJECT_NAME);
        // bot.button(FINISH).click();
        shell.bot_().button(FINISH).click();
        bot().waitsUntilIsShellClosed(SHELL_NEW_JAVA_PROJECT);
    }
}
