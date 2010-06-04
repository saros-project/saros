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
 * to other file lists. Folders are denoted by a trailing separator.
 * 
 * NOTE: As computation of a FileList involves a complete rescan of the project,
 * creating new instances should be avoided.<br>
 * TODO: This class should be split up to clearly separate between file lists
 * and differences between file lists. Fields like removed, added, etc. do not
 * make sense for plain lists and just add confusion.
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
     */
    // * @see #readResolve()
    // @XStreamOmitField
    protected Map<IPath, Long> all = new HashMap<IPath, Long>();

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
        // this.unaltered.putAll(this.all);
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
        // this.unaltered.putAll(this.all);
    }

    /**
     * Returns a new FileList which contains the difference of two FileLists,
     * consisting of files missing in <code>other</code> but present in
     * <code>this</code> and files in <code>other</code> which are not present
     * in <code>this</code>.
     * 
     * @param other
     *            the <code>FileList</code> to compare to.
     * 
     * @return a new <code>FileList</code> which contains the difference
     *         information of the two <code>FileList</code>s:
     *         <code>result.removed</code> contains paths present in
     *         <code>this.all</code> but not in <code>other.all</code>.
     *         <code>result.added</code> contains paths present in
     *         <code>other.all</code> but not in <code>this.all</code>.
     *         <code>result.unaltered</code>/<code>results.altered</code>
     *         contain paths where the checksum is equal to/differs between
     *         <code>this</code> and <code>other</code>. <code>result.all</code>
     *         contains all paths from <code>other</code>.
     * 
     *         The diff contains the operations which are needed to get from
     *         <code>this</code> <code>FileList</code> to the <code>other</code>
     *         <code>FileList</code>.
     */
    public FileListDiff diff(FileList other) {
        FileListDiff result = new FileListDiff();

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

        // result.all.putAll(other.all);
        return result;
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
            FileListDiff difference = this.diff(other);
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
        return sorted(this.all.keySet());
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
    // protected Object readResolve() {
    // this.all = new HashMap<IPath, Long>();
    // this.all.putAll(added);
    // this.all.putAll(altered);
    // this.all.putAll(unaltered);
    // return this;
    // }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((all == null) ? 0 : all.hashCode());
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
        return this.all.equals(other.all);
    }

    @Override
    public String toString() {
        return "FileList(files:" + this.all.size() + ")";
    }

    protected List<IPath> sorted(Set<IPath> pathSet) {
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

}
