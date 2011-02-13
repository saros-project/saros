package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface FileM extends EclipseComponent {
    /**
     * Performs the action "create a project" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>if the project already exist, return</li>
     * <li>click context menu: New -> Project</li>
     * <li>confirm the pop-up window "New Project"</li>
     * <li>wait until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Project..." on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * 
     */
    public void newProject(String projectName) throws RemoteException;

    /**
     * Performs the action "create a java project" which should be done with the
     * following steps:
     * <ol>
     * <li>if the java project already exist, return.</li>
     * <li>click context menu: New -> Java Project</li>
     * <li>confirm the pop-up window "New Java Project"</li>
     * <li>wait until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Java Project" on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     */
    public void newJavaProject(String projectName) throws RemoteException;

    /**
     * Performs the action "create a new folder" which should be done with the
     * following steps:
     * <ol>
     * <li>If the folder already exists, return</li>
     * <li>Click sub menu: New -> Folder</li>
     * <li>Confirm pop-up window "New Folder"</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Folder" on the package Explorer view.</li>
     * </ol>
     * 
     * @param newFolderName
     *            the name of the new created folder which should be located in
     *            the parent folder specified by the parentNodes array
     *            parameter. e.g. "myFolder"
     * @param parentNodes
     *            node path to expand. Attempts to expand all parent nodes along
     *            the path specified by the parent node array parameter.e.g.
     *            {"Foo-saros","parentFolder" }
     */
    public void newFolder(String... folderNodes) throws RemoteException;

    /**
     * Performs the action "create a new package" which should be done with the
     * following steps:
     * <ol>
     * <li>if the package already exist, return</li>
     * <li>Click sub menu: New -> Package</li>
     * <li>Confirm pop-up window "New Java Package"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Package" on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * 
     */
    public void newPackage(String projectName, String pkg)
        throws RemoteException;

    /**
     * Performs the action "create a new file" which should be done with the
     * following steps:
     * <ol>
     * <li>if the package already exist, return</li>
     * <li>Click sub menu: New -> File</li>
     * <li>Confirm pop-up window "New File"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "File" on the package Explorer view.</li>
     * </ol>
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder", "myFile.xml"}
     * 
     */
    public void newFile(String... fileNodes) throws RemoteException;

    /**
     * Performs the action "create a new class" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>if the class already exist, return</li>
     * <li>Click sub menu: New -> Class</li>
     * <li>Confirm pop-up window "New Java Class"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Class" on the package Explorer view.</li>
     * </ol>
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

    /**
     * Performs the action "create a new class that implements runnable" which
     * should be done with the following steps:
     * 
     * <ol>
     * <li>if the class already exist, return</li>
     * <li>Click sub menu: New -> Class</li>
     * <li>Confirm pop-up window "New Java Class"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Class" + extra actions on the package Explorer view.
     * </li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * 
     */
    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Create a java project and a class in the project. The two functions
     * newJavaProject and newClass are often used, so i put them together to
     * simplify the junit-tests.
     * 
     * Attention: after creating a project bot need to sleep a moment until he
     * is allowed to create class. so if you want to create a project with a
     * class, please use this method, otherwise you should get
     * WidgetNotfoundException.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     */
    public void newJavaProjectWithClasses(String projectName, String pkg,
        String... className) throws RemoteException;

}
