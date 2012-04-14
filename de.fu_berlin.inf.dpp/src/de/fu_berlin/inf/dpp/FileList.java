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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * A FileList is a list of resources - files and folders - which belong to the
 * same {@link IProject}. FileLists can be compared to other FileLists. Folders
 * are denoted by a trailing separator. Instances of this class are immutable.
 * No further modification is allowed after creation.<br>
 * <br>
 * NOTE: As computation of a FileList involves a complete rescan of the project,
 * creating new instances should be avoided.
 * 
 * @author rdjemili
 */
@XStreamAlias("fileList")
public class FileList {

    private static final XStream XSTREAM;

    static {
        XStream xstream = new XStream();
        xstream.registerConverter(BooleanConverter.BINARY);
        xstream.processAnnotations(FileList.class);
        XSTREAM = xstream;
    }

    private static final Logger log = Logger.getLogger(FileList.class);

    @XStreamAlias("f")
    private static class File {

        @XStreamAlias("p")
        @XStreamAsAttribute
        String path;

        @XStreamAlias("m")
        MetaData metaData;

        @XStreamAlias("l")
        List<File> files;

        @XStreamAlias("d")
        @XStreamAsAttribute
        boolean isDirectory;

        private File(String path, MetaData metaData, boolean isDirectory) {
            this.path = path;
            this.metaData = metaData;
            this.isDirectory = isDirectory;
        }

        /**
         * Converts the content of this file node and its sub nodes to full
         * paths.<br/>
         * 
         * e.g:<br/>
         * 
         * <pre>
         * DIR   FILES
         * a/b/[a,b,c,d] 
         *  -> [a/b/a, a/b/b, a/b/c, a/b/d]
         * </pre>
         * 
         * @return the list containing the full paths
         */
        public List<IPath> toList() {
            List<IPath> paths = new ArrayList<IPath>();
            toList(new Path(path), paths);
            return paths;
        }

        /**
         * Adds all paths elements from the current file node and its sub nodes.
         * 
         * @param path
         *            the path of the parent node
         * @param paths
         *            a list to store the paths
         */
        private void toList(IPath path, List<IPath> paths) {
            if (files == null)
                return;

            for (File file : files) {
                if (file.isDirectory && file.files == null)
                    paths.add(path.append(file.path).addTrailingSeparator());
                else if (!file.isDirectory)
                    paths.add(path.append(file.path));

                file.toList(path.append(file.path), paths);
            }
        }

        public boolean contains(IPath path) {
            return getFile(path) != null;
        }

        public MetaData getMetaData(IPath path) {
            File file = getFile(path);
            return file == null ? null : file.metaData;
        }

        private File getFile(IPath path) {
            if (files == null)
                return null;

            String[] segments = path.segments();

            for (File file : files) {
                File foundFile = file.getFile(segments, 0);
                if (foundFile != null)
                    return foundFile;
            }

            return null;
        }

        private File getFile(String[] segments, int segmentIndex) {
            if (segmentIndex >= segments.length)
                return null;

            // _________ 0 1 2 3 : segment.length = 4
            // _search : a/b/c/d : segmentIndex = 3
            // current : a/b/c/d : return this

            if (path.equals(segments[segmentIndex])
                && segmentIndex + 1 == segments.length) {
                return this;
            }

            /*
             * only continue if we are still on a valid path segment e.g we
             * search for a/b/c/d but are now in a/b/a
             */
            if (!path.equals(segments[segmentIndex])) {
                return null;
            }

            if (files == null)
                return null;

            for (File file : files) {
                File foundFile = file.getFile(segments, segmentIndex + 1);
                if (foundFile != null)
                    return foundFile;
            }
            return null;
        }

        public void addPath(IPath path, MetaData metaData, boolean isDirectory) {
            addPath(path.segments(), 0, metaData, isDirectory);
        }

        private void addPath(String[] segments, int segmentIndex,
            MetaData metaData, boolean isDirectory) {

            if (segmentIndex >= segments.length)
                return;

            if (files == null)
                files = new ArrayList<File>();

            for (File file : files) {
                if (file.path.equals(segments[segmentIndex])) {
                    if (segmentIndex + 1 == segments.length) {
                        file.metaData = metaData;
                        file.isDirectory = isDirectory;
                        return;
                    } else {
                        file.addPath(segments, segmentIndex + 1, metaData,
                            isDirectory);
                        return;
                    }
                }
            }

            if (segmentIndex + 1 == segments.length) {
                files.add(new File(segments[segmentIndex], metaData,
                    isDirectory));
                return;
            }

            File file = new File(segments[segmentIndex], null, true);
            files.add(file);
            file.addPath(segments, segmentIndex + 1, metaData, isDirectory);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((files == null) ? 0 : files.hashCode());
            result = prime * result + (isDirectory ? 1231 : 1237);
            result = prime * result
                + ((metaData == null) ? 0 : metaData.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            File other = (File) obj;

            if (isDirectory != other.isDirectory)
                return false;

            if (path == null) {
                if (other.path != null)
                    return false;
            } else if (!path.equals(other.path))
                return false;

            if (metaData == null) {
                if (other.metaData != null)
                    return false;
            } else if (!metaData.equals(other.metaData))
                return false;

            if (files == null) {
                if (other.files != null)
                    return false;
            } else if (!files.equals(other.files))
                return false;

            return true;
        }
    }

