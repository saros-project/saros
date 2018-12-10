/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A FileList is a list of resources -- files and folders -- which belong to the same project.
 * FileLists can be compared to other FileLists. Folders are denoted by a trailing separator.
 * Instances of this class are immutable. No further modification is allowed after creation.
 * Instances should be created using the methods provided by the {@link FileListFactory}.
 */

// FIXME remove the projectID stuff, as it is mutable !
@XStreamAlias("FILELIST")
public class FileList {

  /**
   * Separator used to divide path segments and mark directories in a file list by suffixing the
   * path entry with this value.
   */
  public static final String DIR_SEPARATOR = "/";

  /**
   * Separator char used to divide path segments and mark directories in a file list by suffixing
   * the path entry with this value.
   */
  public static final char DIR_SEPARATOR_CHAR = '/';

  /*
   * Do NOT optimize this code in regards to understandability. This class IS
   * optimized in regards to memory consumption, i.e serializing / marshaling
   * an instance of this class will consume as less memory as possible.
   *
   * This class only stores segments differences, i.e foo/bar/foo.txt, and
   * foo/bar/foobar.txt will be stored as foo, bar, foo.txt, and foobar.txt
   */
  @XStreamAlias("f")
  private static class File {

    @XStreamAlias("p")
    @XStreamAsAttribute
    String path;

    @XStreamAlias("m")
    MetaData metaData;

    @XStreamAlias("l")
    List<File> files;

    @XStreamAlias("d")
    @XStreamAsAttribute
    boolean isDirectory;

    private File(String path, MetaData metaData, boolean isDirectory) {
      this.path = path;
      this.metaData = metaData;
      this.files = new ArrayList<File>();
      this.isDirectory = isDirectory;
    }

    public static File createRoot() {
      return new File("", null, true);
    }

    /**
     * Helper: Adds this File's path to the given <code>base</code> with a "/" in between. There
     * will be no leading "/" and no doubled "/"s.
     */
    private String appendTo(String base) {
      if (base.isEmpty()) return path;

      if (base.endsWith(DIR_SEPARATOR)) return base + path;

      return base + DIR_SEPARATOR + path;
    }

    /** Helper: Cuts a path into its segments */
    private String[] segments(String path) {
      return path.split(DIR_SEPARATOR);
    }

    /**
     * Converts the content of this file node and its sub nodes to full paths.<br>
     * e.g:<br>
     *
     * <pre>
     * DIR   FILES
     * a/b/[a,b,c,d]
     *  -> [a/b/a, a/b/b, a/b/c, a/b/d]
     * </pre>
     *
     * @return the list containing the full paths
     */
    public List<String> toList() {
      List<String> paths = new ArrayList<String>();
      toList(path, paths);
      return paths;
    }

    /**
     * Will be called recursively to reach all leaves of this file node, and will put all entries
     * into the given list.
     *
     * @param base the path of the parent node
     * @param paths a list to store the paths
     */
    private void toList(String base, List<String> paths) {
      for (File sub : files) {
        if (sub.isDirectory && sub.files.isEmpty())
          paths.add(sub.appendTo(base).concat(DIR_SEPARATOR));
        else if (!sub.isDirectory) paths.add(sub.appendTo(base));

        sub.toList(sub.appendTo(base), paths);
      }
    }

    /** True, if the given path is one of this File's sub-nodes. */
    public boolean contains(String path) {
      return getFile(path) != null;
    }

    /**
     * Retrieves the meta data for the given path. Returns <code>null</code> if there is
     * corresponding file in this node.
     */
    public MetaData getMetaData(String path) {
      File file = getFile(path);
      return file == null ? null : file.metaData;
    }

    /** Retrieves the file for the given path, <code>null</code> if it does not exist. */
    private File getFile(String path) {
      for (File file : files) {
        File foundFile = file.getFile(segments(path), 0);
        if (foundFile != null) return foundFile;
      }

      return null;
    }

    /** Will be called recursively to find the file represented by the given path segments. */
    private File getFile(String[] segments, int segmentIndex) {
      if (segmentIndex >= segments.length) return null;

      // _________ 0 1 2 3 : segment.length = 4
      // _search : a/b/c/d : segmentIndex = 3
      // current : a/b/c/d : return this

      if (path.equals(segments[segmentIndex]) && segmentIndex + 1 == segments.length) {
        return this;
      }

      /*
       * only continue if we are still on a valid path segment e.g we
       * search for a/b/c/d but are now in a/b/a
       */
      if (!path.equals(segments[segmentIndex])) {
        return null;
      }

      for (File file : files) {
        File foundFile = file.getFile(segments, segmentIndex + 1);
        if (foundFile != null) return foundFile;
      }
      return null;
    }

