package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class NewC extends Component implements INewC {

    private static transient NewC self;

    private IRemoteBotTreeItem treeItem;
    private IRemoteBotTree tree;

    /**
     * {@link NewC} is a singleton, but inheritance is possible.
     */
    public static NewC getInstance() {
        if (self != null)
            return self;
        self = new NewC();
        return self;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTree(IRemoteBotTree tree) {
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
                treeItem.contextMenus(MENU_NEW, MENU_FOLDER).click();
                confirmShellNewFolder(folderName);
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void pkg(String projectName, String pkg) throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            // if (!sarosBot().state().existsPkgNoGUI(projectName, pkg))
            try {
                precondition();
                remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_PACKAGE).click();
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
                treeItem.contextMenus(MENU_NEW, MENU_FILE).click();
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
                treeItem.contextMenus(MENU_NEW, MENU_CLASS).click();
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
            remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();
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
            remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();
            IRemoteBotShell shell_new = remoteBot().shell(SHELL_NEW_JAVA_CLASS);
            shell_new.activate();

            shell_new.bot().textWithLabel(LABEL_NAME).setText(className);
            shell_new.bot().button("Add...").click();
            remoteBot().waitUntilShellIsOpen("Implemented Interfaces Selection");
            IRemoteBotShell shell = remoteBot().shell("Implemented Interfaces Selection");
            shell.activate();
            shell.bot().textWithLabel("Choose interfaces:")
                .setText("java.lang.Runnable");
            shell.bot().table().waitUntilTableHasRows(1);
            shell.bot().button(OK).click();
            remoteBot().shell(SHELL_NEW_JAVA_CLASS).activate();
            shell.bot().checkBox("Inherited abstract methods").click();
            shell.bot().button(FINISH).click();
            remoteBot().waitUntilShellIsClosed(SHELL_NEW_JAVA_CLASS);
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
        remoteBot().activateWorkbench();
    }

    private void confirmShellNewJavaClass(String className)
        throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        shell.bot().textWithLabel(LABEL_NAME).setText(className);
        shell.bot().button(FINISH).click();
        remoteBot().waitUntilShellIsClosed(SHELL_NEW_JAVA_CLASS);
    }

    private void confirmShellNewJavaClass(String projectName, String pkg,
        String className) throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        shell.bot().textWithLabel(LABEL_SOURCE_FOLDER)
            .setText(projectName + "/" + SRC);
        shell.bot().textWithLabel(LABEL_PACKAGE).setText(pkg);
        shell.bot().textWithLabel(LABEL_NAME).setText(className);
        shell.bot().button(FINISH).click();
        remoteBot().waitUntilShellIsClosed(SHELL_NEW_JAVA_CLASS);
    }

    private void confirmWizardNewProject(String projectName)
        throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_PROJECT);
        shell.confirmWithTree(NEXT, NODE_GENERAL, NODE_PROJECT);
        shell.bot().textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        shell.bot().button(FINISH).click();
        remoteBot().waitUntilShellIsClosed(SHELL_NEW_PROJECT);
        // bot.sleep(50);
    }

    private void confirmShellNewFile(String fileName) throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_FILE);
        shell.activate();
        shell.bot().textWithLabel(LABEL_FILE_NAME).setText(fileName);
        shell.bot().button(FINISH).waitUntilIsEnabled();
        shell.bot().button(FINISH).click();
        remoteBot().waitUntilShellIsClosed(SHELL_NEW_FILE);
    }

    private void confirmShellNewJavaPackage(String projectName, String pkg)
        throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_JAVA_PACKAGE);
        shell.activate();
        shell.bot().textWithLabel(LABEL_SOURCE_FOLDER)
            .setText((projectName + "/" + SRC));
        shell.bot().textWithLabel(LABEL_NAME).setText(pkg);
        shell.bot().button(FINISH).click();
        if (remoteBot().isShellOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT))
            remoteBot().waitUntilShellIsClosed(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
    }

    private void confirmShellNewFolder(String folderName)
        throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_FOLDER);
        shell.activate();

        shell.bot().textWithLabel(LABEL_FOLDER_NAME).setText(folderName);

        shell.bot().button(FINISH).click();
        remoteBot().waitUntilShellIsClosed(SHELL_NEW_FOLDER);
    }

    private void confirmShellNewJavaProject(String projectName)
        throws RemoteException {
        IRemoteBotShell shell = remoteBot().shell(SHELL_NEW_JAVA_PROJECT);
        shell.activate();
        shell.bot().textWithLabel(LABEL_PROJECT_NAME).setText(projectName);

        // bot.button(FINISH).click();
        shell.bot().button(FINISH).click();
        remoteBot().waitUntilShellIsClosed(SHELL_NEW_JAVA_PROJECT);
    }

    private boolean exists(String name) throws RemoteException {
        if (treeItem == null) {
            return tree.getTextOfItems().contains(name);
        } else
            return treeItem.getTextOfItems().contains(name);
        // return false;
    }

}