    @XStreamAlias("md")
    static class MetaData {
        /** Checksum of this file. */

        @XStreamAlias("crc")
        long checksum;

        /** Identifies the version of this file in the repository. */

        @XStreamAlias("vcs")
        VCSResourceInfo vcsInfo;

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (o == null)
                return false;

            if (!(o instanceof MetaData))
                return false;

            if (checksum != ((MetaData) o).checksum)
                return false;

            if (vcsInfo == null && ((MetaData) o).vcsInfo == null)
                return true;

            if (vcsInfo == null)
                return false;

            return vcsInfo.equals(((MetaData) o).vcsInfo);
        }

        @Override
        public int hashCode() {
            return (int) checksum;
        }

        @Override
        public String toString() {
            return "[Checksum: 0x" + Long.toHexString(checksum).toUpperCase()
                + ", VCS: " + vcsInfo + "]";
        }
    }

    private final boolean useVersionControl;

    /** Identifies the VCS used. */
    private String vcsProviderID;

    /** @see VCSAdapter#getRepositoryString(IResource) */
    private String vcsRepositoryRoot;
    /** VCS internal information. */
    private VCSResourceInfo vcsProjectInfo;

    /** ID of Project this list of files belong to */
    private String projectID;

    private File root = new File("", null, true);

    MetaData getMetaData(IPath path) {
        return root.getMetaData(path);
    }

    public String getVCSRevision(IPath path) {

        if (path.isEmpty())
            return vcsProjectInfo.revision;

        MetaData metaData = root.getMetaData(path);

        if (metaData == null)
            return null;

        return metaData.vcsInfo == null ? null : metaData.vcsInfo.revision;
    }

    public String getVCSUrl(IPath path) {
        if (path.isEmpty())
            return vcsProjectInfo.url;

        MetaData metaData = root.getMetaData(path);

        if (metaData == null)
            return null;

        return metaData.vcsInfo == null ? null : metaData.vcsInfo.url;
    }

    /**
     * Creates an empty file list.
     */
    FileList() {
        this(true);
    }

    /**
     * Creates an empty file list.
     * 
     * @param useVersionControl
     *            If false, the FileList won't include version control
     *            information.
     */
    FileList(boolean useVersionControl) {
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
     */
    FileList(IContainer container, IChecksumCache checksumCache,
        boolean useVersionControl, IProgressMonitor monitor)
        throws CoreException {
        this(useVersionControl);
        container.refreshLocal(IResource.DEPTH_INFINITE, null);
        addMembers(Arrays.asList(container.members()), checksumCache, monitor);
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
    FileList(List<IResource> resources, IChecksumCache checksumCache,
        boolean useVersionControl, IProgressMonitor monitor)
        throws CoreException {
        this(useVersionControl);
        addMembers(resources, checksumCache, monitor);
    }

    /**
     * Creates a new file list from given paths. It does not compute checksums
     * or location information.
     * 
     * @NOTE This method does not check the input. The caller is
     *       <b>responsible</b> for the <b>correct</b> input !
     * 
     * @param paths
     *            a list of paths that <b>refers</b> to <b>files</b> that should
     *            be added to this file list.
     */
    FileList(List<IPath> paths) {
        this(false);

        if (paths == null)
            throw new NullPointerException("path list must not be null");

        for (IPath path : paths)
            root.addPath(path, null, false);
    }

    /**
     * Calculate an approximation of how equal <code>this</code> file list is to
     * the <code>other</code> file list.
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

        if (nPaths == 0) // other is empty
            return 0;

        FileListDiff difference = FileListDiff.diff(this, other);
        int nUnalteredPaths = difference.getUnalteredPaths().size();

        return (int) ((nUnalteredPaths * 100L) / nPaths);
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
            return computeMatch(FileListFactory.createFileList(project, null,
                null, useVersionControl, null));
        } catch (CoreException e) {
            log.error(
                "failed to generate file list for project: "
                    + project.getName(), e);
        }
        return 0;
    }

    @XStreamOmitField
    private List<IPath> cachedList = null;

    /**
     * @return a sorted list of all paths in this file list. The paths are
     *         sorted by their character length.
     */
    public List<IPath> getPaths() {
        if (cachedList != null)
            return cachedList;

        cachedList = root.toList();
        return cachedList;
    }

    /**
     * @return the XML representation of this FileList.
     * 
     * @see #fromXML(String)
     */
    public String toXML() {
        StringWriter writer = new StringWriter(512 * 1024);
        XSTREAM.marshal(this, new CompactWriter(writer));
        return writer.toString();
    }

    /**
     * Parses given XML and returns a {@link FileList} instance.
     * 
     * @see FileList#toXML()
     */
    public static FileList fromXML(String xml) {
        return (FileList) XSTREAM.fromXML(xml);
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof FileList))
            return false;

        return root.equals(((FileList) o).root);
    }