    /**
     * Inserts a new path into this File structure. Missing intermediate folder nodes will be
     * created.
     *
     * @param path not <code>null</code>
     * @param metaData can be <code>null</code>
     */
    public void addPath(String path, MetaData metaData, boolean isDirectory) {
      addPath(segments(path), 0, metaData, isDirectory);
    }

    /**
     * Will be called recursively to create the entry for the path given by its segments including
     * all folder hierarchy levels.
     */
    private void addPath(
        String[] segments, int segmentIndex, MetaData metaData, boolean isDirectory) {

      if (segmentIndex >= segments.length) return;

      for (File file : files) {
        if (file.path.equals(segments[segmentIndex])) {
          if (segmentIndex + 1 == segments.length) {
            file.metaData = metaData;
            file.isDirectory = isDirectory;
            return;
          } else {
            file.addPath(segments, segmentIndex + 1, metaData, isDirectory);
            return;
          }
        }
      }

      if (segmentIndex + 1 == segments.length) {
        files.add(new File(segments[segmentIndex], metaData, isDirectory));
        return;
      }

      File file = new File(segments[segmentIndex], null, true);
      files.add(file);
      file.addPath(segments, segmentIndex + 1, metaData, isDirectory);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ObjectUtils.hashCode(files);
      result = prime * result + (isDirectory ? 1231 : 1237);
      result = prime * result + ObjectUtils.hashCode(metaData);
      result = prime * result + ObjectUtils.hashCode(path);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;

      File other = (File) obj;

      if (isDirectory != other.isDirectory) return false;

      if (!ObjectUtils.equals(path, other.path)) return false;
      if (!ObjectUtils.equals(metaData, other.metaData)) return false;
      if (!ObjectUtils.equals(files, other.files)) return false;

      return true;
    }
  }

  @XStreamAlias("md")
  static class MetaData {
    /** Checksum of this file. */
    @XStreamAlias("crc")
    long checksum;

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (o == null) return false;
      if (!(o instanceof MetaData)) return false;

      MetaData other = (MetaData) o;

      if (!ObjectUtils.equals(checksum, other.checksum)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return (int) checksum;
    }

    @Override
    public String toString() {
      return "[Checksum: 0x" + Long.toHexString(checksum).toUpperCase() + "]";
    }
  }

  /** ID of Project this list of files belong to */
  private String projectID;

  private Set<String> encodings = new HashSet<String>();

  private File root;

  MetaData getMetaData(String path) {
    return root.getMetaData(path);
  }

  /** Creates an empty file list. */
  FileList() {
    this.root = File.createRoot();
  }

  /**
   * Returns all encodings (e.g UTF-8, US-ASCII) that are used by the files contained in this file
   * list.
   *
   * @return used encodings which may be empty if the encodings are not known
   */
  public Set<String> getEncodings() {
    return new HashSet<String>(encodings);
  }

  void addEncoding(String charset) {
    if (charset == null) return;

    encodings.add(charset);
  }

  void addPath(String path) {
    root.addPath(path, null, false);
  }

  void addPath(String path, MetaData metaData, boolean isDirectory) {
    root.addPath(path, metaData, isDirectory);
  }

  boolean contains(String path) {
    return root.contains(path);
  }

  @XStreamOmitField private volatile List<String> cachedList = null;

  /**
   * Returns an immutable list of all paths in this FileList.
   *
   * <p>Example: In case the FileList looks like this:
   *
   * <pre>
   * / A
   *   / A1.java
   * / B
   *   / B2.java
   *   / B3.java
   * / C
   * </pre>
   *
   * then this method returns: <code>[A/A1.java, B/B2.java, B/B3.java, C/]</code>
   *
   * @return Returns only the leaves of the tree, i.e. folders are only included if they don't
   *     contain anything. The paths are sorted by their character length.
   */
  public List<String> getPaths() {

    if (cachedList != null) return cachedList;

    final List<String> inflated = Collections.unmodifiableList(root.toList());

    cachedList = inflated;
    return inflated;
  }

  public String getProjectID() {
    return projectID;
  }

  public void setProjectID(String projectID) {
    this.projectID = projectID;
  }

  @Override
  public int hashCode() {
    return root.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (!(o instanceof FileList)) return false;

    return root.equals(((FileList) o).root);
  }

  @Override
  public String toString() {
    List<String> paths = new ArrayList<String>(getPaths());
    Collections.sort(paths);

    return paths.toString();
  }
}
