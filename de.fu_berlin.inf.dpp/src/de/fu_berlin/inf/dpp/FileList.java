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

import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * A FileList is a list of resources - files and folders - which belong to the
 * same {@link IProject}. FileLists can be compared to other FileLists. Folders
 * are denoted by a trailing separator.<br>
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

    protected final boolean useVersionControl;

    /** The actual file list data. Keys are project relative paths. */
    protected Map<IPath, FileListData> entries = new HashMap<IPath, FileListData>();
    /** Identifies the VCS used. */
    protected String vcsProviderID;

    /** @see VCSAdapter#getRepositoryString(IResource) */
    protected String vcsRepositoryRoot;
    /** VCS internal information. */
    protected VCSResourceInfo vcsProjectInfo;

    /** ID of Project this list of files belog to */
    protected String projectID;

    static class FileListData {
        /** Checksum of this file. */
        public long checksum;
        /** Identifies the version of this file in the repository. */
        VCSResourceInfo vcsInfo;

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FileListData other = (FileListData) obj;
            if (checksum != other.checksum)
                return false;
            if (vcsInfo == null) {
                if (other.vcsInfo != null)
                    return false;
            } else if (!vcsInfo.equals(other.vcsInfo))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (checksum ^ (checksum >>> 32));
            result = prime * result
                + ((vcsInfo == null) ? 0 : vcsInfo.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "FLD[" + checksum + ", " + vcsInfo + "]";
        }

    }

    public String getVCSRevision(IPath path) {
        if (path.isEmpty())
            return vcsProjectInfo.revision;
        FileListData fileListData = entries.get(path);
        if (fileListData == null)
            return null;
        return fileListData.vcsInfo.revision;
    }

    public String getVCSUrl(IPath path) {
        if (path.isEmpty())
            return vcsProjectInfo.url;
        FileListData fileListData = entries.get(path);
        if (fileListData == null)
            return null;
        return fileListData.vcsInfo.url;
    }

    public static class PathLengthComparator implements Comparator<IPath>,
        Serializable {
        // Serializable so that e.g. a TreeMap sorted by PathLengthComparator
        // remains serializable.
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
        this(true);
    }

    /**
     * Creates an empty file list.
     * 
     * @param useVersionControl
     *            If false, the FileList won't include version control
     *            information.
     */
    public FileList(boolean useVersionControl) {
        this.useVersionControl = useVersionControl;
    }

    /**
     * Creates a new file list from the file tree in given container.
     * 
     * @param container
     *            The resource container that should be represented by the new
     *            file list.
     * @param useVersionControl
     *            If false, the FileList won't include version control
     *            information.
     * @throws CoreException
     *             Exception that might happen while fetching the files from the
     *             given container.
     * 
     *             TODO 4 - Use ProgressMonitors to keep track of the creation
     *             of a FileList
     */
    public FileList(IContainer container, boolean useVersionControl)
        throws CoreException {
        this(useVersionControl);
        container.refreshLocal(IResource.DEPTH_INFINITE, null);
        addMembers(container.members());
    }

    /**
     * Creates a new file list from given resources.
     * 
     * @param resources
     *            The resources that should be added to this file list.
     * @param useVersionControl
     *            If false, the FileList won't include version control
     *            information.
     * 
     * @throws CoreException
     */
    public FileList(IResource[] resources, boolean useVersionControl)
        throws CoreException {
        this(useVersionControl);
        addMembers(resources);
    }

    /**
     * Creates a new file list from given paths. Doesn't compute checksums or
     * location information.
     * 
     * @param paths
     *            The paths that should be added to this file list.
     */
    public FileList(List<IPath> paths) {
        this(false);
        if (paths != null) {
            for (IPath path : paths) {
                this.entries.put(path, null);
            }
        }
    }

    public FileList(IProject source) throws CoreException {
        this(source, true);
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
     * Calculate an approximation of how equal this <code>FileList</code> is to
     * <code>project</code>. NOTE: This is a long-running method linear to size
     * of the length of the two file lists.
     * 
     * @param project
     * @return Percentage of "sameness" of <code>this</code> and
     *         <code>other</code> counting the number of identical files
     *         relative to the size of the larger list. 100 means identical
     *         <code>FileList</code>s. On error, returns 0.
     */
    public int computeMatch(IProject project) {
        try {
            return this.computeMatch(new FileList(project, useVersionControl));
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

    private void addMembers(IResource[] resources) throws CoreException {
        if (resources.length == 0)
            return;
        IProject project = null;
        VCSAdapter vcs = null;
        if (useVersionControl) {
            project = resources[0].getProject();
            vcs = VCSAdapter.getAdapter(project);
            if (vcs != null) {
                String providerID = vcs.getProviderID(project);

                this.vcsProviderID = providerID;
                this.vcsRepositoryRoot = vcs.getRepositoryString(project);
                this.vcsProjectInfo = vcs.getCurrentResourceInfo(project);
            }
        }

        final boolean isManagedProject = vcs != null;
        for (IResource resource : resources) {
            if (resource.isDerived()) {
                continue;
            }

            assert !useVersionControl
                || (project != null && project.equals(resource.getProject()));

            if (resource instanceof IFile) {
                IFile file = (IFile) resource;
                if (!file.exists()) {
                    continue;
                }

                try {
                    FileListData data = new FileListData();
                    data.checksum = FileUtils.checksum(file);

                    if (isManagedProject)
                        addVCSInfo(resource, data, vcs);

                    this.entries.put(file.getProjectRelativePath(), data);
                } catch (IOException e) {
                    log.error(e);
                }

            } else if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;

                IPath path = folder.getProjectRelativePath();
                if (!path.hasTrailingSeparator()) {
                    path = path.addTrailingSeparator();
                }

                FileListData data = null;
                if (isManagedProject) {
                    data = new FileListData();
                    if (!addVCSInfo(resource, data, vcs))
                        data = null;
                }
                this.entries.put(path, data);
                addMembers(folder.members());
            }
        }
    }

    private boolean addVCSInfo(IResource resource, FileListData data,
        VCSAdapter vcs) {
        final VCSResourceInfo info = vcs.getCurrentResourceInfo(resource);
        if (info == null)
            return false;

        data.vcsInfo = info;
        return true;
    }

    // FIXME ndh remove/fix tests
    public FileListDiff diff(FileList other) {
        return FileListDiff.diff(this, other);
    }

    // TODO ndh error handling
    public long getChecksum(IPath path) {
        FileListData fileListData = this.entries.get(path);
        return fileListData != null ? fileListData.checksum : -1;
    }

    public String getVcsProviderID() {
        return vcsProviderID;
    }

    public VCSResourceInfo getProjectInfo() {
        return vcsProjectInfo;
    }

    public String getRepositoryRoot() {
        return vcsRepositoryRoot;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }
}