    private void addMembers(List<IResource> resources,
        IChecksumCache checksumCache, IProgressMonitor monitor)
        throws CoreException {

        if (resources.size() == 0)
            return;

        if (monitor == null)
            monitor = new NullProgressMonitor();

        if (resources.size() == 0)
            return;

        IProject project = null;
        VCSAdapter vcs = null;

        if (useVersionControl) {
            project = resources.get(0).getProject();
            vcs = VCSAdapter.getAdapter(project);

            if (vcs != null) {
                String providerID = vcs.getProviderID(project);

                vcsProviderID = providerID;
                vcsRepositoryRoot = vcs.getRepositoryString(project);
                vcsProjectInfo = vcs.getCurrentResourceInfo(project);
                /*
                 * FIXME we need to stop querying for VCS revisions the moment
                 * we reach the first exception
                 * 
                 * Caused by:
                 * org.tigris.subversion.svnclientadapter.SVNClientException:
                 * org.apache.subversion.javahl.ClientException: The working
                 * copy needs to be upgraded
                 * 
                 * which will significantly slow down the overall invitation
                 * process. It doesn't make sense to check for other files. If
                 * there is one resource that is not upgraded, this fails
                 * overall...
                 */
            }
        }

        Deque<IResource> stack = new LinkedList<IResource>();

        stack.addAll(resources);

        List<IFile> files = new LinkedList<IFile>();

        monitor.subTask("Reading SVN revisions for shared files...");
        while (!stack.isEmpty()) {
            IResource resource = stack.pop();

            if (resource.isDerived() || !resource.exists())
                continue;

            IPath path = resource.getProjectRelativePath();

            if (root.contains(path))
                continue;

            VCSResourceInfo info = null;

            if (vcs != null)
                info = vcs.getCurrentResourceInfo(resource);

            assert !useVersionControl
                || (project != null && project.equals(resource.getProject()));

            MetaData data = null;

            switch (resource.getType()) {
            case IResource.FILE:
                files.add((IFile) resource);
                data = new MetaData();
                data.vcsInfo = info;
                root.addPath(path, data, false);
                break;
            case IResource.FOLDER:
                stack.addAll(Arrays.asList(((IFolder) resource).members()));

                if (info != null) {
                    data = new MetaData();
                    data.vcsInfo = info;
                }
                root.addPath(path, data, true);
                break;
            }
        }

        monitor.beginTask("Calculating checksums...", files.size());

        for (IFile file : files) {
            try {
                monitor.subTask(file.getProject().getName() + ": "
                    + file.getName());

                MetaData data = root.getMetaData(file.getProjectRelativePath());

                Long checksum = null;

                /** {@link IChecksumCache} **/
                String path = file.getFullPath().toPortableString();

                if (checksumCache != null)
                    checksum = checksumCache.getChecksum(path);

                data.checksum = checksum == null ? FileUtils.checksum(file)
                    : checksum;

                if (checksumCache != null) {
                    boolean isInvalid = checksumCache.addChecksum(path,
                        data.checksum);

                    if (isInvalid && checksum != null)
                        log.warn("calculated checksum on dirty data: "
                            + file.getFullPath());
                }

            } catch (IOException e) {
                log.error(e);
            }

            monitor.worked(1);
        }
    }

    public FileListDiff diff(FileList other) {
        return FileListDiff.diff(this, other);
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

    @XStreamOmitField
    private String toString;

    @Override
    public String toString() {
        if (toString != null)
            return toString;

        List<String> paths = new ArrayList<String>();

        for (IPath path : getPaths())
            paths.add(path.toString());

        Collections.sort(paths);
        toString = Arrays.toString(paths.toArray());
        return toString;
    }
}
