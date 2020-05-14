package saros.filesystem;

import java.io.IOException;

/**
 * Represents a handle for an element (normally a file or folder) in the (virtual) file system.
 *
 * <p>The referenced resources do not necessarily have to exist in the local filesystem.
 */
public interface IResource {

  /** The different types of resources. */
  enum Type {
    FILE,
    FOLDER,
    PROJECT,
    ROOT
  }

  /**
   * Returns whether the resource exists in the local filesystem.
   *
   * @return whether the resource exists in the local filesystem
   */
  boolean exists();

  /**
   * Returns the name of the resource.
   *
   * @return the name of the resource
   */
  String getName();

  /**
   * Returns the parent for the resource.
   *
   * <p>Returns <code>null</code> if the resource is a project.
   *
   * @return the parent for the resource or <code>null</code> if the resource is a project
   */
  IContainer getParent();

  /**
   * Returns the project for the resource.
   *
   * @return the project for the resource
   */
  IProject getProject();

  /**
   * Returns the project-relative path for the resource.
   *
   * <p>Returns an empty path if the resource is a project.
   *
   * @return the project-relative path for the resource or an empty path if the resource is a
   *     project
   */
  IPath getProjectRelativePath();

  /**
   * Returns the type of the resource.
   *
   * @return the type of the resource
   * @see Type
   */
  Type getType();

  /**
   * Returns whether the resource should be ignored. Resources should be ignored if they match any
   * of the following characteristics:
   *
   * <ul>
   *   <li>Their direct manipulation can cause unwanted side effects to the filesystem, IDE, or IDE
   *       configuration.
   *   <li>They are not of interest to the IDE. (This mostly concerns things like build artifacts.)
   * </ul>
   *
   * @return whether the resource should be ignored
   */
  boolean isIgnored();

  /**
   * Deletes this resource from the disk.
   *
   * <p>If the resource does not exist, this method does nothing.
   *
   * @throws IOException if the resource deletion failed
   */
  void delete() throws IOException;
}
