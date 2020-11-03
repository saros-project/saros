package saros.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.log4j.Logger;

/** Utility class offering helpful methods to operate on paths. */
public class PathUtils {
  private static final Logger log = Logger.getLogger(PathUtils.class);

  /** The separator character used for portable paths. */
  public static final String PORTABLE_SEPARATOR = "/";

  /** The default separator character used on the Windows platform. */
  private static final String WINDOWS_SEPARATOR = "\\";

  /**
   * Returns whether the given path is empty.
   *
   * @param path the path to check
   * @return whether the given path is empty
   * @throws NullPointerException if the given path is <code>null</code>
   */
  public static boolean isEmpty(Path path) {
    Objects.requireNonNull(path, "The given path must not be null");

    Path normalized = path.normalize();

    return normalized.getNameCount() == 1 && normalized.toString().isEmpty();
  }

  /**
   * Removes the given number of segments from the start of the given path.
   *
   * <p>Returns an empty path if the number of segments to drop is larger or equal to the number of
   * segments in the given path.
   *
   * @param path the path whose segments to drop
   * @param count the number of segments to drop
   * @return the remaining path
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given count is negative
   * @see Path#subpath(int, int)
   */
  public static Path removeFirstSegments(Path path, int count) {
    Objects.requireNonNull(path, "The given path must not be null");

    if (count < 0) {
      throw new IllegalArgumentException("The given count must not be negative - count: " + count);
    }

    if (path.getNameCount() <= count) {
      return Paths.get("");
    }

    return path.subpath(count, path.getNameCount());
  }

  /**
   * Removes the given number of segments from the end of the given path.
   *
   * <p>Returns an empty path if the number of segments to drop is larger or equal to the number of
   * segments in the given path.
   *
   * @param path the path whose segments to drop
   * @param count the number of segments to drop
   * @return the remaining path
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given count is negative
   * @see Path#subpath(int, int)
   */
  public static Path removeLastSegments(Path path, int count) {
    Objects.requireNonNull(path, "The given path must not be null");

    if (count < 0) {
      throw new IllegalArgumentException("The given count must not be negative - count: " + count);
    }

    if (path.getNameCount() <= count) {
      return Paths.get("");
    }

    return path.subpath(0, path.getNameCount() - count);
  }

  /**
   * Returns a normalized version of the given path, not containing any redundant segments. The
   * method also checks that the normalized path does not contain any unresolved parent directory
   * references.
   *
   * @param path the path to normalized
   * @return a normalized version of the given path
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute or contains unresolvable parent
   *     directory references (i.e. starts with the segment '<code>..</code>' after normalization)
   * @see Path#normalize()
   */
  public static Path normalize(Path path) {
    Objects.requireNonNull(path, "The given path must not be null");

    if (path.isAbsolute()) {
      throw new IllegalArgumentException("The given path must not be absolute - path: " + path);
    }

    Path normalizedPath = path.normalize();

    String normalizedPathString = normalizedPath.toString();

    if (normalizedPathString.equals("..")
        || normalizedPathString.startsWith(".." + File.separator)) {

      throw new IllegalArgumentException(
          "The given path must not contain unresolvable parent directory references - path: "
              + path
              + " , normalized: "
              + normalizedPathString);
    }

    return normalizedPath;
  }

  /**
   * Returns a platform-independent string representation of the given path. The given path must not
   * be absolute.
   *
   * <p>The given path is normalized before it is made portable.
   *
   * <p>The string can be converted into a path for the local filesystem using {@link
   * PathUtils#fromPortableString(String)}.
   *
   * @param path the path to convert
   * @return a platform-independent string representation of the given path
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute or contains unresolvable parent
   *     directory references (i.e. starts with the segment '<code>..</code>' after normalization)
   * @see PathUtils#normalize(Path)
   * @see PathUtils#fromPortableString(String)
   */
  public static String toPortableString(Path path) {
    Objects.requireNonNull(path, "The given path must not be null");

    Path normalizedPath = normalize(path);

    String normalizedLocalPathString = normalizedPath.toString();

    if (File.separator.equals(PORTABLE_SEPARATOR)) {
      if (normalizedLocalPathString.contains(WINDOWS_SEPARATOR)) {
        log.warn(
            "The given local path contains occurrences of the Windows separator character '"
                + WINDOWS_SEPARATOR
                + "'. This character is not seen as a separator by the local filesystem, meaning "
                + " resolving the path on a Windows system will lead to a different path. path: "
                + normalizedLocalPathString);
      }

      return normalizedLocalPathString;
    }

    return normalizedLocalPathString.replace(File.separator, PORTABLE_SEPARATOR);
  }

  /**
   * Returns a path in the local filesystem for the given platform-independent path string. This
   * method is only intended to work with strings generated by {@link
   * PathUtils#toPortableString(Path)}.
   *
   * <p>The created path is normalized before it is returned.
   *
   * @param portablePathString the path string to convert
   * @return a path in the local filesystem for the given platform-independent path string
   * @throws NullPointerException if the given path is <code>null</code>
   * @throws IllegalArgumentException if the created path is absolute or contains unresolvable
   *     parent directory references (i.e. starts with the segment '<code>..</code>' after
   *     normalization)
   * @see PathUtils#toPortableString(Path)
   * @see PathUtils#normalize(Path)
   */
  public static Path fromPortableString(String portablePathString) {
    Objects.requireNonNull(portablePathString, "The given string must not be null");

    String localPathString;

    if (File.separator.equals(PORTABLE_SEPARATOR)) {
      localPathString = portablePathString;

    } else {
      if (portablePathString.contains(File.separator)) {
        log.warn(
            "The given portable path contains occurrences of the local separator character '"
                + File.separator
                + "'. This character was not seen as a separator on the system where the portable "
                + "path string was created, meaning resolving it locally will lead to a different "
                + "path. portable path string: "
                + portablePathString);
      }

      localPathString = portablePathString.replace(PORTABLE_SEPARATOR, File.separator);
    }

    Path localPath = Paths.get(localPathString);

    return normalize(localPath);
  }
}
