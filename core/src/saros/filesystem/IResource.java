package saros.filesystem;

import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 *
 * <p>Represents an element (normally a file or folder) in the (virtual) file system.
 */
public interface IResource {

  /** The different types of resources. */
  enum Type {
    FILE,
    FOLDER,
    PROJECT,
    ROOT
  }

  public boolean exists();

  public IPath getFullPath();

  public String getName();

  public IContainer getParent();

  public IProject getProject();

  public IPath getProjectRelativePath();

  public Type getType();

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
  public boolean isIgnored();

  /**
   * Deletes this resource from the disk.
   *
   * <p>If the resource does not exist, this method does nothing.
   *
   * @throws IOException if the resource deletion failed
   */
  public void delete() throws IOException;

  public IPath getLocation();
}
