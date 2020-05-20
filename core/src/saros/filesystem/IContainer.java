package saros.filesystem;

import java.io.IOException;

/**
 * Represents a handle for a container in the (virtual) file system. Containers are defined as
 * elements in the filesystem containing other resources.
 *
 * <p>The referenced container do not necessarily have to exist in the local filesystem.
 */
public interface IContainer extends IResource {

  /**
   * Returns whether the resource with the given path relative to this container exists.
   *
   * @param relativePath the relative path for the resource to check
   * @return whether the resource with the given path relative to this container exists
   * @throws NullPointerException if the given path is <code>null</code>
   */
  boolean exists(IPath relativePath);

  /**
   * Returns all resources contained in the container.
   *
   * @return all resources contained in the container
   * @throws IOException if the container does not exist or the contained resources could not be
   *     read
   */
  IResource[] members() throws IOException;

  /**
   * Returns a handle for the file with the given relative path to this resource.
   *
   * @param pathString a string representation of the path relative to this resource
   * @return a handle for the file with the given relative path to this resource
   * @throws NullPointerException if the given string is <code>null</code>
   * @throws IllegalArgumentException if the given string represents an absolute or an empty path
   */
  IFile getFile(String pathString);

  /**
   * Returns a handle for the file with the given relative path to this resource.
   *
   * @param path a path relative to this resource
   * @return a handle for the file with the given relative path to this resource
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute or empty
   */
  IFile getFile(IPath path);

  /**
   * Returns a handle for the folder with the given relative path to this resource.
   *
   * <p>Returns this container if the given string represents an empty path.
   *
   * @param pathString a string representation of the path relative to this resource
   * @return a handle for the folder with the given relative path to this resource
   * @throws NullPointerException if the given string is <code>null</code>
   * @throws IllegalArgumentException if the given string represents an absolute path
   */
  IFolder getFolder(String pathString);

  /**
   * Returns a handle for the folder with the given relative path to this resource.
   *
   * <p>Returns this container if the given path is empty.
   *
   * @param path a path relative to this resource
   * @return a handle for the folder with the given relative path to this resource
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute
   */
  IFolder getFolder(IPath path);
}
