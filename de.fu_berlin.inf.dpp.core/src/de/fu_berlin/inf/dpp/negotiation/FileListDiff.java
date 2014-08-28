package de.fu_berlin.inf.dpp.negotiation;

import java.text.MessageFormat;
import java.util.ArrayList;
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

    private final List<String> added = new ArrayList<String>();

    private final List<String> removed = new ArrayList<String>();

    private final List<String> altered = new ArrayList<String>();

    private final List<String> unaltered = new ArrayList<String>();

    /**
     * Returns a new {@link FileListDiff} which contains the difference of the
     * two {@link FileList}s.
     * <p>
     * The diff describes the operations needed to transform <code>base</code>
     * into <code>target</code>. For example, the result's
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
     * 
     * @return a new {@link FileListDiff} which contains the difference
     *         information of the two {@link FileList}s.
     */
    public static FileListDiff diff(FileList base, FileList target) {
        FileListDiff result = new FileListDiff();

        if (base == null || target == null)
            return result;

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

}
