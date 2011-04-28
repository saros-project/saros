package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.eclipseViews;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.IContextMenusInPEView;

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
public interface IPEView extends Remote {

    public IContextMenusInPEView tree() throws RemoteException;

    public IContextMenusInPEView selectSrc(String projectName)
        throws RemoteException;

    public IContextMenusInPEView selectJavaProject(String projectName)
        throws RemoteException;

    /**
     * select the given project.
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public IContextMenusInPEView selectProject(String projectName)
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
    public IContextMenusInPEView selectPkg(String projectName, String pkg)
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
    public IContextMenusInPEView selectClass(String projectName, String pkg,
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
    public IContextMenusInPEView selectFolder(String... folderNodes)
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
    public IContextMenusInPEView selectFile(String... fileNodes)
        throws RemoteException;

    public String getTitle() throws RemoteException;

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

    /**
     * waits until the window with the title "Saros running VCS operation" is
     * closed
     * 
     * @throws RemoteException
     */
    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException;

    /**
     * waits until the given project is in SVN control
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException;

    /**
     * waits until the given project is not under SVN control
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param revisionID
     *            the expected revision.
     * @throws RemoteException
     */
    public void waitUntilRevisionIsSame(String fullPath, String revisionID)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param url
     *            the expected URL of the remote resource, e.g.
     *            "http://myhost.com/svn/trunk/.../MyFirstTest01.java".
     * @throws RemoteException
     */
    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException;

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @return the saved content of the given file. This method is different
     *         from {@link IRemoteBotEditor#getText()} , which return the text of
     *         editor, which may be not saved.
     * @throws RemoteException
     * @throws IOException
     * @throws CoreException
     */
    public String getFileContent(String... fileNodes) throws RemoteException,
        IOException, CoreException;

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a little to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link IRemoteBotEditor#waitUntilIsTextSame(String)}, which compare only the
     * text of editor which may be dirty.
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
}
