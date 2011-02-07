package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EclipseComponent extends Remote {

    /**
     * @return <tt>true</tt>, if the given folder already exists.
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public boolean existsFolderNoGUI(String... folderNodes)
        throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @return <tt>true</tt>, if the given project is exist
     */
    public boolean existsProjectNoGUI(String projectName)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the specified package already exists.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public boolean existsPkgNoGUI(String projectName, String pkg)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the file specified by the passed parameter
     *         "filePath" exists.
     * @param filePath
     *            path of the file, e.g. "Foo_Saros/myFolder/myFile.xml" or path
     *            of a class file, e.g. "Foo_Saros/src/my/pkg/myClass.java
     * 
     */
    public boolean existsFileNoGUI(String filePath) throws RemoteException;

    /**
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder", "myFile.xml"}
     * @return<tt>true</tt>, if the file specified by the passed array parameter
     *                       exists.
     * @throws RemoteException
     */
    public boolean existsFileNoGUI(String... nodes) throws RemoteException;

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

    /**
     * waits until the specified folder is exist
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void waitUntilFolderExisted(String... folderNodes)
        throws RemoteException;

    /**
     * wait until the given package is exist
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgExisted(String projectName, String pkg)
        throws RemoteException;

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException;

    /**
     * 
     * waits until the specified class is exist
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassExisted(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * waits until the specified class isn't exist
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g. {"Foo-saros",
     *            "myFolder", "myFile.xml"}
     */
    public void waitUntilFileExisted(String... fileNodes)
        throws RemoteException;

}
