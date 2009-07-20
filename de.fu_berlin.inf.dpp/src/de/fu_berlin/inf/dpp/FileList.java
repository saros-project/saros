/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;

/**
 * A FileList is a list of resources - files and folders - which can be compared
 * to other file lists. Folders are denoted by a trailing separator.
 * 
 * @author rdjemili
 */
@XStreamAlias("fileList")
public class FileList {

    private static final Logger log = Logger.getLogger(FileList.class);

    /**
     * {@link XStream} instance to serialize {@link FileList} objects to and
     * from XML.
     */
    protected static XStream xstream;

    /**
     * @invariant Contains all entries from this.added, this.altered, and
     *            this.unaltered.
     * 
     * @see #readResolve()
     */
    @XStreamOmitField
    private Map<IPath, Long> all = new HashMap<IPath, Long>();

    private final Map<IPath, Long> added = new HashMap<IPath, Long>();

    private final Map<IPath, Long> removed = new HashMap<IPath, Long>();

    private final Map<IPath, Long> altered = new HashMap<IPath, Long>();

    private final Map<IPath, Long> unaltered = new HashMap<IPath, Long>();

    public static class PathLengthComparator implements Comparator<IPath>,
        Serializable {

        private static final long serialVersionUID = 8656330038498525163L;

        /**
         * Compares {@link IPath}s by the length of their string representation.
         * 
         * @see Comparator#compare(Object, Object)
         */
        public int compare(IPath path1, IPath path2) {
            return path1.toString().length() - path2.toString().length();
        }
    }

    /**
     * Creates an empty file list.
     */
    public FileList() {
        // do nothing
    }

    /**
     * Creates a new file list from the file tree in given container.
     * 
     * @param container
     *            the resource container that should be represented by the new
     *            file list.
     * @throws CoreException
     *             exception that might happen while fetching the files from the
     *             given container.
     * 
     *             TODO 6 - Not all users of FileList need the checksums, so we
     *             should not always calculate them.
     * 
     *             TODO 4 - Use ProgressMonitors to keep track of the creation
     *             of a FileList
     */
    public FileList(IContainer container) throws CoreException {
        container.refreshLocal(IResource.DEPTH_INFINITE, null);
        addMembers(container.members(), this.all, true);
        this.unaltered.putAll(this.all);
    }

    /**
     * Creates a new file list from given resources.
     * 
     * @param resources
     *            the resources that should be added to this file list.
     * @throws CoreException
     */
    public FileList(IResource[] resources) throws CoreException {

        addMembers(resources, this.all, true);
        this.unaltered.putAll(this.all);
    }

