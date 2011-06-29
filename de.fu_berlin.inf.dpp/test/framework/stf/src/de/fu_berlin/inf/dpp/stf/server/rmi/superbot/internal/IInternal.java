package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInternal extends Remote {

    /**
     * Creates a folder in the project
     * 
     * @param project
     *            the name of the project
     * @param path
     *            the path of the folder e.g my/foo/bar
     * @throws RemoteException
     */

    public void createFolder(String project, String path)
        throws RemoteException;

    /**
     * Creates a project
     * 
     * @param project
     *            the name of the project
     * @throws RemoteException
     */

    public void createProject(String project) throws RemoteException;

    /**
     * Creates a Java project
     * 
     * @param project
     *            the name of the project
     * @throws RemoteException
     */
    public void createJavaProject(String project) throws RemoteException;

    /**
     * Deletes the current workspace
     * 
     * @return <code>true</code> if all projects were successfully deleted,
     *         <code>false</code> otherwise
     * @throws RemoteException
     */

    public boolean deleteWorkspace() throws RemoteException;

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
     * Creates a file in the given project
     * 
     * @param project
     *            the project where the file should be created
     * 
     * @param path
     *            the relative path of the file e.g my/foo/bar/hello.java
     * @param content
     *            the content of the file
     */

    public void createFile(String project, String path, String content)
        throws RemoteException;

    /**
     * Creates a file in the given project
     * 
     * @param project
     *            the project where the file should be created
     * 
     * @param path
     *            the relative path of the file e.g my/foo/bar/hello.java
     * @param size
     *            the size of the file
     * @param compressAble
     *            if <code>true</code> the content of the file will compress
     *            into not more than a several bytes
     */
    public void createFile(String project, String path, int size,
        boolean compressAble) throws RemoteException;

}
