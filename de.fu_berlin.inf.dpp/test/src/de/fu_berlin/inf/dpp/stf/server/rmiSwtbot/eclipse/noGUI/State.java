package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;

public interface State extends Remote {
    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link EditorComponent#waitUntilEditorContentSame(String, String...)},
     * which compare the contents which may be dirty.
     * </p>
     * 
     * @param otherFileContent
     *            the file content of another peer, with which you want to
     *            compare your file content.
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     */
    public void waitUntilFileContentSame(String otherFileContent,
        String... fileNodes) throws RemoteException;

    /**
     * 
     * @param otherClassContent
     *            the class content of another peer, with which you want to
     *            compare your class content.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * 
     * @throws RemoteException
     * @see State#waitUntilFileContentSame(String, String...)
     */
    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException;

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @return only the saved content of the specified file, if it is dirty.
     *         This method is different from
     *         {@link EditorComponent#getTextOfEditor(String...)}, which can
     *         return a not saved content.
     * @throws RemoteException
     * @throws IOException
     * @throws CoreException
     */
    public String getFileContent(String... fileNodes) throws RemoteException,
        IOException, CoreException;

    /**
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * @return only the saved content of the specified class file, if it is
     *         dirty. This method is different from
     *         {@link EditorComponent#getTextOfJavaEditor(String, String, String)}
     *         , which can return a not saved content.
     * @throws RemoteException
     * @throws IOException
     * @throws CoreException
     */
    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException;

    /**
     * Delete all the projects in this workspace.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjects() throws RemoteException;

}
