package saros.stf.server.rmi.superbot.component.view.eclipse;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;

/**
 * This interface contains methods to select treeItems in the package explorer view
 *
 * @author lchen
 * @author Stefan Rossbach
 */
public interface IPackageExplorerView extends Remote {

  public IContextMenusInPEView tree() throws RemoteException;

  public IContextMenusInPEView selectSrc(String projectName) throws RemoteException;

  /**
   * Selects the given Java project.
   *
   * @param projectName the name of the Java project
   * @throws RemoteException
   * @throws WidgetNotFoundException if the Java project could not be found
   */
  public IContextMenusInPEView selectJavaProject(String projectName) throws RemoteException;

  /**
   * Selects the given Java project matching the regular expression.
   *
   * @param projectName the name of the Java project as regular expression
   * @throws RemoteException
   * @throws WidgetNotFoundException if the Java project could not be found
   */
  public IContextMenusInPEView selectJavaProjectWithRegex(String projectName)
      throws RemoteException;

  /**
   * Select the given project.
   *
   * @param projectName the name of the project
   * @throws RemoteException
   * @throws WidgetNotFoundException if the project could not be found
   */
  public IContextMenusInPEView selectProject(String projectName) throws RemoteException;

  /**
   * Select the given project matching the regular expression.
   *
   * @param projectName the name of the project as regular expression
   * @throws RemoteException
   * @throws WidgetNotFoundException if the project could not be found
   */
  public IContextMenusInPEView selectProjectWithRegex(String projectName) throws RemoteException;

  /**
   * Select the given package.
   *
   * @param projectName the name of the project, e.g.foo_bar
   * @param pkg the name of the package, e.g. my.pkg
   * @throws RemoteException
   * @throws WidgetNotFoundException if the project or package could not be found
   */
  public IContextMenusInPEView selectPkg(String projectName, String pkg) throws RemoteException;

  /**
   * Select the given package matching the regular expression.
   *
   * @param projectName the name of the project as regular expression
   * @param pkg the name of the package as regular expression
   * @throws RemoteException
   * @throws WidgetNotFoundException if the project or package could not be found
   */
  public IContextMenusInPEView selectPkgWithRegex(String projectName, String pkg)
      throws RemoteException;

  /**
   * Select the given class
   *
   * @param projectName the name of the project, e.g.foo_bar
   * @param pkg the name of the package, e.g. my.pkg
   * @param className the name of the class, e.g. myClass
   * @throws RemoteException
   * @throws WidgetNotFoundException if the project, package or class could not be found
   */
  public IContextMenusInPEView selectClass(String projectName, String pkg, String className)
      throws RemoteException;

  /**
   * Select the given class matching the regular expression.
   *
   * @param projectName the name of the project as regular expression
   * @param pkg the name of the package as regular expression
   * @param className the name of the class as regular expression
   * @throws RemoteException
   * @throws WidgetNotFoundException if the project, package or class could not be found
   */
  public IContextMenusInPEView selectClassWithRegex(
      String projectName, String pkg, String className) throws RemoteException;

  /**
   * Selects the given folder.
   *
   * @param projectName the name of the project
   * @param folderNodes node path to expand. Attempts to expand all nodes along the path specified
   *     by the node array parameter.e.g. {"myFolder", "foo", "bar"}
   * @throws RemoteException
   * @throws WidgetNotFoundException if the folder could not be found
   */
  public IContextMenusInPEView selectFolder(String projectName, String... folderNodes)
      throws RemoteException;

  /**
   * Select the given files
   *
   * @param projectName the name of the project
   * @param fileNodes node path to expand. Attempts to expand all nodes along the path specified by
   *     the node array parameter.e.g. {"myFolder", "myFile.xml"}
   * @throws RemoteException
   */
  public IContextMenusInPEView selectFile(String projectName, String... fileNodes)
      throws RemoteException;

  public String getTitle() throws RemoteException;

  /**
   * Tests if the given resource is shared in the current session
   *
   * @param path the full path of the local resource, e.g.
   *     "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
   * @return <code>true</code> if the resource is currently shared, <code>false</code> otherwise
   * @throws RemoteException
   */
  public boolean isResourceShared(String path) throws RemoteException;

