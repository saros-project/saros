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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;

/**
 * A FileList is a list of resources - files and folders - which can be compared
 * to other file lists. Folders are denoted by a trailing separator.<br>
 * <br>
 * NOTE: As computation of a FileList involves a complete rescan of the project,
 * creating new instances should be avoided.
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
     * Contains all entries.
     */
    protected Map<IPath, FileListData> entries = new HashMap<IPath, FileListData>();

    static class FileListData {
        public long checksum;
    }

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
        addMembers(container.members(), this.entries, true);
    }

    /**
     * Creates a new file list from given resources.
     * 
     * @param resources
     *            the resources that should be added to this file list.
     * @throws CoreException
     */
    public FileList(IResource[] resources) throws CoreException {
        addMembers(resources, this.entries, true);
    }

    /**
     * Creates a new file list from the given paths.
     * 
     * @param paths
     *            The paths that should be added to this file list.
     */
    public FileList(IPath[] paths) {
        for (IPath path : paths) {
            this.entries.put(path, null);
        }
    }

    /**
     * Calculate an approximation of how equal <code>this</code>
     * <code>FileList</code> is to <code>other</code>. NOTE: This is a
     * long-running method linear to size of the length of the two file lists.
     * 
     * @param other
     * @return Percentage of "sameness" of <code>this</code> and
     *         <code>other</code> counting the number of identical files
     *         relative to the size of the larger list. 100 means identical
     *         <code>FileList</code>s.
     */
    public int computeMatch(FileList other) {
        // calculate "sameness" of ratio of longer list
        List<IPath> otherPaths = other.getPaths();
        int nPaths = Math.max(getPaths().size(), otherPaths.size());

        if (nPaths == 0 && otherPaths.isEmpty())
            return 100; // both are empty -> perfect match

        if (nPaths == 0) { // other is empty
            return 0;
        } else {
            FileListDiff difference = FileListDiff.diff(this, other);
            int nUnalteredPaths = difference.getUnalteredPaths().size();
            if (nPaths == nUnalteredPaths) {
                return 100;
            } else {
                return Math.min(99, nUnalteredPaths * 100 / nPaths);
            }
        }
    }

    /**
     * Calculate an approximation of how equal <code>this</code>
     * <code>FileList</code> is to <code>project</code>. NOTE: This is a
     * long-running method linear to size of the length of the two file lists.
     * 
     * @param project
     * @return Percentage of "sameness" of <code>this</code> and
     *         <code>other</code> counting the number of identical files
     *         relative to the size of the larger list. 100 means identical
     *         <code>FileList</code>s. On error, returns 0.
     */
    public int computeMatch(IProject project) {
        try {
            return this.computeMatch(new FileList(project));
        } catch (CoreException e) {
            log.error("Failed to generate FileList for match computation", e);
        }
        return 0;
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
        return sorted(this.entries.keySet());
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
        return this.entries.equals(other.entries);
    }

    @Override
    public String toString() {
        return "FileList(files:" + this.entries.size() + ")";
    }

    protected List<IPath> sorted(Set<IPath> pathSet) {
        List<IPath> paths = new ArrayList<IPath>(pathSet);
        Collections.sort(paths, new PathLengthComparator());
        return paths;
    }

    private void addMembers(IResource[] resources,
        Map<IPath, FileListData> members, boolean ignoreDerived)
        throws CoreException {

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
                    FileListData data = new FileListData();
                    data.checksum = FileUtil.checksum(file);
                    members.put(file.getProjectRelativePath(), data);
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

    // FIXME ndh remove/fix tests
    public FileListDiff diff(FileList other) {
        return FileListDiff.diff(this, other);
    }
}
