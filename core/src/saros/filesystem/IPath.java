package saros.filesystem;

import java.io.File;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
// TODO replace with java.nio.file.Path; see #797
public interface IPath {

  IPath append(IPath path);

  String segment(int index);

  String lastSegment();

  boolean isPrefixOf(IPath path);

  int segmentCount();

  IPath removeFirstSegments(int count);

  IPath removeLastSegments(int count);

  String[] segments();

  IPath append(String path);

  IPath makeAbsolute();

  boolean isAbsolute();

  String toPortableString();

  String toOSString();

  File toFile();
}