  /**
   * Wait until the specified folder exists
   *
   * @param folderNodes node path to expand. Attempts to expand all nodes along the path specified
   *     by the node array parameter.e.g. {"Foo-saros","parentFolder" ,"myFolder"}
   */
  public void waitUntilFolderExists(String... folderNodes) throws RemoteException;

  /**
   * Wait until the specified folder does not exist
   *
   * @param folderNodes node path to expand. Attempts to expand all nodes along the path specified
   *     by the node array parameter.e.g. {"Foo-saros","parentFolder" ,"myFolder"}
   */
  public void waitUntilFolderNotExists(String... folderNodes) throws RemoteException;

  /**
   * wait until the given package exists
   *
   * @param projectName name of the java project, e.g. Foo-Saros.
   * @param pkg name of the package, e.g. my.pkg.
   * @throws RemoteException
   */
  public void waitUntilPkgExists(String projectName, String pkg) throws RemoteException;

  /**
   * Wait until the given package not exists. This method would be used, if you want to check if a
   * shared package exists or not which is deleted by another session_participant.
   *
   * @param projectName name of the java project, e.g. Foo-Saros.
   * @param pkg name of the package, e.g. my.pkg.
   * @throws RemoteException
   */
  public void waitUntilPkgNotExists(String projectName, String pkg) throws RemoteException;

  /**
   * Wait until the specified class exists. This method would be used, if you want to check if a
   * shared class exists or not which is created by another session_participant.
   *
   * @param projectName name of the project, e.g. Foo_Saros.
   * @param pkg name of the package, e.g. my.pkg.
   * @param className name of the class, e.g. myClass.
   * @throws RemoteException
   */
  public void waitUntilClassExists(String projectName, String pkg, String className)
      throws RemoteException;

  /**
   * Wait until the specified class not exists.This method would be used, if you want to check if a
   * shared class exists or not which is deleted by another session_participant.
   *
   * @param projectName name of the project, e.g. Foo_Saros.
   * @param pkg name of the package, e.g. my.pkg.
   * @param className name of the class, e.g. myClass.
   * @throws RemoteException
   */
  public void waitUntilClassNotExists(String projectName, String pkg, String className)
      throws RemoteException;

  /**
   * Wait until the file exists.
   *
   * @param fileNodes node path to expand. Attempts to expand all nodes along the path specified by
   *     the node array parameter.e.g. {"Foo-saros", "myFolder", "myFile.xml"}
   */
  public void waitUntilFileExists(String... fileNodes) throws RemoteException;

  /**
   * Wait until the file does not exist.
   *
   * @param fileNodes node path to expand. Attempts to expand all nodes along the path specified by
   *     the node array parameter.e.g. {"Foo-saros", "myFolder", "myFile.xml"}
   */
  public void waitUntilFileNotExists(String... fileNodes) throws RemoteException;

  /**
   * @param fileNodes node path to expand. Attempts to expand all nodes along the path specified by
   *     the node array parameter.e.g. {"Foo-saros","parentFolder" ,"myFolder"}.
   * @return the saved content of the given file. This method is different from {@link
   *     IRemoteBotEditor#getText()} , which return the text of editor, which may be not saved.
   * @throws RemoteException
   * @throws IOException
   * @throws CoreException
   */
  public String getFileContent(String... fileNodes)
      throws RemoteException, IOException, CoreException;

  /**
   * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of file, which is modified
   * by another peer (e.g. Alice). Because of data transfer delay Bob need to wait a little to see
   * the changes . So it will be a good idea that you give bob some time before you compare the two
   * files from Alice and Bob.
   *
   * <p><b>Note:</b> the mothod is different from {@link
   * IRemoteBotEditor#waitUntilIsTextSame(String)}, which compare only the text of editor which may
   * be dirty.
   *
   * @param otherFileContent the file content of another peer, with which you want to compare your
   *     file content.
   * @param fileNodes node path to expand. Attempts to expand all nodes along the path specified by
   *     the node array parameter.e.g. {"Foo-saros","parentFolder" ,"myFolder"}.
   */
  public void waitUntilFileContentSame(String otherFileContent, String... fileNodes)
      throws RemoteException;

  /**
   * Waits until the given resource is shared in the current session
   *
   * @param path the full path of the local resource, e.g.
   *     "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
   * @throws RemoteException
   */
  public void waitUntilResourceIsShared(String path) throws RemoteException;
}
