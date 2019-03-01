package saros.negotiation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import saros.negotiation.FileList.MetaData;

/**
 * A diff between two {@link FileList file lists}.
 *
 * @see #diff(FileList, FileList, boolean)
 */
public class FileListDiff {

  private FileListDiff() {
    // Empty but non-public. Only FileListDiff#diff should create
    // FileListDiffs.
  }

  private final List<String> addedFiles = new ArrayList<String>();

  private final List<String> removedFiles = new ArrayList<String>();

  private final List<String> alteredFiles = new ArrayList<String>();

  private final List<String> unalteredFiles = new ArrayList<String>();

  private final List<String> addedFolders = new ArrayList<String>();

  private final List<String> removedFolders = new ArrayList<String>();

  private final List<String> unalteredFolders = new ArrayList<String>();

  /**
   * Returns a new {@link FileListDiff diff} which contains the difference of the two {@link
   * FileList}s.
   *
   * <p>The <code>diff</code> describes the operations needed to transform <code>base</code> into
   * <code>target</code>. For example, the result's {@link #getAddedFolders()} returns the list of
   * folders that are present in <code>target</code>, but not in <code>base</code>.
   *
   * <p>If either of the two parameters is <code>null</code>, the result is an empty diff.
   *
   * @param base The base {@link FileList}.
   * @param target The {@link FileList} to compare to.
   * @param excludeRemoved if <code>true</code> removed files and folders are not included in the
   *     result
   * @return a new {@link FileListDiff} which contains the difference information of the two {@link
   *     FileList}s.
   */
  public static FileListDiff diff(
      final FileList base, final FileList target, final boolean excludeRemoved) {

    FileListDiff result = new FileListDiff();

    if (base == null || target == null) return result;

    final List<String> basePaths = base.getPaths();
    final List<String> targetPaths = target.getPaths();

    /*
     * TODO find better values to avoid multiple rehashing for larger file
     * lists
     */
    final Set<String> baseFolders = new HashSet<String>(1024);
    final Set<String> baseFiles = new HashSet<String>(1024);

    final Set<String> targetFolders = new HashSet<String>(1024);
    final Set<String> targetFiles = new HashSet<String>(1024);

    for (final String path : basePaths) {

      addAllFolders(baseFolders, path);

      if (!isFolder(path)) baseFiles.add(path);
    }

    for (final String path : targetPaths) {

      addAllFolders(targetFolders, path);

      if (!isFolder(path)) targetFiles.add(path);
    }

    final Set<String> complementSet =
        new HashSet<String>(
            max(baseFolders.size(), baseFiles.size(), targetFolders.size(), targetFiles.size()));

    complementSet.addAll(targetFiles);
    complementSet.removeAll(baseFiles);

    result.addedFiles.addAll(complementSet);

    if (!excludeRemoved) {
      complementSet.clear();
      complementSet.addAll(baseFiles);
      complementSet.removeAll(targetFiles);

      result.removedFiles.addAll(complementSet);
    }

    final Set<String> intersectionSet =
        new HashSet<String>(max(targetFiles.size(), targetFolders.size()));

    intersectionSet.addAll(targetFiles);
    intersectionSet.retainAll(baseFiles);

    for (final String path : intersectionSet) {

      final MetaData baseData = base.getMetaData(path);
      final MetaData targetData = target.getMetaData(path);

      if ((baseData == null && targetData == null)
          || (baseData != null && targetData != null)
              && (baseData.checksum == targetData.checksum)) {
        result.unalteredFiles.add(path);
      } else {
        result.alteredFiles.add(path);
      }
    }

    complementSet.clear();
    complementSet.addAll(targetFolders);
    complementSet.removeAll(baseFolders);

    result.addedFolders.addAll(complementSet);

    if (!excludeRemoved) {
      complementSet.clear();
      complementSet.addAll(baseFolders);
      complementSet.removeAll(targetFolders);

      result.removedFolders.addAll(complementSet);
    }

    intersectionSet.clear();
    intersectionSet.addAll(targetFolders);
    intersectionSet.retainAll(baseFolders);

    result.unalteredFolders.addAll(intersectionSet);

    return result;
  }

