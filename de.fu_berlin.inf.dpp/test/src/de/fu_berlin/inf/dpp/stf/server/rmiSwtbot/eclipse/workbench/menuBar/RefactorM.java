package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RefactorM extends Remote {

    public void moveClassTo(String sourceProject, String sourcePkg,
        String className, String targetProject, String targetPkg)
        throws RemoteException;

    /**
     * Perform the action "rename package" which should be done with the
     * following steps:
     * <ol>
     * <li>Select the given package with the given node path and click
     * "Refactor" > "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename package" is closed. It guarantee that
     * the "rename package" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given package.
     * 
     * @throws RemoteException
     */
    public void renamePkg(String newName, String projectName, String pkg)
        throws RemoteException;

    /**
     * Perform the action "rename folder" which should be done with the
     * following steps:
     * <ol>
     * <li>Select the given folder with the given node path and click "Refactor"
     * > "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename resource" is closed. It guarantee that
     * the "rename folder" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given folder.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "myFolder"}
     * @throws RemoteException
     */
    public void renameFolder(String newName, String... nodes)
        throws RemoteException;

    /**
     * Perform the action "rename file" which should be done with the following
     * steps:
     * <ol>
     * <li>Select the given file with the given node path and click "Refactor" >
     * "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename Compilation Unit" is closed. It
     * guarantee that the "rename file" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given file.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "myFolder", "myFile.xml"}
     * @throws RemoteException
     */
    public void renameFile(String newName, String... nodes)
        throws RemoteException;

    /**
     * Perform the action "rename class" which should be done with the following
     * steps:
     * <ol>
     * <li>Select the given class with the given node path and click "Refactor"
     * > "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename Compilation Unit" is closed. It
     * guarantee that the "rename file" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given class.
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException;

    public void renameJavaProject(String newName, String... nodes)
        throws RemoteException;

}