    // TODO invert diff direction
    /**
     * Returns a new FileList which contains the diff from the two FileLists.
     * 
     * @param other
     *            the other FileList with which this FileList is compared with.
     * 
     * @return a new FileList which contains the diff information from the two
     *         FileLists. The diff contains the operations which are needed to
     *         get from this FileList to the other FileList.
     */
    public FileList diff(FileList other) {
        FileList result = new FileList();

        for (Map.Entry<IPath, Long> entry : this.all.entrySet()) {
            if (!other.all.containsKey(entry.getKey())) {
                result.removed.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<IPath, Long> entry : other.all.entrySet()) {
            if (!this.all.containsKey(entry.getKey())) {
                result.added.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<IPath, Long> entry : this.all.entrySet()) {
            IPath path = entry.getKey();
            if (other.all.containsKey(path)) {

                if (path.hasTrailingSeparator()) {
                    result.unaltered.put(path, null);
                } else {
                    long checksum = entry.getValue();
                    long otherChecksum = other.all.get(path);

                    if (checksum == otherChecksum) {
                        result.unaltered.put(path, checksum);
                    } else {
                        result.altered.put(path, checksum);
                    }
                }
            }
        }

        result.all.putAll(other.all);
        return result;
    }

    /**
     * @return the amount in percentage by which this file list has the same
     *         files as the other file list. Returns 100 only if the given
     *         FileList matches perfectly.
     */
    public int match(FileList other) {
        int nPaths = getPaths().size();

        if (nPaths == 0 && other.getPaths().isEmpty())
            return 100; // both are empty -> perfect match

        if (nPaths == 0) {
            return 0;
        } else {
            FileList diff = this.diff(other);
            int nUnalteredPaths = diff.getUnalteredPaths().size();
            if (nPaths == nUnalteredPaths) {
                return 100;
            } else {
                return Math.min(99, nUnalteredPaths * 100 / nPaths);
            }
        }
    }

    protected static synchronized XStream getXStream() {
        if (xstream == null) {
            xstream = new XStream();
            xstream.registerConverter(new IPathConverter());
            xstream.aliasType("path", Path.class);
            xstream.processAnnotations(FileList.class);
        }
        return xstream;
    }

    /**
     * @return a sorted list of all paths in this file list. The paths are
     *         sorted by their character length.
     */
    public List<IPath> getPaths() {
        return sorted(this.all.keySet());
    }

    public List<IPath> getAddedPaths() {
        return sorted(this.added.keySet());
    }

    public List<IPath> getRemovedPaths() {
        return sorted(this.removed.keySet());
    }

    public List<IPath> getAlteredPaths() {
        return sorted(this.altered.keySet());
    }

    public List<IPath> getUnalteredPaths() {
        return sorted(this.unaltered.keySet());
    }

    /**
     * @return the XML representation of this FileList.
     * 
     * @see #fromXML(String)
     */
    public String toXML() {
        return getXStream().toXML(this);
    }

    /**
     * Parses given XML and returns a {@link FileList} instance.
     * 
     * @see FileList#toXML()
     */
    public static FileList fromXML(String xml) {
        return (FileList) getXStream().fromXML(xml);
    }

    /**
     * This is called after deserialization by XStream.
     * 
     * Initializes {@link #all} (which is not sent via XML) to ensure the
     * invariant of this class.
     */
    private Object readResolve() {
        all = new HashMap<IPath, Long>();
        all.putAll(added);
        all.putAll(altered);
        all.putAll(unaltered);
        return this;
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

        if (!(obj instanceof FileList)) {
            return false;
        }

        FileList other = (FileList) obj;
        return this.added.equals(other.added)
            && this.removed.equals(other.removed)
            && this.altered.equals(other.altered)
            && this.unaltered.equals(other.unaltered);
    }

    @Override
    public String toString() {
        return "FileList(files:" + this.all.size() + ")";
    }

    private List<IPath> sorted(Set<IPath> pathSet) {
        List<IPath> paths = new ArrayList<IPath>(pathSet);
        Collections.sort(paths, new PathLengthComparator());
        return paths;
    }

    private void addMembers(IResource[] resources, Map<IPath, Long> members,
        boolean ignoreDerived) throws CoreException {

        for (IResource resource : resources) {
            if (ignoreDerived && resource.isDerived()) {
                continue;
            }

            if (resource instanceof IFile) {
                IFile file = (IFile) resource;
                if (!file.exists()) {
                    continue;
                }

                try {
                    members.put(file.getProjectRelativePath(), FileUtil
                        .checksum(file));
                } catch (IOException e) {
                    log.error(e);
                }

            } else if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;

                IPath path = folder.getProjectRelativePath();
                if (!path.hasTrailingSeparator()) {
                    path = path.addTrailingSeparator();
                }

                members.put(path, null);
                addMembers(folder.members(), members, ignoreDerived);
            }
        }
    }

    /**
     * Removes all resources marked as removed in this FileList from the given
     * project.
     * 
     * @param localProject
     *            the local project were the shared project will be replicated.
     * @throws CoreException
     */
    public FileList removeUnneededResources(IProject localProject,
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

        FileList result = new FileList();
        result.added.putAll(this.added);
        result.altered.putAll(this.altered);
        // Removed is empty now
        result.unaltered.putAll(this.unaltered);
        result.readResolve();

        monitor.done();

        return result;
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
    public FileList addAllFolders(IProject localProject, SubMonitor monitor)
        throws CoreException {

        List<IPath> toCheck = this.getAddedPaths();
        monitor.beginTask("Adding folders", toCheck.size());

        FileList result = new FileList();
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
        result.readResolve();

        monitor.done();
        return result;
    }

}
