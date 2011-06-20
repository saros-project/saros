package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.FileList.FileListData;
import de.fu_berlin.inf.dpp.util.FileUtils;

/**
 * A diff between two {@link FileList}s.
 * 
 * @see FileList#diff(FileList)
 * 
 * @author ahaferburg
 */
public class FileListDiff {
    // TODO Make this a public static internal class of FileList?

    protected FileListDiff() {
        // Empty but non-public. Only FileListDiff#diff should create
        // FileListDiffs.
    }

    private final List<IPath> added = new ArrayList<IPath>();

    private final List<IPath> removed = new ArrayList<IPath>();

    private final List<IPath> altered = new ArrayList<IPath>();

    private final List<IPath> unaltered = new ArrayList<IPath>();

    /**
     * Returns a new <code>FileListDiff</code> which contains the difference of
     * the two <code>FileList</code>s.<br>
     * <br>
     * The diff describes the operations needed to transform <code>base</code>
     * into <code>target</code>. For example, the result's
     * <code>getAddedPaths()</code> returns the list of files that are present
     * in <code>target</code>, but not in <code>base</code>.<br>
     * <br>
     * If either of the two parameters is <code>null</code>, the result is an
     * empty diff.
     * 
     * @param base
     *            The base <code>FileList</code>.
     * @param target
     *            The <code>FileList</code> to compare to.
     * 
     * @return a new <code>FileListDiff</code> which contains the difference
     *         information of the two <code>FileList</code>s.
     */
    public static FileListDiff diff(FileList base, FileList target) {
        FileListDiff result = new FileListDiff();
        if (base == null || target == null)
            return result;
        // we have to copy the set because we should not work on references when
        // deleting
        Set<IPath> baseEntries = new HashSet<IPath>(base.entries.keySet());
        Set<IPath> targetEntries = new HashSet<IPath>(target.entries.keySet());

        // determine the paths that don't match the target to delete them
        baseEntries.removeAll(targetEntries);
        result.removed.addAll(baseEntries);

        // determine the paths that are not already present in local workspace
        baseEntries = new HashSet<IPath>(base.entries.keySet());
        targetEntries.removeAll(baseEntries);
        result.added.addAll(targetEntries);

        // determine for all matching paths if files are altered
        targetEntries = new HashSet<IPath>(target.entries.keySet());
        baseEntries.retainAll(targetEntries);
        for (IPath path : baseEntries) {
            if (path.hasTrailingSeparator()) {
                result.unaltered.add(path);
            } else {
                FileListData fileData = base.entries.get(path);
                FileListData otherFileData = target.entries.get(path);
                if (fileData != null && otherFileData != null) {
                    if (fileData.checksum == otherFileData.checksum) {
                        result.unaltered.add(path);
                    } else {
                        result.altered.add(path);
                    }
                } else {
                    result.unaltered.add(path);
                }
            }
        }

        FileList.PathLengthComparator pathLengthComparator = new FileList.PathLengthComparator();
        Collections.sort(result.added, pathLengthComparator);
        Collections.sort(result.removed, pathLengthComparator);
        Collections.sort(result.unaltered, pathLengthComparator);
        Collections.sort(result.altered, pathLengthComparator);

        return result;
    }

    /**
     * @return The sorted list of paths that were removed.
     */
    public List<IPath> getRemovedPaths() {
        return sorted(this.removed);
    }

    public List<IPath> getUnalteredPaths() {
        return sorted(this.unaltered);
    }

    public List<IPath> getAddedPaths() {
        return sorted(this.added);
    }

    public List<IPath> getAlteredPaths() {
        return sorted(this.altered);
    }

    private boolean issorted(List<IPath> paths) {
        FileList.PathLengthComparator pathLengthComparator = new FileList.PathLengthComparator();
        Iterator<IPath> it = paths.iterator();
        if (!it.hasNext())
            return true;
        IPath prev = it.next();
        IPath current;
        while (it.hasNext()) {
            current = it.next();
            if (pathLengthComparator.compare(prev, current) > 0)
                return false;
            prev = current;
        }
        return true;
    }

    private List<IPath> sorted(List<IPath> paths) {
        assert issorted(paths);
        return new ArrayList<IPath>(paths);
    }

    /**
     * Will create all folders contained in this FileList for the given project
     * and return a FileList which does not contain these folders.
     * 
     * @throws CoreException
     */
    public FileListDiff addAllFolders(IProject localProject, SubMonitor monitor)
        throws CoreException {

        List<IPath> toCheck = this.getAddedPaths();
        monitor.beginTask("Adding folders", toCheck.size());

        FileListDiff result = new FileListDiff();
        result.altered.addAll(this.altered);
        result.removed.addAll(this.removed);
        result.unaltered.addAll(this.unaltered);

        for (IPath path : toCheck) {

            if (path.hasTrailingSeparator()) {
                IFolder folder = localProject.getFolder(path);
                if (!folder.exists()) {
                    monitor.subTask("Creating folder " + path.lastSegment());
                    FileUtils.create(folder);
                    continue;
                }
            } else {
                result.added.add(path);
            }
            monitor.worked(1);
        }
        monitor.done();
        return result;
    }

    /**
     * Removes all resources marked as removed in this FileList from the given
     * project.
     * 
     * @param localProject
     *            the local project were the shared project will be replicated.
     * @throws CoreException
     */
    public FileListDiff removeUnneededResources(final IProject localProject,
        final SubMonitor monitor) throws CoreException {
        // TODO don't throw CoreException

        // TODO Move to FileUtil, refactor FileUtil#delete(IResource).
        final List<IPath> toDelete = this.getRemovedPaths();

        // don't delete the path of unaltered files
        List<IPath> toKeep = this.getUnalteredPaths();
        for (IPath iPath : toKeep) {
            for (int i = 0; i < iPath.segmentCount(); i++) {
                toDelete.remove(iPath.removeLastSegments(i));
            }
        }
        IWorkspaceRunnable deleteProcedure = new IWorkspaceRunnable() {
            public void run(IProgressMonitor progress) throws CoreException {
                // SubMonitor monitor = (SubMonitor) progress;// doesn't work?
                for (IPath path : toDelete) {
                    monitor.subTask("Deleting " + path.lastSegment());
                    IResource resource = path.hasTrailingSeparator() ? localProject
                        .getFolder(path) : localProject.getFile(path);

                    // Check if resource exists because it might have already
                    // been deleted when deleting its folder
                    if (resource.exists()) {
                        resource.delete(true, monitor.newChild(1));
                    }
                }
            }
        };

        monitor.beginTask("Removing resources", toDelete.size());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(deleteProcedure, workspace.getRoot(),
            IWorkspace.AVOID_UPDATE, monitor);

        FileListDiff result = new FileListDiff();
        result.added.addAll(this.added);
        result.altered.addAll(this.altered);
        // Removed is empty now
        // TODO only if there was no exception
        result.unaltered.addAll(this.unaltered);

        monitor.done();

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((added == null) ? 0 : added.hashCode());
        result = prime * result + ((altered == null) ? 0 : altered.hashCode());
        result = prime * result + ((removed == null) ? 0 : removed.hashCode());
        result = prime * result
            + ((unaltered == null) ? 0 : unaltered.hashCode());
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

}