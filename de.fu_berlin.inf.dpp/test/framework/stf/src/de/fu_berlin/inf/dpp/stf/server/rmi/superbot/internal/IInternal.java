package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInternal extends Remote {

    /**
     * Creates a folder in the project. All missing folders will be created
     * automatically.
     * 
     * @Note the project must already exists
     * @param projectName
     *            the name of the project
     * @param path
     *            the path of the folder e.g my/foo/bar
     * @throws RemoteException
     */

    public void createFolder(String projectName, String path)
        throws RemoteException;

    /**
     * Creates a project
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */

    public void createProject(String projectName) throws RemoteException;

    /**
     * Creates a Java project
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void createJavaProject(String projectName) throws RemoteException;

    /**
     * Clears the current workspace by deleting all projects
     * 
     * @return <code>true</code> if all projects were successfully deleted,
     *         <code>false</code> otherwise
     * @throws RemoteException
     */

    public boolean clearWorkspace() throws RemoteException;

    /**
     * Changes the current Saros version to the given version
     * 
     * @param version
     *            the version that Saros will be set to e.g
     *            <code>2.6.2.11</code>
     * @throws RemoteException
     */
    public void changeSarosVersion(String version) throws RemoteException;

    /**
     * Resets the current Saros version to its default state as the plugin was
     * started
     * 
     * @throws RemoteException
     */
    public void resetSarosVersion() throws RemoteException;

    /**
     * Creates a file in the given project. All missing folders will be created
     * automatically.
     * 
     * @Note the project must already exists
     * @param projectName
     *            the project where the file should be created
     * 
     * @param path
     *            the relative path of the file e.g my/foo/bar/hello.java
     * @param content
     *            the content of the file
     * @throws RemoteException
     * 
     */

    public void createFile(String projectName, String path, String content)
        throws RemoteException;

    /**
     * Creates a file in the given project. All missing folders will be created
     * automatically.
     * 
     * @Note the project must already exists
     * @param projectName
     *            the project where the file should be created
     * 
     * @param path
     *            the relative path of the file e.g my/foo/bar/hello.java
     * @param size
     *            the size of the file
     * @param compressAble
     *            if <code>true</code> the content of the file will compress
     *            into not more than a several bytes
     * @throws RemoteException
     */
    public void createFile(String projectName, String path, int size,
        boolean compressAble) throws RemoteException;

    /**
     * Creates a java class in the given project
     * 
     * @Note the project must already exists
     * @param projectName
     *            the name project where the java class should be created
     * 
     * @param packageName
     *            the name of the package e.g my.foo.bar
     * @param className
     *            the name of the class e.g HelloWorld
     * @throws RemoteException
     */

    public void createJavaClass(String projectName, String packageName,
        String className) throws RemoteException;

    /**
     * Appends content to the give file
     * 
     * @param projectName
     *            the name project where the java class should be created
     * @param path
     *            the relative path of the file e.g my/foo/bar/hello.java
     * @param content
     *            the content to append
     * @throws RemoteException
     */
    public void append(String projectName, String path, String content)
        throws RemoteException;
}
