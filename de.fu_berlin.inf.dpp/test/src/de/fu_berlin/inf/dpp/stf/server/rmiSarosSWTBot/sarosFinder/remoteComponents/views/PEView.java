package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.ContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenC;

/**
 * This interface contains only APIs to select treeItems in the package explorer
 * view e.g.
 * 
 * <pre>
 * alice.pEV.selectProject(...)
 * </pre>
 * 
 * </li>
 * 
 * @author lchen
 */
public interface PEView extends EclipseComponent {

    public ContextMenuWrapper tree() throws RemoteException;

    public ContextMenuWrapper selectJavaProject(String projectName)
        throws RemoteException;

    /**
     * select the given project.
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public ContextMenuWrapper selectProject(String projectName)
        throws RemoteException;

    /**
     * select the given package
     * 
     * @param projectName
     *            the name of the project, e.g.foo_bar
     * @param pkg
     *            the name of the package, e.g. my.pkg
     * @throws RemoteException
     */
    public ContextMenuWrapper selectPkg(String projectName, String pkg)
        throws RemoteException;

    /**
     * select the given class
     * 
     * @param projectName
     *            the name of the project, e.g.foo_bar
     * @param pkg
     *            the name of the package, e.g. my.pkg
     * @param className
     *            the name of the class, e.g. myClass
     * @throws RemoteException
     * 
     */
    public ContextMenuWrapper selectClass(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * select the given folder
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","myFolder"}
     * @throws RemoteException
     */
    public ContextMenuWrapper selectFolder(String... folderNodes)
        throws RemoteException;

    /**
     * select the given file
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","myFolder", "myFile.xml"}
     * @throws RemoteException
     */
    public ContextMenuWrapper selectFile(String... fileNodes)
        throws RemoteException;

    public OpenC open() throws RemoteException;

    // public SarosC saros() throws RemoteException;
    //
    // public TeamC team() throws RemoteException;

    public String getTitle() throws RemoteException;
}
