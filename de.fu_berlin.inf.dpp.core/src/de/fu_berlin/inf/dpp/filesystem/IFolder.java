package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

public interface IFolder extends IResource {

  /**
   * Returns whether a resource with the given path exists relative to this folder.
   *
   * @param path Relative path from this folder to resource.
   * @return true, if a resource exists, otherwise false.
   */
  public boolean exists(IPath path);

  /**
   * Returns a list of existing member resources within this folder.
   *
   * @return An array with existing member resources
   * @throws IOException
   */
  public IResource[] members() throws IOException;

  /**
   * Returns a list of existing member resources within this folder.
   *
   * @return An array with existing member resources
   * @throws IOException
   */
  public IResource[] members(int memberFlags) throws IOException;

  /**
   * Returns the default charset for resources in this folder.
   *
   * @return the default charset
   * @throws IOException
   */
  public String getDefaultCharset() throws IOException;

  /**
   * Creates a new folder resource.
   *
   * @param updateFlags
   * @param local
   * @throws IOException
   */
  public void create(int updateFlags, boolean local) throws IOException;

  /**
   * Creates a new folder resource.
   *
   * @param force
   * @param local
   * @throws IOException
   */
  public void create(boolean force, boolean local) throws IOException;

  /**
   * Finds and returns the member resource given by the path in this folder
   *
   * @param path Relative path from this folder to resource.
   * @return the member resource, or null if no such resource exists.
   */
  public IResource findMember(IPath path);

  /**
   * Returns a handle to the file with given name.
   *
   * @param name Name of the file
   * @return the handle of the file
   */
  public IFile getFile(String name);

  /**
   * Returns a handle to the file identified by the given path.
   *
   * @param path Relative path from this folder to resource.
   * @return the handle of the file
   */
  public IFile getFile(IPath path);

  /**
   * Returns a handle to the folder with given name.
   *
   * @param name Name of the folder
   * @return the handle of the folder
   */
  public IFolder getFolder(String name);

  /**
   * Returns a handle to the folder identified by the given path
   *
   * @param path Relative path from this folder to resource.
   * @return the handle of the folder
   */
  public IFolder getFolder(IPath path);
}
