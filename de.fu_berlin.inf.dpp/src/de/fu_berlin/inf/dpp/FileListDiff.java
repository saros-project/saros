package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;

/*
 * FIXME ndh: Both the Incoming and OutgoingInvProc send a file list. But Out
 * uses a FileList, Inc uses a Diff. Inc should use the Diff to construct
 * another FileList.
 */

public class FileListDiff
// extends FileList
{
    protected final Map<IPath, Long> added = new HashMap<IPath, Long>();

    protected final Map<IPath, Long> removed = new HashMap<IPath, Long>();

    protected final Map<IPath, Long> altered = new HashMap<IPath, Long>();

    protected final Map<IPath, Long> unaltered = new HashMap<IPath, Long>();

    public List<IPath> getRemovedPaths() {
        return sorted(this.removed.keySet());
    }

    public List<IPath> getUnalteredPaths() {
        return sorted(this.unaltered.keySet());
    }

    public List<IPath> getAddedPaths() {
        return sorted(this.added.keySet());
    }

    public List<IPath> getAlteredPaths() {
        return sorted(this.altered.keySet());
    }

    // @Override
    // not
    protected List<IPath> sorted(Set<IPath> pathSet) {
        List<IPath> paths = new ArrayList<IPath>(pathSet);
        Collections.sort(paths, new FileList.PathLengthComparator());
        return paths;
    }

    /**
     * Will create all folders contained in this FileList for the given project
     * and return a FileList which does not contain these folders.
     * 
     * Note: All parent folders of any folder contained in the FileList must be
     * contained as well.
     * 
     * @throws CoreException
     */
    public FileListDiff addAllFolders(IProject localProject, SubMonitor monitor)
        throws CoreException {

        List<IPath> toCheck = this.getAddedPaths();
        monitor.beginTask("Adding folders", toCheck.size());

        FileListDiff result = new FileListDiff();
        result.altered.putAll(this.altered);
        result.removed.putAll(this.removed);
        result.unaltered.putAll(this.unaltered);

        for (IPath path : toCheck) {

            if (path.hasTrailingSeparator()) {
                IFolder folder = localProject.getFolder(path);
                if (!folder.exists()) {
                    monitor.subTask("Creating folder " + path.lastSegment());
                    folder.create(true, true, monitor.newChild(1));
                    continue;
                }
            } else {
                result.added.put(path, added.get(path));
            }
            monitor.worked(1);
        }
        // result.readResolve();

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
    public FileListDiff removeUnneededResources(IProject localProject,
        SubMonitor monitor) throws CoreException {

        // TODO don't throw CoreException
        // TODO check if this triggers the resource listener

        List<IPath> toDelete = this.getRemovedPaths();
        monitor.beginTask("Removing resources", toDelete.size());

        for (IPath path : toDelete) {

            monitor.subTask("Deleting " + path.lastSegment());
            if (path.hasTrailingSeparator()) {
                IFolder folder = localProject.getFolder(path);

                if (folder.exists()) {
                    folder.delete(true, monitor.newChild(1));
                }

            } else {
                IFile file = localProject.getFile(path);

                // check if file exists because it might have already been
                // deleted when deleting its folder
                if (file.exists()) {
                    file.delete(true, monitor.newChild(1));
                }
            }
        }

        FileListDiff result = new FileListDiff();
        result.added.putAll(this.added);
        result.altered.putAll(this.altered);
        // Removed is empty now
        result.unaltered.putAll(this.unaltered);
        // result.readResolve();

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