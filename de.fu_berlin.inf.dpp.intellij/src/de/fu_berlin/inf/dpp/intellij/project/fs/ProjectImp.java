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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectImp implements IProject {
    public static final String DEFAULT_CHARSET = "UTF-8";
    private String defaultCharset = DEFAULT_CHARSET;
    private String name;
    private File path;

    //FIXME: Fix concurrency issues.
    private Map<IPath, IResource> resourceMap = new HashMap<IPath, IResource>();
    private Map<String, IFile> fileMap = new HashMap<String, IFile>();
    private Map<String, IFolder> folderMap = new HashMap<String, IFolder>();

    private boolean isOpen;
    private IPath fullPath;
    private IPath relativePath;
    private IContainer parent;
    private boolean isAccessible;
    private IResourceAttributes attributes;

    public ProjectImp(String name) {
        this.name = name;
    }

    public ProjectImp(String name, File path) {
        this.name = name;
        setPath(path);

        //scan(path);
    }

    public void setPath(File path) {
        this.path = path;

        isAccessible = false;

        fullPath = new PathImp(path.getAbsolutePath());
        relativePath = new PathImp(path.getPath());

        attributes = new ResourceAttributes(); //todo
    }

    public void scan(File path) {
        //clear old
        resourceMap.clear();
        fileMap.clear();
        folderMap.clear();
        if (!path.exists()) {
            path.mkdirs();
        } else {
            addRecursive(path);
        }

        isAccessible = true;
    }

    protected void addRecursive(File file) {
        if (file.isDirectory()) {
            for (File myFile : file.listFiles()) {
                addRecursive(myFile);
            }
        } else {
            addFile(file);
        }
    }

    @Override
    public IResource findMember(IPath path) {

        return resourceMap.get(path);
    }

    protected void addResource(IResource res) {
        resourceMap.put(res.getProjectRelativePath(), res);
    }

    protected void addResource(IFile file) {
        addResource((IResource) file);
        //String key = file.getProjectRelativePath().toString();
        String key = file.getFullPath().toString();

        fileMap.put(key, file);
    }

    protected void addResource(IFolder folder) {
        addResource((IResource) folder);
        // String key = folder.getProjectRelativePath().toString();
        String key = folder.getFullPath().toString();
        folderMap.put(key, folder);
    }

    public void addFile(File file) {
        if (file.isDirectory()) {
            IFolder folder = new FolderImp(this, file);
            addResource(folder);
        }

        if (file.isFile()) {
            IFile myFile = new FileImp(this, file);
            addResource(myFile);
        }
    }

    public void removeFile(File file) {
        if (file.isDirectory()) {
            IFolder folder = new FolderImp(this, file);
            folderMap.remove(folder);
        }

        if (file.isFile()) {
            IFile myFile = new FileImp(this, file);
            resourceMap.remove(myFile.getProjectRelativePath());
        }
    }

    @Override
    public IFile getFile(String name) {
        final IFile file = fileMap.get(name);
        if (file != null) {
            return file;
        }

        if (path.isAbsolute()) {
            return new FileImp(this, new File(name));
        } else {
            return new FileImp(this, new File(this.path + "/" + name));
        }
    }

    @Override
    public IFile getFile(IPath path) {
        return getFile(path.toPortableString());
    }

    @Override
    public IFolder getFolder(String name) {
        final IFolder folder = folderMap.get(name);
        if (folder != null) {
            return folder;
        }

        return new FolderImp(this, new File(name));
    }

    @Override
    public IFolder getFolder(IPath path) {
        return getFolder(path.toPortableString());
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void open() throws IOException {
        this.isOpen = true;
    }

    @Override
    public boolean exists(IPath path) {
        return resourceMap.containsKey(path);
    }

    @Override
    public IResource[] members() {
        return resourceMap.values().toArray(new IResource[] { });
    }

    @Override
    public IResource[] members(int memberFlags) {
        List<IResource> list = new ArrayList<IResource>();
        for (IResource res : resourceMap.values()) {
            if (memberFlags == FOLDER && res.getType() == FOLDER) {
                list.add(res);
            } else if (memberFlags == FILE && res.getType() == FILE) {
                list.add(res);
            } else if (memberFlags == NONE) {
                list.add(res);
            }
        }

        return list.toArray(new IResource[] { });

    }

    @Override
    public String getDefaultCharset() throws IOException {
        return defaultCharset;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Override
    public boolean exists() {
        return fullPath.toFile().exists();
    }

    @Override
    public IPath getFullPath() {
        return fullPath;
    }

    public void setFullPath(IPath fullPath) {
        this.fullPath = fullPath;
    }

    public void setRelativePath(IPath relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public IContainer getParent() {
        return parent;
    }

    public void setParent(IContainer parent) {
        this.parent = parent;
    }

    @Override
    public IProject getProject() {
        return this;
    }

    @Override
    public IPath getProjectRelativePath() {
        return this.relativePath;
    }

    @Override
    public int getType() {
        return IResource.PROJECT;
    }

    @Override
    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    @Override
    public boolean isDerived(boolean checkAncestors) {
        return isDerived();
    }

    @Override
    public boolean isDerived() {
        return parent != null;
    }

    @Override
    public void refreshLocal() throws IOException {
        scan(path);
    }

    @Override
    public void delete(int updateFlags) throws IOException {
        FileUtils.deleteDirectory(path);
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException {
        path.renameTo(destination.toFile());
    }

    @Override
    public IResourceAttributes getResourceAttributes() {
        return attributes;
    }

    @Override
    public void setResourceAttributes(IResourceAttributes attributes)
        throws IOException {
        this.attributes = attributes;
    }

    @Override
    public URI getLocationURI() {
        try {
            return new URI(fullPath.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        if (clazz.isInstance(this)) {
            return this;
        }

        return null;
    }

    public IPath getLocation() {
        return this.fullPath;
    }

    public File toFile() {
        return path;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder(" ");
        if (name != null) {
            sb.append("name=[");
            sb.append(name);
            sb.append("]");
        }

        if (path != null) {
            sb.append(" path=[");
            sb.append(path);
            sb.append("]");
        }

        return getClass().getName() + sb;
    }

}
