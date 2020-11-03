package saros.filesystem;

import java.io.File;
import java.nio.file.Path;
import saros.util.PathUtils;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 *
 * @deprecated replace with usage of {@link Path} and {@link PathUtils}
 */
// TODO replace with java.nio.file.Path; see #797
@Deprecated
public interface IPath {

  /** @deprecated replace with {@link Path#resolve(Path)} */
  @Deprecated
  IPath append(IPath path);

  /** @deprecated replace with {@link Path#getName(int)} */
  @Deprecated
  String segment(int index);

  /** @deprecated replace with {@link Path#getFileName()} */
  @Deprecated
  String lastSegment();

  /** @deprecated replace with reverse call to {@link Path#startsWith(Path)} */
  @Deprecated
  boolean isPrefixOf(IPath path);

  /** @deprecated replace with {@link Path#getNameCount()} */
  @Deprecated
  int segmentCount();

  /**
   * @deprecated replace with {@link Path#subpath(int, int)} or {@link
   *     PathUtils#removeFirstSegments(Path, int)}
   */
  @Deprecated
  IPath removeFirstSegments(int count);

  /**
   * @deprecated replace with {@link Path#subpath(int, int)} or {@link
   *     PathUtils#removeLastSegments(Path, int)}
   */
  @Deprecated
  IPath removeLastSegments(int count);

  /** @deprecated remove; only used by path impl tests */
  @Deprecated
  String[] segments();

  /** @deprecated replace with {@link Path#resolve(String)} */
  @Deprecated
  IPath append(String path);

  /** @deprecated replace with {@link Path#toAbsolutePath()} */
  @Deprecated
  IPath makeAbsolute();

  /** @deprecated replace with {@link Path#isAbsolute()} */
  @Deprecated
  boolean isAbsolute();

  /** @deprecated replace with {@link PathUtils#toPortableString(Path)} */
  @Deprecated
  String toPortableString();

  /** @deprecated replace with {@link Path#toString()} */
  @Deprecated
  String toOSString();

  /** @deprecated replace with {@link Path#toFile()} */
  @Deprecated
  File toFile();
}
