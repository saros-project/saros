package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInternal extends Remote {

  /**
   * Returns the file size in bytes
   *
   * @param projectName the name of the project
   * @param path the path of the folder e.g my/foo/bar
   * @return The length, in bytes, or 0L if the file does not exist. Some operating systems may
   *     return 0L for pathnames denoting system-dependent entities such as devices or pipes.
   * @throws RemoteException
   */
  public long getFileSize(String projectName, String path) throws RemoteException;

  /**
   * Checks if the given resource exists in the current workspace.
   *
   * @param path the path to the resource e.g my/foo/bar
   * @return <code>true</code> if the resource exists, <code>false</code> otherwise
   * @throws RemoteException
   */
  public boolean existsResource(String path) throws RemoteException;

  /**
   * Creates a folder in the project. All missing folders will be created automatically. @Note the
   * project must already exists
   *
   * @param projectName the name of the project
   * @param path the path of the folder e.g my/foo/bar
   * @throws RemoteException if the folder could not be created or the project do not exists
   */
  public void createFolder(String projectName, String path) throws RemoteException;

  /**
   * Creates a project
   *
   * @param projectName the name of the project
   * @throws RemoteException if the project could not be created or already exists
   */
  public void createProject(String projectName) throws RemoteException;

  /**
   * Changes the encoding of a project
   *
   * @param projectName the name of the project
   * @param charset the charset to use for this project
   * @throws RemoteException if the project does not exists or the charset is not available on the
   *     current remote platform
   */
  public void changeProjectEncoding(String projectName, String charset) throws RemoteException;

  /**
   * Changes the encoding of a file. @Note the project must already exists
   *
   * @param projectName the project the file belongs to
   * @param path the relative path of the file e.g my/foo/bar/hello.java
   * @param charset the charset to use for this file
   * @throws RemoteException if the new encoding could not be applied
   */
  public void changeFileEncoding(String projectName, String path, String charset)
      throws RemoteException;

  /**
   * Creates a Java project
   *
   * @param projectName the name of the project
   * @throws RemoteException if the project could not be created or already exists
   */
  public void createJavaProject(String projectName) throws RemoteException;

  /**
   * Clears the current workspace by deleting all projects
   *
   * @return <code>true</code> if all projects were successfully deleted, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public boolean clearWorkspace() throws RemoteException;

  /**
   * Changes the current Saros version to the given version
   *
   * @param version the version that Saros will be set to e.g <code>2.6.2.11</code>
   * @throws RemoteException
   */
  public void changeSarosVersion(String version) throws RemoteException;

  /**
   * Resets the current Saros version to its default state as the plugin was started
   *
   * @throws RemoteException
   */
  public void resetSarosVersion() throws RemoteException;

  /**
   * Creates a file in the given project. All missing folders will be created automatically. @Note
   * the project must already exists
   *
   * @param projectName the project where the file should be created
   * @param path the relative path of the file e.g my/foo/bar/hello.java
   * @param content the content of the file
   * @throws RemoteException if the file could not be created or already exists
   */
  public void createFile(String projectName, String path, String content) throws RemoteException;

  /**
   * Creates a file in the given project. All missing folders will be created automatically. @Note
   * the project must already exists
   *
   * @param projectName the project where the file should be created
   * @param path the relative path of the file e.g my/foo/bar/hello.java
   * @param size the size of the file
   * @param compressAble if <code>true</code> the content of the file will compress into not more
   *     than a several bytes
   * @throws RemoteException if the file could not be created or already exists
   */
  public void createFile(String projectName, String path, int size, boolean compressAble)
      throws RemoteException;

  /**
   * Creates a java class in the given project. All missing packages will be created
   * automatically. @Note the project must already exists
   *
   * @param projectName the name project where the java class should be created
   * @param packageName the name of the package e.g my.foo.bar
   * @param className the name of the class e.g HelloWorld
   * @throws RemoteException if the class could not be created or already exists
   */
  public void createJavaClass(String projectName, String packageName, String className)
      throws RemoteException;

  /**
   * Appends content to the give file
   *
   * @param projectName the name project where the java class should be created
   * @param path the relative path of the file e.g my/foo/bar/hello.java
   * @param content the content to append
   * @throws RemoteException if the file does not exists
   */
  public void append(String projectName, String path, String content) throws RemoteException;

  /**
   * Gets the content from the given file
   *
   * @param projectName the name project where the java class should be created
   * @param path the relative path of the file e.g my/foo/bar/hello.java
   * @return the content of this file as byte array
   * @throws RemoteException if the file does not exists
   */
  public byte[] getFileContent(String projectName, String path) throws RemoteException;
}
