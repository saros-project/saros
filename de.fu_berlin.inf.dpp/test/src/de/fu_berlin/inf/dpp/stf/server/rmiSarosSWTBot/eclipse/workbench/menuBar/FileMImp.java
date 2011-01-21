package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class FileMImp extends EclipsePart implements FileM {

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

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    private static final String NEW = "New";
    private static final String PROJECT = "Project...";
    private static final String FOLDER = "Folder";
    private static final String FILE = "File";
    private static final String CLASS = "Class";
    private static final String PACKAGE = "Package";
    private static final String JAVA_PROJECT = "Java Project";
    private final static String SHELL_NEW_FOLDER = "New Folder";
    private final static String SHELL_NEW_FILE = "New File";
    private final static String SHELL_NEW_JAVA_PACKAGE = "New Java Package";
    private final static String SHELL_NEW_JAVA_CLASS = "New Java Class";
    protected final static String VIEWNAME = "Package Explorer";
    private static final String SHELL_NEW_PROJECT = "New Project";
    /* categories and nodes of the shell "New Project" */
    private static final String CATEGORY_GENERAL = "General";
    private static final String NODE_PROJECT = "Project";
    private static final String LABEL_PROJECT_NAME = "Project name:";
    private static final String LABEL_FILE_NAME = "File name:";
    private static final String LABEL_FOLDER_NAME = "Folder name:";
    private static final String SHELL_NEW_JAVA_PROJECT = "New Java Project";

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/
    public void newProject(String projectName) throws RemoteException {
        if (!existsProject(projectName)) {
            workbenchC.activateEclipseShell();
            menuW.clickMenuWithTexts(FILE, NEW, PROJECT);
            confirmWizardNewProject(projectName);
        }
    }

    public void newJavaProject(String projectName) throws RemoteException {
        if (!existsProject(projectName)) {
            workbenchC.activateEclipseShell();
            menuW.clickMenuWithTexts(FILE, NEW, JAVA_PROJECT);
            confirmWindowNewJavaProject(projectName);
        }
    }

    public boolean existsProject(String projectName) throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        return project.exists();
    }

    public void newFolder(String newFolderName, String... parentNodes)
        throws RemoteException {
        precondition();
        String[] folderNodes = new String[parentNodes.length];
        for (int i = 0; i < parentNodes.length; i++) {
            folderNodes[i] = parentNodes[i];
        }
        folderNodes[folderNodes.length - 1] = newFolderName;
        if (!existsFolder(folderNodes)) {
            try {
                treeW.getTreeItemInView(VIEWNAME, parentNodes);
                menuW.clickMenuWithTexts(FILE, NEW, FOLDER);
                confirmWindowNewFolder(newFolderName);
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public boolean existsFolder(String... folderNodes) throws RemoteException {
        IPath path = new Path(getPath(folderNodes));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    public void waitUntilFolderExisted(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void newPackage(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            if (!existsPkg(projectName, pkg))
                try {
                    workbenchC.activateEclipseShell();
                    menuW.clickMenuWithTexts(FILE, NEW, PACKAGE);
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

    public boolean existsPkg(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(getPkgPath(projectName, pkg));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource != null)
            return true;
        return false;
    }

    public void waitUntilPkgExisted(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            waitUntil(SarosConditions.isResourceExist(getPkgPath(projectName,
                pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            waitUntil(SarosConditions.isResourceNotExist(getPkgPath(
                projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void newFile(String... fileNodes) throws RemoteException {
        if (!existsFile(getPath(fileNodes)))
            try {
                precondition();
                String[] parentNodes = new String[fileNodes.length - 1];
                String newFileName = "";
                for (int i = 0; i < fileNodes.length; i++) {
                    if (i == fileNodes.length - 1)
                        newFileName = fileNodes[i];
                    else
                        parentNodes[i] = fileNodes[i];
                }
                treeW.getTreeItemInView(VIEWNAME, parentNodes);
                menuW.clickMenuWithTexts(FILE, NEW, FILE);
                confirmWindowNewFile(newFileName);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new file.";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public boolean existsFile(String filePath) throws RemoteException {
        IPath path = new Path(filePath);
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    public boolean existsFile(String... nodes) throws RemoteException {
        return existsFile(getPath(nodes));
    }

    public boolean existsClass(String projectName, String pkg, String className)
        throws RemoteException {
        return existsFile(getClassPath(projectName, pkg, className));
    }

    public boolean existsFiletWithGUI(String... nodes) throws RemoteException {
        workbenchC.activateEclipseShell();
        precondition();
        SWTBotTree tree = treeW.getTreeInView(VIEWNAME);
        return treeW.existsTreeItemWithRegexs(tree, nodes);
    }

    public void waitUntilFileExisted(String... fileNodes)
        throws RemoteException {
        String fullPath = getPath(fileNodes);
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void newClass(String projectName, String pkg, String className)
        throws RemoteException {
        if (!existsFile(getClassPath(projectName, pkg, className))) {
            try {
                workbenchC.activateEclipseShell();
                menuW.clickMenuWithTexts(FILE, NEW, CLASS);
                confirmWindowNewJavaClass(projectName, pkg, className);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new Java Class";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void waitUntilClassExisted(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException {
        if (!existsFile(getClassPath(projectName, pkg, className))) {
            precondition();
            menuW.clickMenuWithTexts(FILE, NEW, CLASS);
        }
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        bot.textWithLabel("Source folder:").setText(projectName + "/src");
        bot.textWithLabel("Package:").setText(pkg);
        bot.textWithLabel("Name:").setText(className);
        bot.button("Add...").click();
        shellC.waitUntilShellActive("Implemented Interfaces Selection");
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

    public void newJavaProjectWithClass(String projectName, String pkg,
        String className) throws RemoteException {
        newJavaProject(projectName);
        newClass(projectName, pkg, className);
    }

    protected void precondition() throws RemoteException {
        pEV.openPEView();
        pEV.setFocusOnPEView();
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
        shellC.confirmShellWithTree(SHELL_NEW_PROJECT, NEXT, CATEGORY_GENERAL,
            NODE_PROJECT);
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SHELL_NEW_PROJECT);
        bot.sleep(50);
    }

    private void confirmWindowNewFile(String newFileName)
        throws RemoteException {
        SWTBotShell shell = bot.shell(SHELL_NEW_FILE);
        shell.activate();
        bot.textWithLabel(LABEL_FILE_NAME).setText(newFileName);
        buttonW.waitUntilButtonEnabled(FINISH);
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private void confirmWindowNewJavaPackage(String projectName, String pkg)
        throws RemoteException {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_PACKAGE);
        shell.activate();
        bot.textWithLabel("Source folder:").setText((projectName + "/src"));
        bot.textWithLabel("Name:").setText(pkg);
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SHELL_NEW_JAVA_PACKAGE);
    }

    private void confirmWindowNewFolder(String newFolderName) {
        SWTBotShell shell = bot.shell(SHELL_NEW_FOLDER);
        shell.activate();
        bot.textWithLabel(LABEL_FOLDER_NAME).setText(newFolderName);
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private void confirmWindowNewJavaProject(String projectName)
        throws RemoteException {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_PROJECT);
        shell.activate();
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SHELL_NEW_JAVA_PROJECT);
    }

}
