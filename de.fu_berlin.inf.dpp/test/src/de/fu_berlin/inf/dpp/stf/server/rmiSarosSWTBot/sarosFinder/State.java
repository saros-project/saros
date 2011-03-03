package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;

public interface State extends Remote {

    /**
     * 
     * @param prjectName
     *            the name of the project
     * @return <tt>true</tt>, if the given project is under SVN control
     * @throws RemoteException
     */
    public boolean isProjectManagedBySVN(String prjectName)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @return the revision id of the given resource.
     * @throws RemoteException
     */
    public String getRevision(String fullPath) throws RemoteException;

    /**
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * 
     * @return the VCS specific URL information for the given resource specified
     *         by the passed parameter"fullPath".
     * @throws RemoteException
     */
    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException;

    /**
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the acount specified by the given jid is active
     * @throws RemoteException
     * @see XMPPAccount#isActive()
     */
    public boolean isAccountActiveNoGUI(JID jid) throws RemoteException;

    /**
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the account specified by the given jid and
     *         password exists in preference store
     * @throws RemoteException
     * @see XMPPAccountStore#getAllAccounts()
     */
    public boolean isAccountExistNoGUI(JID jid, String password)
        throws RemoteException;

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
    // public boolean existsFile(String viewTitle, String... fileNodes)
    // throws RemoteException;

    /**
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     * @return <tt>true</tt>, if the given folder already exists.
     */
    // public boolean existsFolderNoGUI(String... folderNodes)
    // throws RemoteException;

    /**
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @return <tt>true</tt>, if the given project exists
     */
    // public boolean existsProjectNoGUI(String projectName)
    // throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @return <tt>true</tt>, if the specified package already exists.
     * @throws RemoteException
     */
    // public boolean existsPkgNoGUI(String projectName, String pkg)
    // throws RemoteException;

    /**
     * @param filePath
     *            path of the file, e.g. "Foo_Saros/myFolder/myFile.xml" or path
     *            of a class file, e.g. "Foo_Saros/src/my/pkg/myClass.java
     * @return <tt>true</tt>, if the file specified by the passed parameter
     *         "filePath" exists.
     */
    // public boolean existsFileNoGUI(String filePath) throws RemoteException;

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
    // public boolean existsFileNoGUI(String... fileNodes) throws
    // RemoteException;

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
    // public boolean existsClassNoGUI(String projectName, String pkg,
    // String className) throws RemoteException;

}
