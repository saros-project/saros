package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class NewCImp extends EclipseComponentImp implements NewC {

    private static transient NewCImp self;

    private STFBotTreeItem treeItem;
    private STFBotTree tree;

    /**
     * {@link NewCImp} is a singleton, but inheritance is possible.
     */
    public static NewCImp getInstance() {
        if (self != null)
            return self;
        self = new NewCImp();
        return self;
    }

    public void setTreeItem(STFBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTree(STFBotTree tree) {
        this.tree = tree;
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
    public void project(String projectName) throws RemoteException {
        if (!exists(projectName)) {
            tree.contextMenu(MENU_NEW, MENU_PROJECT).click();
            confirmWizardNewProject(projectName);
        }
    }

    public void javaProject(String projectName) throws RemoteException {
        if (!exists(projectName)) {
            tree.contextMenu(MENU_NEW, MENU_JAVA_PROJECT).click();
            confirmShellNewJavaProject(projectName);
        }
    }

    public void folder(String folderName) throws RemoteException {
        if (!exists(folderName)) {
            try {
                treeItem.contextMenu(MENU_NEW, MENU_FOLDER).click();
                confirmShellNewFolder(folderName);
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void okg(String projectName, String pkg) throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            // if (!sarosBot().state().existsPkgNoGUI(projectName, pkg))
            try {
                precondition();
                bot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_PACKAGE).click();
                confirmShellNewJavaPackage(projectName, pkg);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new package";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
        // else {
        // throw new RuntimeException(
        // "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        // }
    }

    public void file(String fileName) throws RemoteException {
        if (!exists(fileName))
            try {
                treeItem.contextMenu(MENU_NEW, MENU_FILE).click();
                confirmShellNewFile(fileName);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new file.";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public void cls(String className) throws RemoteException {
        if (!exists(className)) {
            try {
                treeItem.contextMenu(MENU_NEW, MENU_CLASS).click();
                confirmShellNewJavaClass(className);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new Java Class";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void cls(String projectName, String pkg, String className)
        throws RemoteException {
        // if (!sarosBot().state().existsFileNoGUI(
        // getClassPath(projectName, pkg, className))) {
        try {
            precondition();
            bot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();
            confirmShellNewJavaClass(projectName, pkg, className);
        } catch (WidgetNotFoundException e) {
            final String cause = "error creating new Java Class";
            log.error(cause, e);
            throw new RemoteException(cause, e);
        }
        // }
    }

    public void clsImplementsRunnable(String className) throws RemoteException {
        if (!exists(className)) {
            bot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();
            STFBotShell shell_new = bot().shell(SHELL_NEW_JAVA_CLASS);
            shell_new.activate();

            shell_new.bot().textWithLabel(LABEL_NAME).setText(className);
            shell_new.bot().button("Add...").click();
            bot().waitUntilShellIsOpen("Implemented Interfaces Selection");
            STFBotShell shell = bot().shell("Implemented Interfaces Selection");
            shell.activate();
            shell.bot().textWithLabel("Choose interfaces:")
                .setText("java.lang.Runnable");
            shell.bot().table().waitUntilTableHasRows(1);
            shell.bot().button(OK).click();
            bot().shell(SHELL_NEW_JAVA_CLASS).activate();
            shell.bot().checkBox("Inherited abstract methods").click();
            shell.bot().button(FINISH).click();
            bot().waitsUntilShellIsClosed(SHELL_NEW_JAVA_CLASS);
        }
    }

    public void javaProjectWithClasses(String projectName, String pkg,
        String... classNames) throws RemoteException {
        javaProject(projectName);
        for (String className : classNames) {
            cls(projectName, pkg, className);
        }

    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/
    protected void precondition() throws RemoteException {
        bot().activateWorkbench();
    }

    private void confirmShellNewJavaClass(String className)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        shell.bot().textWithLabel(LABEL_NAME).setText(className);
        shell.bot().button(FINISH).click();
        bot().waitsUntilShellIsClosed(SHELL_NEW_JAVA_CLASS);
    }

    private void confirmShellNewJavaClass(String projectName, String pkg,
        String className) throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        shell.bot().textWithLabel(LABEL_SOURCE_FOLDER)
            .setText(projectName + "/" + SRC);
        shell.bot().textWithLabel(LABEL_PACKAGE).setText(pkg);
        shell.bot().textWithLabel(LABEL_NAME).setText(className);
        shell.bot().button(FINISH).click();
        bot().waitsUntilShellIsClosed(SHELL_NEW_JAVA_CLASS);
    }

    private void confirmWizardNewProject(String projectName)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_PROJECT);
        shell.confirmWithTree(NEXT, NODE_GENERAL, NODE_PROJECT);
        shell.bot().textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        shell.bot().button(FINISH).click();
        bot().waitsUntilShellIsClosed(SHELL_NEW_PROJECT);
        // bot.sleep(50);
    }

    private void confirmShellNewFile(String fileName) throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_FILE);
        shell.activate();
        shell.bot().textWithLabel(LABEL_FILE_NAME).setText(fileName);
        shell.bot().button(FINISH).waitUntilIsEnabled();
        shell.bot().button(FINISH).click();
        bot().waitsUntilShellIsClosed(SHELL_NEW_FILE);
    }

    private void confirmShellNewJavaPackage(String projectName, String pkg)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_JAVA_PACKAGE);
        shell.activate();
        shell.bot().textWithLabel(LABEL_SOURCE_FOLDER)
            .setText((projectName + "/" + SRC));
        shell.bot().textWithLabel(LABEL_NAME).setText(pkg);
        shell.bot().button(FINISH).click();
        if (bot().isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT))
            bot().waitsUntilShellIsClosed(SHELL_CREATE_NEW_XMPP_ACCOUNT);
    }

    private void confirmShellNewFolder(String folderName)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_FOLDER);
        shell.activate();

        shell.bot().textWithLabel(LABEL_FOLDER_NAME).setText(folderName);

        shell.bot().button(FINISH).click();
        bot().waitsUntilShellIsClosed(SHELL_NEW_FOLDER);
    }

    private void confirmShellNewJavaProject(String projectName)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_NEW_JAVA_PROJECT);
        shell.activate();
        shell.bot().textWithLabel(LABEL_PROJECT_NAME).setText(projectName);

        // bot.button(FINISH).click();
        shell.bot().button(FINISH).click();
        bot().waitsUntilShellIsClosed(SHELL_NEW_JAVA_PROJECT);
    }

    private boolean exists(String name) throws RemoteException {
        if (treeItem == null) {
            return tree.getTextOfItems().contains(name);
        } else
            return treeItem.getTextOfItems().contains(name);
        // return false;
    }

}