  /**
   * Returns an unmodifiable list containing the files that must be added to <code>base</code> to
   * match <code>target</code>, i.e the files do not exist.
   *
   * @return an unmodifiable list containing the files that must be added.
   */
  public List<String> getAddedFiles() {
    return Collections.unmodifiableList(addedFiles);
  }

  /**
   * Returns an unmodifiable list containing the files that must be removed from <code>base</code>
   * to match <code>target</code>.
   *
   * @return an unmodifiable list containing the files that must be removed.
   */
  public List<String> getRemovedFiles() {
    return Collections.unmodifiableList(removedFiles);
  }

  /**
   * Returns an unmodifiable list containing the files that must be changed, i.e their contents
   * differ.
   *
   * @return an unmodifiable list containing the files that that must be changed.
   */
  public List<String> getAlteredFiles() {
    return Collections.unmodifiableList(alteredFiles);
  }

  /**
   * Returns an unmodifiable list containing the files that do not need to be changed.
   *
   * @return an unmodifiable list containing the files that do not need to be changed.
   */
  public List<String> getUnalteredFiles() {
    return Collections.unmodifiableList(unalteredFiles);
  }

  /**
   * Returns an unmodifiable list containing the folders that must be added to <code>base</code> to
   * match <code>target</code>.
   *
   * @return an unmodifiable list containing the folders that must be added.
   */
  public List<String> getAddedFolders() {
    return Collections.unmodifiableList(addedFolders);
  }

  /**
   * Returns an unmodifiable list containing the folders that must be deleted from <code>base</code>
   * to match <code>target</code>.
   *
   * @return an unmodifiable list containing the removed folders that must be removed.
   */
  public List<String> getRemovedFolders() {
    return Collections.unmodifiableList(removedFolders);
  }

  /**
   * Returns an unmodifiable list containing the folders that do not need to be changed.
   *
   * @return an unmodifiable list containing the folders that do not need to be changed.
   */
  public List<String> getUnalteredFolders() {
    return Collections.unmodifiableList(unalteredFolders);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + addedFiles.hashCode();
    result = prime * result + addedFolders.hashCode();
    result = prime * result + alteredFiles.hashCode();
    result = prime * result + removedFiles.hashCode();
    result = prime * result + removedFolders.hashCode();
    result = prime * result + unalteredFiles.hashCode();
    result = prime * result + unalteredFolders.hashCode();

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    FileListDiff other = (FileListDiff) obj;

    return addedFiles.equals(other.addedFiles)
        && addedFolders.equals(other.addedFolders)
        && alteredFiles.equals(other.alteredFiles)
        && removedFiles.equals(other.removedFiles)
        && removedFolders.equals(other.removedFolders)
        && unalteredFiles.equals(other.unalteredFiles)
        && unalteredFolders.equals(other.unalteredFolders);
  }

  @Override
  public String toString() {
    return "FileListDiff [addedFiles="
        + addedFiles
        + ", removedFiles="
        + removedFiles
        + ", alteredFiles="
        + alteredFiles
        + ", unalteredFiles="
        + unalteredFiles
        + ", addedFolders="
        + addedFolders
        + ", removedFolders="
        + removedFolders
        + ", unalteredFolders="
        + unalteredFolders
        + "]";
  }

  private static boolean isFolder(final String path) {
    return path.charAt(path.length() - 1) == FileList.DIR_SEPARATOR_CHAR;
  }

  private static void addAllFolders(final Set<String> into, final String path) {

    int idx = path.length() - 1;

    if (!isFolder(path)) idx = path.lastIndexOf(FileList.DIR_SEPARATOR_CHAR, idx - 1);

    String subPath;

    while (idx >= 0) {

      subPath = path.substring(0, idx + 1);

      if (into.contains(subPath)) break;

      into.add(subPath);

      idx = subPath.lastIndexOf(FileList.DIR_SEPARATOR_CHAR, idx - 1);
    }
  }

  private static int max(int a, int... others) {
    int max = a;

    for (int i = 0; i < others.length; i++) max = Math.max(max, others[i]);

    return max;
  }
}
