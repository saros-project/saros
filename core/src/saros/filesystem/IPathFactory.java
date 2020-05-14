package saros.filesystem;

/**
 * An interface for implementing a factory that is able to convert {@link saros.filesystem.IPath
 * path} objects to their string representation and vice versa. Implementations must throw the given
 * {@link RuntimeException runtime exceptions} as declared in the method signatures.
 */
// TODO remove with IPath; see #797
public interface IPathFactory {

  /**
   * Converts a path to its string representation.
   *
   * @param path the path to convert
   * @return the string representation of the path
   * @throws NullPointerException if path is <code>null</code>
   * @throws IllegalArgumentException if the path is not relative (e.g it presents a full path like
   *     <code>/etc/init.d/</code>)
   */
  String fromPath(IPath path);

  /**
   * Converts a string representation of a path to an <code>IPath</code> object.
   *
   * @param pathString the string path to convert
   * @return an <code>IPath</code> object representing the path of the given string
   * @throws NullPointerException if name is <code>null</code>
   * @throws IllegalArgumentException if the resulting path object is not a relative path
   */
  IPath fromString(String pathString);
}
