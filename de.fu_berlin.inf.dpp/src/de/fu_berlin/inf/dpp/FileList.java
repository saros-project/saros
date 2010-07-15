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
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;

/**
 * A FileList is a list of resources - files and folders - which can be compared
 * to other file lists. Folders are denoted by a trailing separator.<br>
 * <br>
 * NOTE: As computation of a FileList involves a complete rescan of the project,
 * creating new instances should be avoided.<br>
 * <br>
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

    /** The actual file list data. */
    protected Map<IPath, FileListData> data = new HashMap<IPath, FileListData>();
    /** Identifies the VCS used. */
    public String vcsProviderID;
    /** URL of the repository. */
    public String vcsRepository;

    public String vcsProjectRoot;

    public String vcsBaseRevision;

    static class FileListData {
        /** MD5 checksum of this file. */
        public long checksum;
        /** Identifies the version of this file in the repository. */
        String vcsRevision;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof FileListData)) {
                return false;
            }
            FileListData other = (FileListData) obj;
            return checksum == other.checksum
                && (vcsRevision == null || vcsRevision
                    .equals(other.vcsRevision));
        }

        @Override
        public int hashCode() {
            // TODO Auto-generated method stub
            return super.hashCode();
        }

        @Override
        public String toString() {
            return "FLD> " + checksum + " " + vcsRevision;
        }
    }

    public String getVCSRevision(IPath path) {
        FileListData fileListData = data.get(path);
        if (fileListData == null)
            return null;
        return fileListData.vcsRevision;
    }

    // TODO ndh Why would this have to be Serializable again?
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
        addMembers(container.members(), true);
    }

    /**
     * Creates a new file list from given resources.
     * 
     * @param resources
     *            the resources that should be added to this file list.
     * @throws CoreException
     */
    public FileList(IResource[] resources) throws CoreException {

        addMembers(resources, true);
    }

    /**
     * Creates a new file list from given paths. Don't compute checksums and
     * location information.
     * 
     * @param paths
     *            The paths that should be added to this file list.
     */
    public FileList(List<IPath> paths) {
        if (paths != null) {
            for (IPath path : paths) {
                this.data.put(path, null);
            }
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
        return sorted(this.data.keySet());
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
        result = prime * result + ((data == null) ? 0 : data.hashCode());
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
        return this.data.equals(other.data);
    }

    @Override
    public String toString() {
        return "FileList(files:" + this.data.size() + ")";
    }

    protected List<IPath> sorted(Set<IPath> pathSet) {
        List<IPath> paths = new ArrayList<IPath>(pathSet);
        Collections.sort(paths, new PathLengthComparator());
        return paths;
    }

    private void addMembers(IResource[] resources, boolean ignoreDerived)
        throws CoreException {

        if (resources.length == 0)
            return;
        IProject project = resources[0].getProject();
        VCSAdapter vcs = VCSAdapterFactory.getAdapter(project);
        if (vcs != null) {
            String providerID = vcs.getProviderID(project);
            assert providerID != null;
            String repository = vcs.getRepositoryString(project);
            assert repository != null;
            String projectPath = vcs.getProjectPath(project);
            assert projectPath != null;

            this.vcsProviderID = providerID;
            this.vcsRepository = repository;
            this.vcsProjectRoot = projectPath;
            this.vcsBaseRevision = vcs.getRevisionString(project);
        }

        boolean isManagedProject = vcs != null;
        for (IResource resource : resources) {
            if (ignoreDerived && resource.isDerived()) {
                continue;
            }

            assert project.equals(resource.getProject());

            if (resource instanceof IFile) {
                IFile file = (IFile) resource;
                if (!file.exists()) {
                    continue;
                }

                try {
                    FileListData data = new FileListData();
                    data.checksum = FileUtil.checksum(file);

                    if (isManagedProject)
                        addVCSInformation(resource, data, vcs);

                    this.data.put(file.getProjectRelativePath(), data);
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
                    if (!addVCSInformation(resource, data, vcs))
                        data = null;
                }
                this.data.put(path, data);
                addMembers(folder.members(), ignoreDerived);
            }
        }
    }

    private boolean addVCSInformation(IResource resource, FileListData data,
        VCSAdapter vcs) {
        String revision = vcs.getRevisionString(resource);
        if (revision == null)
            return false;

        assert this.vcsProviderID != null
            && this.vcsProviderID.equals(vcs.getProviderID(resource));
        assert this.vcsRepository != null
            && this.vcsRepository.equals(vcs.getRepositoryString(resource));
        // TODO ndh: Only add vcs information to tell the client that an update
        // might be necessary, because this file's revision differs from the
        // project revision.
        data.vcsRevision = revision;
        return true;
    }

    // FIXME ndh remove/fix tests
    public FileListDiff diff(FileList other) {
        return FileListDiff.diff(this, other);
    }

    // TODO ndh error handling
    public long getChecksum(IPath path) {
        FileListData fileListData = this.data.get(path);
        return fileListData != null ? fileListData.checksum : -1;
    }
}
