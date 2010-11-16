package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class EclipseMainMenuObjectImp extends EclipseObject implements
    EclipseMainMenuObject {

    public void preference() throws RemoteException {
        exWorkbenchO.activateEclipseShell();
        menuO.clickMenuWithTexts("Window", "Preferences");
    }

    public void newTextFileLineDelimiter(String OS) throws RemoteException {
        preference();
        SWTBotTree tree = bot.tree();
        tree.expandNode("General").select("Workspace");

        if (OS.equals("Default")) {
            bot.radioInGroup("Default", "New text file line delimiter").click();
        } else {
            bot.radioInGroup("Other:", "New text file line delimiter").click();
            bot.comboBoxInGroup("New text file line delimiter")
                .setSelection(OS);
        }
        bot.button("Apply").click();
        bot.button("OK").click();
        windowO.waitUntilShellClosed("Preferences");
    }

    public String getTextFileLineDelimiter() throws RemoteException {
        preference();
        SWTBotTree tree = bot.tree();
        tree.expandNode("General").select("Workspace");
        if (bot.radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            windowO.closeShell("Preferences");
            return "Default";
        } else if (bot.radioInGroup("Other:", "New text file line delimiter")
            .isSelected()) {
            SWTBotCombo combo = bot
                .comboBoxInGroup("New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            windowO.closeShell("Preferences");
            return itemName;
        }
        windowO.closeShell("Preferences");
        return "";
    }

    /**
     * Create a java project and a class in the project. The combination with
     * function newJavaProject and newClass is used very often, so i put them
     * together to simplify the junit-tests.
     * 
     * Attention: after creating a project bot need to sleep a moment until he
     * is allowed to create class. so if you want to create a project with a
     * class, please use this mothde, otherwise you should get
     * WidgetNotfoundException.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     */
    public void newJavaProjectWithClass(String projectName, String pkg,
        String className) throws RemoteException {
        newJavaProject(projectName);
        bot.sleep(50);
        newClass(projectName, pkg, className);
    }

    /**
     * Create a java project. The name of the method is defined the same as the
     * menu names. The name "newJavaProject" then means: hello guys, please
     * click main menus File -> New -> JavaProject.
     * 
     * 1. if the java project already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.
     * 
     * 3. click main menus File -> New -> JavaProject.
     * 
     * 4. confirm the pop-up window "New Java Project"
     * 
     * 5. bot wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * 
     */
    public void newJavaProject(String projectName) throws RemoteException {
        if (!exStateO.existsProject(projectName)) {
            exWorkbenchO.activateEclipseShell();
            bot.menu("File").menu("New").menu("Java Project").click();
            SWTBotShell shell = bot.shell("New Java Project");
            shell.activate();
            bot.textWithLabel("Project name:").setText(projectName);
            bot.button("Finish").click();
            bot.waitUntil(Conditions.shellCloses(shell));
            bot.sleep(50);
            // // TODO version without timeout
            // final String baseName = projectName + "_base";
            // if (!isJavaProjectExist(projectName)) {
            // activateEclipseShell();
            // try {
            // // New Java Project
            // clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            // SarosConstant.MENU_TITLE_NEW,
            // SarosConstant.MENU_TITLE_JAVA_PROJECT);
            // } catch (WidgetNotFoundException e) {
            // // New Project...
            // clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            // SarosConstant.MENU_TITLE_NEW,
            // SarosConstant.MENU_TITLE_PROJECT);
            // // Java Project
            // confirmWindowWithTreeWithFilterText(
            // SarosConstant.SHELL_TITLE_NEW_PROJECT,
            // SarosConstant.CATEGORY_JAVA,
            // SarosConstant.NODE_JAVA_PROJECT,
            // SarosConstant.BUTTON_NEXT);
            // }
            //
            // waitUntilShellActive("New Java Project");
            // final SWTBotShell newProjectDialog = bot.activeShell();
            //
            // setTextWithLabel("Project name:", projectName);
            // clickButton(SarosConstant.BUTTON_FINISH);
            // waitUntilShellCloses(newProjectDialog);
            //
            // if (isShellActive("Open Associated Perspective?")) {
            // clickButton(SarosConstant.BUTTON_YES);
            // waitUntilShellCloses("Open Associated Perspective?");
            // }
            // }
            // // final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
            // // .getRoot();
            // // IProject project = root.getProject(baseName);
            // // try {
            // // project.copy(new Path(projectName), true, null);
            // // root.refreshLocal(IResource.DEPTH_INFINITE, null);
            // // } catch (CoreException e) {
            // // log.debug("Couldn't copy project " + baseName, e);
            // // }
            // bot.sleep(100);
        }
    }

    /**
     * Create a project. The name of the method is defined the same as the menu
     * names. The name "newProject" then means: hello guys, please click main
     * menus File -> New -> Project.
     * 
     * 1. if the project already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.
     * 
     * 3. click main menus File -> New -> Project.
     * 
     * 4. confirm the pop-up window "New Project"
     * 
     * 5. bot wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * 
     */
    public void newProject(String projectName) throws RemoteException {
        if (!exStateO.existsProject(projectName)) {
            exWorkbenchO.activateEclipseShell();
            bot.menu("File").menu("New").menu("Project...").click();
            SWTBotShell shell = bot.shell("New Project");
            shell.activate();
            bot.tree().expandNode("General").select("Project");
            bot.button(SarosConstant.BUTTON_NEXT).click();
            bot.textWithLabel("Project name:").setText(projectName);
            bot.button("Finish").click();
            bot.waitUntil(Conditions.shellCloses(shell));
            bot.sleep(50);
        }
    }

    /**
     * Create a new package. The name of the method is defined the same as the
     * menu names. The name "newPackage" then means: hello guys, please click
     * main menus File -> New -> Package.
     * 
     * 1. if the package already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.
     * 
     * 3. click main menus File -> New -> Package.
     * 
     * 4. confirm the pop-up window "New Java Package"
     * 
     * 5. bot wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * 
     */
    public void newPackage(String projectName, String pkg)
        throws RemoteException {
        if (!exStateO.isPkgExist(projectName, pkg))
            try {
                exWorkbenchO.activateEclipseShell();
                menuO.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                    SarosConstant.MENU_TITLE_NEW, "Package");
                windowO.activateShellWithMatchText("New Java Package");
                bot.textWithLabel("Source folder:").setText(
                    (projectName + "/src"));
                bot.textWithLabel("Name:").setText(pkg);
                bot.button(SarosConstant.BUTTON_FINISH).click();
                windowO.waitUntilShellClosed("New java Package");
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new package";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    /**
     * Create a new folder. Via File -> New -> Folder.
     * 
     * 1. If the folder already exists, return.
     * 
     * 2. Activate saros instance window. If workbench isn't active, bot can't
     * find main menus.
     * 
     * 3. Click menu: File -> New -> Folder.
     * 
     * 4. Confirm pop-up window "New Folder".
     * 
     * @param folderPath
     *            the path of the new folder. e.g. {Foo_Saros, myFolder,
     *            subFolder}
     */
    public void newFolder(String... folderPath) throws RemoteException {
        if (!exStateO.isFolderExist(folderPath))
            try {
                String projectName = "";
                String folders = "";
                for (int i = 0; i < folderPath.length; i++) {
                    if (i == 0)
                        projectName = folderPath[i];
                    else
                        folders += folderPath[i] + "/";
                }
                exWorkbenchO.activateEclipseShell();
                bot.menu("File").menu("New").menu("Folder").click();
                SWTBotShell shell = bot.shell("New Folder");
                shell.activate();
                bot.textWithLabel("Enter or select the parent folder:")
                    .setText(projectName);
                bot.textWithLabel("Folder name:").setText(folders);
                bot.button("Finish").click();
                bot.waitUntil(Conditions.shellCloses(shell));
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public void newFile(String... filePath) throws RemoteException {
        if (!exStateO.isFileExist(filePath))
            try {
                String[] folders = new String[filePath.length - 1];
                String fileName = "";
                for (int i = 0; i < filePath.length; i++) {
                    if (i == filePath.length - 1)
                        fileName = filePath[i];
                    else
                        folders[i] = filePath[i];
                }
                exWorkbenchO.activateEclipseShell();
                bot.menu("File").menu("New").menu("File").click();
                SWTBotShell shell = bot.shell("New File");
                shell.activate();
                if (folders.length > 0)
                    bot.tree().expandNode(folders).select();
                bot.textWithLabel("File name:").setText(fileName);
                bot.button("Finish").click();
                bot.waitUntil(Conditions.shellCloses(shell));
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new file.";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    /**
     * Create a new package. The name of the method is defined the same as the
     * menu names. The name "newClass" then means: hello guys, please click main
     * menus File -> New -> Class.
     * 
     * 1. if the package already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.
     * 
     * 3. click main menus File -> New -> Class.
     * 
     * 4. confirm the pop-up window "New Java Class"
     * 
     * 5. bot wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * 
     */
    public void newClass(String projectName, String pkg, String className)
        throws RemoteException {
        if (!exStateO.existsClass(projectName, pkg, className))
            try {
                exWorkbenchO.activateEclipseShell();
                bot.menu("File").menu("New").menu("Class").click();
                SWTBotShell shell = bot.shell("New Java Class");
                shell.activate();
                bot.textWithLabel("Source folder:").setText(
                    projectName + "/src");
                bot.textWithLabel("Package:").setText(pkg);
                bot.textWithLabel("Name:").setText(className);
                bot.button("Finish").click();
                bot.waitUntil(Conditions.shellCloses(shell));
                // activateEclipseShell();
                // clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                // SarosConstant.MENU_TITLE_NEW,
                // SarosConstant.MENU_TITLE_CLASS);
                // waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_JAVA_CLASS);
                // activateShellWithMatchText(SarosConstant.SHELL_TITLE_NEW_JAVA_CLASS);
                // // FIXME WidgetNotFoundException in TestEditDuringInvitation
                // final SWTBotShell newClassDialog = bot.activeShell();
                // setTextWithLabel("Source folder:", projectName + "/src");
                // setTextWithLabel("Package:", pkg);
                // setTextWithLabel("Name:", className);
                // // implementsInterface("java.lang.Runnable");
                // // bot.checkBox("Inherited abstract methods").click();
                // clickCheckBox("Inherited abstract methods");
                // waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
                // clickButton(SarosConstant.BUTTON_FINISH);
                // waitUntilShellCloses(newClassDialog);
                // // openJavaFileWithEditor(projectName, pkg, className +
                // // ".java");
                // // bot.sleep(sleepTime);
                // // editor.navigateTo(2, 0);
                // // editor.quickfix("Add unimplemented methods");
                // // editor.save();
                // // bot.sleep(750);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new Java Class";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException {
        exWorkbenchO.activateEclipseShell();
        bot.menu("File").menu("New").menu("Class").click();
        SWTBotShell shell = bot.shell("New Java Class");
        shell.activate();
        bot.textWithLabel("Source folder:").setText(projectName + "/src");
        bot.textWithLabel("Package:").setText(pkg);
        bot.textWithLabel("Name:").setText(className);

        bot.button("Add...").click();
        windowO.waitUntilShellActive("Implemented Interfaces Selection");
        bot.shell("Implemented Interfaces Selection").activate();
        SWTBotText text = bot.textWithLabel("Choose interfaces:");
        bot.sleep(2000);
        text.setText("java.lang.Runnable");
        tableO.waitUntilTableHasRows(1);

        bot.button("OK").click();
        bot.shell("New Java Class").activate();

        bot.checkBox("Inherited abstract methods").click();
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    /**
     * Open the view "Problems". The name of the method is defined the same as
     * the menu names. The name "showViewProblem" then means: hello guy, please
     * click main menus Window -> Show view -> Problems.
     * 
     */
    public void showViewProblems() throws RemoteException {
        viewO.openViewWithName("Problems", "General", "Problems");
    }

    /**
     * Open the view "Project Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewProblem" then means: hello guy,
     * please click main menus Window -> Show view -> Project Explorer.
     * 
     */
    public void showViewProjectExplorer() throws RemoteException {
        viewO.openViewWithName("Project Explorer", "General",
            "Project Explorer");
    }

    /**
     * Open the perspective "Java". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveJava" then means: hello guy,
     * please click main menus Window -> Open perspective -> Java.
     * 
     */
    public void openPerspectiveJava() throws RemoteException {
        perspectiveO.openPerspectiveWithId(SarosConstant.ID_JAVA_PERSPECTIVE);
    }

    /**
     * test, if the java perspective is active.
     */
    public boolean isJavaPerspectiveActive() throws RemoteException {
        return perspectiveO
            .isPerspectiveActive(SarosConstant.ID_JAVA_PERSPECTIVE);
    }

    /**
     * Open the perspective "Debug". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveDebug" then means: hello guy,
     * please click main menus Window -> Open perspective -> Debug.
     * 
     */
    public void openPerspectiveDebug() throws RemoteException {
        perspectiveO.openPerspectiveWithId(SarosConstant.ID_DEBUG_PERSPECTIVE);
    }

    /**
     * test, if the debug perspective is active.
     */
    public boolean isDebugPerspectiveActive() throws RemoteException {
        return perspectiveO
            .isPerspectiveActive(SarosConstant.ID_DEBUG_PERSPECTIVE);
    }

}
