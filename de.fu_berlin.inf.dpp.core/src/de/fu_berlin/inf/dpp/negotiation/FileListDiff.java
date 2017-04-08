package de.fu_berlin.inf.dpp.negotiation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fu_berlin.inf.dpp.negotiation.FileList.MetaData;

/**
 * A diff between two {@link FileList}s.
 *
 * @see FileList#diff(FileList)
 *
 * @author ahaferburg
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

    // OLD

    private final List<String> added = new ArrayList<String>();

    private final List<String> removed = new ArrayList<String>();

    private final List<String> altered = new ArrayList<String>();

    private final List<String> unaltered = new ArrayList<String>();

    public static FileListDiff diff(final FileList base, final FileList target) {
        return diff(base, target, false);
    }

    /**
     * Returns a new {@link FileListDiff diff} which contains the difference of
     * the two {@link FileList}s.
     * <p>
     * The <code>diff</code> describes the operations needed to transform
     * <code>base</code> into <code>target</code>. For example, the result's
     * {@link #getAddedPaths()} returns the list of files and folders that are
     * present in <code>target</code>, but not in <code>base</code>.
     * <p>
     * If either of the two parameters is <code>null</code>, the result is an
     * empty diff.
     *
     * @param base
     *            The base {@link FileList}.
     * @param target
     *            The {@link FileList} to compare to.
     * @param excludeRemoved
     *            if <code>true</code> removed files and folders are not
     *            included in the result
     *
     * @return a new {@link FileListDiff} which contains the difference
     *         information of the two {@link FileList}s.
     */
    public static FileListDiff diff(final FileList base, final FileList target,
        final boolean excludeRemoved) {
        FileListDiff result = new FileListDiff();

        if (base == null || target == null)
            return result;

        computeDiff(result, base, target, excludeRemoved);

        /*
         * we have to copy the set because we should not work on references when
         * deleting
         */
        Set<String> baseEntries = new HashSet<String>(base.getPaths());
        Set<String> targetEntries = new HashSet<String>(target.getPaths());

        /* determine the paths that don't match the target to delete them */
        baseEntries.removeAll(targetEntries);
        result.removed.addAll(baseEntries);

        /* determine the paths that are not already present in base set */
        baseEntries = new HashSet<String>(base.getPaths());
        targetEntries.removeAll(baseEntries);
        result.added.addAll(targetEntries);

        /* determine for all matching paths if files are altered */
        targetEntries = new HashSet<String>(target.getPaths());
        baseEntries.retainAll(targetEntries);

        for (String path : baseEntries) {
            /* folders cannot be altered */
            if (path.endsWith(FileList.DIR_SEPARATOR)) {
                result.unaltered.add(path);
                continue;
            }

            MetaData baseData = base.getMetaData(path);
            MetaData targetData = target.getMetaData(path);

            if ((baseData == null && targetData == null)
                || (baseData != null && targetData != null)
                && (baseData.checksum == targetData.checksum)) {
                result.unaltered.add(path);
            } else {
                result.altered.add(path);
            }
        }

        return result;
    }

    private static void computeDiff(final FileListDiff diff,
        final FileList base, final FileList target, final boolean excludeRemoved) {

        final Set<String> baseFolders = new HashSet<String>();
        final Set<String> baseFiles = new HashSet<String>();

        final Set<String> targetFolders = new HashSet<String>();
        final Set<String> targetFiles = new HashSet<String>();

        for (final String path : base.getPaths()) {

            if (isFolder(path))
                baseFolders.add(path);
            else
                baseFiles.add(path);
        }

        for (final String path : target.getPaths()) {

            if (isFolder(path))
                targetFolders.add(path);
            else
                targetFiles.add(path);
        }

        diff.addedFiles.addAll(targetFiles);
        diff.addedFiles.removeAll(baseFiles);

        if (!excludeRemoved) {
            diff.removedFiles.addAll(baseFiles);
            diff.removedFiles.removeAll(targetFiles);
        }

        final Set<String> filesIntersection = new HashSet<String>();

        filesIntersection.addAll(targetFiles);
        filesIntersection.retainAll(baseFiles);

        for (final String path : filesIntersection) {

            final MetaData baseData = base.getMetaData(path);
            final MetaData targetData = target.getMetaData(path);

            if ((baseData == null && targetData == null)
                || (baseData != null && targetData != null)
                && (baseData.checksum == targetData.checksum)) {
                diff.unalteredFiles.add(path);
            } else {
                diff.alteredFiles.add(path);
            }
        }

        diff.addedFolders.addAll(targetFolders);
        diff.addedFiles.removeAll(baseFolders);

        if (!excludeRemoved) {
            diff.removedFolders.addAll(baseFolders);
            diff.removedFolders.removeAll(targetFolders);
        }

        final Set<String> foldersIntersection = new HashSet<String>();

        foldersIntersection.addAll(targetFolders);
        foldersIntersection.retainAll(baseFolders);

        diff.unalteredFolders.addAll(foldersIntersection);

    }

    /**
     * Returns an unmodifiable list containing the files that must be added to
     * <code>base</code> to match <code>target</code>, i.e the files do not
     * exist.
     *
     * @return an unmodifiable list containing the files that must be added.
     */

    public List<String> getAddedFiles() {
        return Collections.unmodifiableList(addedFiles);
    }

    /**
     * Returns an unmodifiable list containing the files that must be removed
     * from <code>base</code> to match <code>target</code>.
     *
     * @return an unmodifiable list containing the files that must be removed.
     */
    public List<String> getRemovedFiles() {
        return Collections.unmodifiableList(removedFiles);
    }

    /**
     * Returns an unmodifiable list containing the files that must be changed,
     * i.e their contents differ.
     *
     * @return an unmodifiable list containing the files that that must be
     *         changed.
     */
    public List<String> getAlteredFiles() {
        return Collections.unmodifiableList(alteredFiles);
    }

    /**
     * Returns an unmodifiable list containing the files that do not need to be
     * changed.
     *
     * @return an unmodifiable list containing the files that do not need to be
     *         changed.
     */
    public List<String> getUnalteredFiles() {
        return Collections.unmodifiableList(unalteredFiles);
    }

    /**
     * Returns an unmodifiable list containing the folders that must be added to
     * <code>base</code> to match <code>target</code>.
     *
     * @return an unmodifiable list containing the folders that must be added.
     */
    public List<String> getAddedFoldersV2() {
        return Collections.unmodifiableList(addedFolders);
    }

    /**
     * Returns an unmodifiable list containing the folders that must be deleted
     * from <code>base</code> to match <code>target</code>.
     *
     * @return an unmodifiable list containing the removed folders that must be
     *         removed.
     */
    public List<String> getRemovedFolders() {
        return Collections.unmodifiableList(removedFolders);
    }

    /**
     * Returns an unmodifiable list containing the folders that do not need to
     * be changed.
     *
     * @return an unmodifiable list containing the folders that do not need to
     *         be changed.
     */
    public List<String> getUnalteredFolders() {
        return Collections.unmodifiableList(unalteredFolders);
    }

    /**
     * Subset of {@link FileList#getPaths() target.getPaths()}: All entries that
     * do not exist in <code>base</code>.
     *
     * @return Same format as {@link FileList#getPaths()}. It is safe to
     *         manipulate the returned list; this diff won't be affected.
     */
    public List<String> getAddedPaths() {
        return new ArrayList<String>(added);
    }

    /**
     * Subset of {@link FileList#getPaths() target.getPaths()}: All empty
     * folders that do not exist in <code>base</code>.
     *
     * @return Same format as {@link FileList#getPaths()}. It is safe to
     *         manipulate the returned list; this diff won't be affected.
     */
    public List<String> getAddedFolders() {
        List<String> addedFolders = new ArrayList<String>();

        for (String path : added) {
            if (path.endsWith(FileList.DIR_SEPARATOR)) {
                addedFolders.add(path);
            }
        }

        return addedFolders;
    }

    /**
     * Excludes all added empty folders from this diff.
     */
    public void clearAddedFolders() {
        added.removeAll(getAddedFolders());
    }

    /**
     * Subset of {@link FileList#getPaths() base.getPaths()}: All entries that
     * do not exist in <code>target</code>.
     *
     * @return Same format as {@link FileList#getPaths()}. It is safe to
     *         manipulate the returned list; this diff won't be affected.
     */
    public List<String> getRemovedPaths() {
        return new ArrayList<String>(removed);
    }

    /**
     * Subset of {@link FileList#getPaths() base.getPaths()}: All entries that
     * do not exist in <code>target</code>, except folders that contain
     * unaltered entries.
     *
     * @return Same format as {@link FileList#getPaths()}. It is safe to
     *         manipulate the returned list; this diff won't be affected.
     */
    public List<String> getRemovedPathsSanitized() {
        List<String> sanitized = getRemovedPaths();

        for (String path : unaltered) {
            String previous = path;
            while (previous.indexOf(FileList.DIR_SEPARATOR) > -1) {
                previous = path.substring(0,
                    previous.lastIndexOf(FileList.DIR_SEPARATOR));
                sanitized.remove(previous);
            }
        }

        return sanitized;
    }

    /**
     * Excludes all removed paths from this diff.
     */
    public void clearRemovedPaths() {
        removed.clear();
    }

    /**
     * Subset of the intersection of {@link FileList#getPaths() base.getPaths()}
     * and {@link FileList#getPaths() target.getPaths()}: All entries that have
     * not been changed.
     *
     * @return Same format as {@link FileList#getPaths()}. It is safe to
     *         manipulate the returned list; this diff won't be affected.
     */
    public List<String> getUnalteredPaths() {
        return new ArrayList<String>(unaltered);
    }

    /**
     * Subset of the intersection of {@link FileList#getPaths() base.getPaths()}
     * and {@link FileList#getPaths() target.getPaths()}: All entries that have
     * been changed.
     *
     * @return Same format as {@link FileList#getPaths()}. It is safe to
     *         manipulate the returned list; this diff won't be affected.
     */
    public List<String> getAlteredPaths() {
        return new ArrayList<String>(altered);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + added.hashCode();
        result = prime * result + altered.hashCode();
        result = prime * result + removed.hashCode();
        result = prime * result + unaltered.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FileListDiff)) {
            return false;
        }

        FileListDiff other = (FileListDiff) obj;
        return this.added.equals(other.added)
            && this.removed.equals(other.removed)
            && this.altered.equals(other.altered)
            && this.unaltered.equals(other.unaltered);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
            "added {0}, removed {1}, altered {2}, unaltered {3}", added,
            removed, altered, unaltered);
    }

    private static boolean isFolder(final String path) {
        return path.endsWith(FileList.DIR_SEPARATOR);
    }
}
