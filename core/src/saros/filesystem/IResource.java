package saros.filesystem;

import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 *
 * <p>Represents an element (normally a file or folder) in the (virtual) file system.
 *
 * <p><b>Note:</b> Instances of this class should <b>NOT</b> be casted using the Java type cast
 * operator. Instead use the {@link #adaptTo(Class)} method.
 *
 * <p>Example:
 *
 * <pre>
 *     IResource resource = getAResource();
 *
 *     if (resource == IResource.File) {
 *         IFile file = resource.adaptTo(IFile.class) // you may assume this will never return null
 *
 *         // do stuff
 *     }
 * </pre>
 */
public interface IResource {

  public static final int NONE = 0;
  public static final int FILE = 1;
  public static final int FOLDER = 2;
  public static final int PROJECT = 4;
  public static final int ROOT = 8;

  public boolean exists();

  public IPath getFullPath();

  public String getName();

  public IContainer getParent();

  public IProject getProject();

  public IPath getProjectRelativePath();

  public int getType();

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

  public void delete() throws IOException;

  public IPath getLocation();

  public <T extends IResource> T adaptTo(Class<T> clazz);
}
