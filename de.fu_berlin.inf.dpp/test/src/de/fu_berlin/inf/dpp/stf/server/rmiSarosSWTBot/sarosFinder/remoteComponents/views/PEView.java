package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

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

    /**
     * select the given project.
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public STFBotTreeItem selectProject(String projectName)
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
    public void selectPkg(String projectName, String pkg)
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
    public STFBotTreeItem selectClass(String projectName, String pkg,
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
    public void selectFolder(String... folderNodes) throws RemoteException;

    /**
     * select the given file
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","myFolder", "myFile.xml"}
     * @throws RemoteException
     */
    public STFBotTreeItem selectFile(String... fileNodes)
        throws RemoteException;

}
