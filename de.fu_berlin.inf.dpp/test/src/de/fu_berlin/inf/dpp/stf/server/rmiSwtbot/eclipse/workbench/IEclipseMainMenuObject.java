package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IEclipseMainMenuObject extends Remote {

    public void preference() throws RemoteException;

    public void newTextFileLineDelimiter(String OS) throws RemoteException;

    public String getTextFileLineDelimiter() throws RemoteException;

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
        String className) throws RemoteException;

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
    public void newJavaProject(String projectName) throws RemoteException;

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
    public void newProject(String projectName) throws RemoteException;

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
        throws RemoteException;

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
    public void newFolder(String... folderPath) throws RemoteException;

    public void newFile(String... filePath) throws RemoteException;

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
        throws RemoteException;

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Open the view "Problems". The name of the method is defined the same as
     * the menu names. The name "showViewProblem" then means: hello guy, please
     * click main menus Window -> Show view -> Problems.
     * 
     */
    public void showViewProblems() throws RemoteException;

    /**
     * Open the view "Project Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewProblem" then means: hello guy,
     * please click main menus Window -> Show view -> Project Explorer.
     * 
     */
    public void showViewProjectExplorer() throws RemoteException;

    /**
     * Open the perspective "Java". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveJava" then means: hello guy,
     * please click main menus Window -> Open perspective -> Java.
     * 
     */
    public void openPerspectiveJava() throws RemoteException;

    /**
     * test, if the java perspective is active.
     */
    public boolean isJavaPerspectiveActive() throws RemoteException;

    /**
     * Open the perspective "Debug". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveDebug" then means: hello guy,
     * please click main menus Window -> Open perspective -> Debug.
     * 
     */
    public void openPerspectiveDebug() throws RemoteException;

    /**
     * test, if the debug perspective is active.
     */
    public boolean isDebugPerspectiveActive() throws RemoteException;
}
