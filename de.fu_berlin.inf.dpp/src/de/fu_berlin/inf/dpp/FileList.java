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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.util.FileUtil;

/**
 * A FileList is a list of resources - files and folders - which can be compared
 * to other file lists. Folders are denoted by a trailing separator.
 * 
 * @author rdjemili
 */
public class FileList {
    private Map<IPath, Long> all = new HashMap<IPath, Long>();

    private final Map<IPath, Long> added = new HashMap<IPath, Long>();

    private final Map<IPath, Long> removed = new HashMap<IPath, Long>();

    private final Map<IPath, Long> altered = new HashMap<IPath, Long>();

    private final Map<IPath, Long> unaltered = new HashMap<IPath, Long>();

    private final Comparator<IPath> comparator = new PathLengthComprarator();

    private class PathLengthComprarator implements Comparator<IPath> {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator
         */
        public int compare(IPath p1, IPath p2) {
            int l1 = p1.toString().length();
            int l2 = p2.toString().length();

            if (l1 < l2) {
                return -1;
            } else if (l1 > l2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Creates an empty file list.
     */
    public FileList() {
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
     */
    public FileList(IContainer container) throws CoreException {
        container.refreshLocal(IResource.DEPTH_INFINITE, null);
        addMembers(container.members(), this.all, true);
        this.unaltered.putAll(this.all);
    }

    /**
     * Creates a new file list from the file tree in given container.
     * 
     * @param container
     *            the resource container that should be represented by the new
     *            file list.
     * @param ignoreDerived
     *            <code>true</code> if derived resources should be ignored.
     * @throws CoreException
     *             exception that might happen while fetching the files from the
     *             given container.
     */
    public FileList(IContainer container, boolean ignoreDerived)
            throws CoreException {

        container.refreshLocal(IResource.DEPTH_INFINITE, null);
        addMembers(container.members(), this.all, ignoreDerived);
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

        addMembers(resources, this.all, false);
        this.unaltered.putAll(this.all);
    }

    /**
     * Build the FileList from its XML representation.
     * 
     * @throws XmlPullParserException
     * @throws IOException
     */
    public FileList(String xml) throws XmlPullParserException, IOException {
        MXParser parser = new MXParser();
        parser.setInput(new StringReader(xml));

        Map<IPath, Long> context = this.added;

        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {

                if (parser.getName().equals("added")) {
                    context = this.added;

                } else if (parser.getName().equals("removed")) {
                    context = this.removed;

                } else if (parser.getName().equals("altered")) {
                    context = this.altered;

                } else if (parser.getName().equals("unaltered")) {
                    context = this.unaltered;

                } else if (parser.getName().equals("file")) {
                    IPath path = new Path(parser
                            .getAttributeValue(null, "path"));
                    Long checksum = Long.parseLong(parser.getAttributeValue(
                            null, "checksum"));

                    context.put(path, checksum);

                    if (context != this.removed) {
                        this.all.put(path, checksum);
                    }

                } else if (parser.getName().equals("folder")) {
                    IPath path = new Path(parser
                            .getAttributeValue(null, "path"));

                    context.put(path, null);

                    if (context != this.removed) {
                        this.all.put(path, null);
                    }
                }

            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("filelist")) {
                    done = true;
                }
            }
        }
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
        FileList fileList = new FileList();

        for (Map.Entry<IPath, Long> entry : this.all.entrySet()) {
            if (!other.all.containsKey(entry.getKey())) {
                fileList.removed.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<IPath, Long> entry : other.all.entrySet()) {
            if (!this.all.containsKey(entry.getKey())) {
                fileList.added.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<IPath, Long> entry : this.all.entrySet()) {
            IPath path = entry.getKey();
            if (other.all.containsKey(path)) {

                if (path.hasTrailingSeparator()) {
                    fileList.unaltered.put(path, null);

                } else {
                    long checksum = entry.getValue();
                    long otherChecksum = other.all.get(path);

                    if (checksum == otherChecksum) {
                        fileList.unaltered.put(path, checksum);
                    } else {
                        fileList.altered.put(path, checksum);
                    }
                }

            }
        }

        fileList.all = new HashMap<IPath, Long>(other.all);
        return fileList;
    }

    /**
     * @return the amount in percentage by which this file list has the same
     *         files as the other filelist.
     */
    public int match(FileList other) {
        return getPaths().size() == 0 ? 0 : 100
                * diff(other).getUnalteredPaths().size() / getPaths().size();
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
     * @return the XML representation of this FileList. You can use the returned
     *         string to construct the same file list again.
     */
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<filelist>");

        appendFileGroup(sb, "added", this.added);
        appendFileGroup(sb, "removed", this.removed);
        appendFileGroup(sb, "altered", this.altered);
        appendFileGroup(sb, "unaltered", this.unaltered);

        sb.append("</filelist>");

        return sb.toString();
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
        return this.all.equals(other.all) && this.added.equals(other.added)
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
        Collections.sort(paths, this.comparator);
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
                if (file.exists() == false) {
                    continue;
                }

                Long checksum = FileUtil.checksum(file);
                if (checksum != -1) {
                    members.put(file.getProjectRelativePath(), checksum);
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

    private void appendFileGroup(StringBuilder sb, String element,
            Map<IPath, Long> map) {

        if (map.size() == 0) {
            return;
        }

        sb.append('<').append(element).append('>');
        for (Map.Entry<IPath, Long> entry : map.entrySet()) {
            IPath path = entry.getKey();

            if (path.hasTrailingSeparator()) {
                sb.append("<folder path=\"").append(path).append("\"/>");

            } else {
                long checksum = entry.getValue();
                sb.append("<file path=\"").append(path).append("\" ");
                sb.append("checksum=\"").append(checksum).append("\"/>");
            }

        }
        sb.append("</").append(element).append('>');
    }
}
