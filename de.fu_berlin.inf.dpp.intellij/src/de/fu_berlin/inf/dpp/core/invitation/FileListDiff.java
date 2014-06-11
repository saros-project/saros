/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.invitation.FileList.MetaData;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.picocontainer.annotations.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A diff between two {@link FileList}s.
 *
 * @author ahaferburg
 * @see FileList#diff(FileList)
 */
// TODO Consolidate carefully, considerable difference with Saros/E counterpart
public class FileListDiff {
    // TODO Make this a public static internal class of FileList?

    //todo: inject it
    @Inject
    public static IWorkspace workspace;
    private final List<IPath> added = new ArrayList<IPath>();

    private final List<IPath> removed = new ArrayList<IPath>();

    private final List<IPath> altered = new ArrayList<IPath>();

    private final List<IPath> unaltered = new ArrayList<IPath>();

    private FileListDiff() {
        // Empty but non-public. Only FileListDiff#diff should create
        // FileListDiffs.
    }

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
     * @param base   The base <code>FileList</code>.
     * @param target The <code>FileList</code> to compare to.
     * @return a new <code>FileListDiff</code> which contains the difference
     * information of the two <code>FileList</code>s.
     */
    public static FileListDiff diff(FileList base, FileList target) {
        FileListDiff result = new FileListDiff();
        if (base == null || target == null) {
            return result;
        }
        // we have to copy the set because we should not work on references when
        // deleting
        Set<IPath> baseEntries = new TreeSet<IPath>(base.getPaths());
        Set<IPath> targetEntries = new TreeSet<IPath>(target.getPaths());

        // determine the paths that don't match the target to delete them
        baseEntries.removeAll(targetEntries);
        result.removed.addAll(baseEntries);

        baseEntries = new TreeSet<IPath>(base.getPaths());
        targetEntries = new TreeSet<IPath>(target.getPaths());
        // determine the paths that are not already present in local workspace
        targetEntries.removeAll(baseEntries);
        result.added.addAll(targetEntries);

        // determine for all matching paths if files are altered
        baseEntries = new TreeSet<IPath>(base.getPaths());
        targetEntries = new TreeSet<IPath>(target.getPaths());
        baseEntries
            .retainAll(targetEntries);    //todo: not check for changes RK
        for (IPath path : baseEntries) {
            if (path.hasTrailingSeparator()) {
                result.unaltered.add(path);
                continue;
            }

            MetaData fileData = base.getMetaData(path);
            MetaData otherFileData = target.getMetaData(path);
            if ((fileData == null && otherFileData == null)
                || (fileData != null && otherFileData != null) && (
                fileData.checksum == otherFileData.checksum)) {
                result.unaltered.add(path);
            } else {
                result.altered.add(path);
            }
        }

        return result;
    }

    public List<IPath> getRemovedPaths() {
        return removed;
    }

    public void clearRemovedPaths() {
        removed.clear();
    }

    public List<IPath> getUnalteredPaths() {
        return unaltered;
    }

    public List<IPath> getAddedPaths() {
        return added;
    }

    public List<IPath> getAlteredPaths() {
        return altered;
    }

    /* FIXME THIS PERFORMS LOGIC THAT SHOULD BE PLACED ELSEWHERE */

    /**
     * Will create all folders contained in this FileList for the given project
     * and return a FileList which does not contain these folders.
     *
     * @throws IOException
     */
    public FileListDiff addAllFolders(IProject localProject,
        IProgressMonitor monitor) throws IOException {

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

    /* FIXME THIS PERFORMS LOGIC THAT SHOULD BE PLACED ELSEWHERE */

    /**
     * Removes all resources marked as removed in this FileList from the given
     * project.
     *
     * @param localProject the local project were the shared project will be replicated.
     * @throws IOException
     */
    public FileListDiff removeUnneededResources(final IProject localProject,
        final IProgressMonitor monitor) throws IOException {

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
            @Override
            public void run(IProgressMonitor progress)
                throws OperationCanceledException, IOException {
                ISubMonitor subMonitor = monitor.convert(progress);
                for (IPath path : toDelete) {
                    IResource resource = path.hasTrailingSeparator() ?
                        localProject.getFolder(path) :
                        localProject.getFile(path);

                    // Check if resource exists because it might have already
                    // been deleted when deleting its folder
                    if (resource.exists()) {
                        subMonitor.subTask("Deleting " + path.lastSegment());
                        resource
                            .delete(IResource.FORCE | IResource.KEEP_HISTORY);
                        //                        resource.delete(IResource.FORCE
                        //                                | IResource.KEEP_HISTORY, subMonitor.newChild(1)); //todo: change interface
                    }
                }
                subMonitor.done();
            }
        };

        monitor.beginTask("Removing resources", toDelete.size());

        workspace
            .run(deleteProcedure, workspace.getRoot(), IWorkspace.AVOID_UPDATE,
                monitor);

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
        result =
            prime * result + ((unaltered == null) ? 0 : unaltered.hashCode());
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
        return this.added.equals(other.added) && this.removed
            .equals(other.removed) && this.altered.equals(other.altered)
            && this.unaltered.equals(other.unaltered);
    }

}