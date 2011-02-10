package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface contains common APIs, which are often used by other
 * components. All components which inherit this interface can use these APIs.
 * For example,
 * <ol>
 * <li>
 * After creating new file with alice.file.newFile(...) you can check if the
 * file is already created with assertTrue(alice.file.existsFile(...)).</li>
 * <li>After deleting a file with alice.edit.deleteFile(...), you can also use
 * the method to check if the file is already deleted with
 * assertFalse(alice.edit.existsFile(...))</li>
 * 
 * 
 * 
 * @author lchen
 */
public interface EclipseComponent extends Remote {

    /**********************************************
     * 
     * No GUI
     * 
     **********************************************/
    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","myFolder", "myFile.xml"}
     * @return <tt>true</tt>, if the file specified by the node array parameter
     *         exists
     * @throws RemoteException
     */
    public boolean existsFile(String viewTitle, String... fileNodes)
        throws RemoteException;

    /**
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     * @return <tt>true</tt>, if the given folder already exists.
     */
    public boolean existsFolderNoGUI(String... folderNodes)
        throws RemoteException;

    /**
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @return <tt>true</tt>, if the given project exists
     */
    public boolean existsProjectNoGUI(String projectName)
        throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @return <tt>true</tt>, if the specified package already exists.
     * @throws RemoteException
     */
    public boolean existsPkgNoGUI(String projectName, String pkg)
        throws RemoteException;

    /**
     * @param filePath
     *            path of the file, e.g. "Foo_Saros/myFolder/myFile.xml" or path
     *            of a class file, e.g. "Foo_Saros/src/my/pkg/myClass.java
     * @return <tt>true</tt>, if the file specified by the passed parameter
     *         "filePath" exists.
     */
    public boolean existsFileNoGUI(String filePath) throws RemoteException;

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder", "myFile.xml"}
     * @return<tt>true</tt>, if the file specified by the passed array parameter
     *                       exists.
     * @throws RemoteException
     */
    public boolean existsFileNoGUI(String... fileNodes) throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @return <tt>true</tt>, if the class file specified by the passed
     *         parameters exists.
     * @throws RemoteException
     */
    public boolean existsClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException;

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    /**
     * Wait until the specified folder exists
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException;

    /**
     * wait until the given package exists
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException;

    /**
     * Wait until the given package not exists. This method would be used, if
     * you want to check if a shared package exists or not which is deleted by
     * another session_participant.
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException;

    /**
     * 
     * Wait until the specified class exists. This method would be used, if you
     * want to check if a shared class exists or not which is created by another
     * session_participant.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Wait until the specified class not exists.This method would be used, if
     * you want to check if a shared class exists or not which is deleted by
     * another session_participant.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Wait until the file exists.
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g. {"Foo-saros",
     *            "myFolder", "myFile.xml"}
     */
    public void waitUntilFileExists(String... fileNodes) throws RemoteException;

}
