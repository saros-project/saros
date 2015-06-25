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

package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntelliJProjectImpl implements IProject {
    public static final String DEFAULT_CHARSET = "utf8";

    public static final String DEFAULT_MODULE_EXTENSION = ".iml";
    private static final Logger LOG = Logger.getLogger(IntelliJProjectImpl.class);

    private String defaultCharset = DEFAULT_CHARSET;

    private Project project;
    private String name;
    private File path;

    //FIXME Replace by handles
    private Map<IPath, IResource> resourceMap = new HashMap<IPath, IResource>();
    private Map<String, IFile> fileMap = new HashMap<String, IFile>();
    private Map<String, IFolder> folderMap = new HashMap<String, IFolder>();

    private boolean isOpen;
    private IPath fullPath;
    private IPath relativePath;
    private IContainer parent;
    private boolean isAccessible;
    private IResourceAttributes attributes;

    public IntelliJProjectImpl(Project project, String name) {

        File path = new File(project.getBasePath() + File.separator + name
        );

        this.project = project;
        this.name = name;
        setPath(path);
        scan(path);
    }

    public IntelliJProjectImpl(Project project, String name, File path) {
        this.project = project;
        this.name = name;
        setPath(path);

        scan(path);
    }

    public void setPath(File path) {
        this.path = path;

        isAccessible = false;

        fullPath = new IntelliJPathImpl(path.getAbsolutePath());
        relativePath = new IntelliJPathImpl(path.getPath());

        attributes = new IntelliJResourceAttributesImpl(); //todo
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
            for (File myFile : getSafeFileList(file)) {
                addRecursive(myFile);
            }
        } else {
            addFile(file);
        }
    }

    private File[] getSafeFileList(File file) {
        File[] files = file.listFiles();
        return files != null ? files : new File[0];
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
        String key = file.getFullPath().toString();

        fileMap.put(key, file);
    }

    protected void addResource(IFolder folder) {
        addResource((IResource) folder);
        String key = folder.getFullPath().toString();
        folderMap.put(key, folder);
    }

    public void addFile(File file) {
        if (file.isDirectory()) {
            IFolder folder = new IntelliJFolderImpl(this, file);
            addResource(folder);
        }

        if (file.isFile()) {
            IFile myFile = new IntelliJFileImpl(this, file);
            addResource(myFile);
        }
    }

    public void removeFile(File file) {
        if (file.isDirectory()) {
            IFolder folder = new IntelliJFolderImpl(this, file);
            folderMap.remove(folder);
        }

        if (file.isFile()) {
            IFile myFile = new IntelliJFileImpl(this, file);
            resourceMap.remove(myFile.getProjectRelativePath());
        }
    }

    @Override
    public IFile getFile(String name) {
        if (fileMap.containsKey(name)) {
            return fileMap.get(name);
        }

        IFile file;
        if (path.isAbsolute()) {
            file = new IntelliJFileImpl(this, new File(name));

        } else {
            file = new IntelliJFileImpl(this,
                new File(this.path + File.separator + name));
        }
        addResource(file);
        return file;
    }

    @Override
    public IFile getFile(IPath path) {
        return getFile(path.toPortableString());
    }

    @Override
    public IFolder getFolder(String name) {
        if (folderMap.containsKey(name)) {
            return folderMap.get(name);
        } else {
            return new IntelliJFolderImpl(this, new File(name));
        }
    }

    @Override
    public IFolder getFolder(IPath path) {
        return getFolder(path.toPortableString());
    }

    @Override
    public boolean isOpen() {
        if (ModuleManager.getInstance(project) != null) {
            return ApplicationManager.getApplication()
                .runReadAction(new Computable<Boolean>() {

                        @Override
                        public Boolean compute() {
                            Module mod = ModuleManager.getInstance(project)
                                .findModuleByName(name);
                            return mod != null && mod.isLoaded();
                        }
                    }
                );

        } else {
            return isOpen;
        }
    }

    @Override
    public void open() throws IOException {
        if (ModuleManager.getInstance(project) != null) {

            //this is only called when the .iml file already exists on disk (after IPN)
            //TODO: Does not work with projects shared from Eclipse, would need
            //independent metadata for that
            LoadModuleRunnable loader = new LoadModuleRunnable();
            ApplicationManager.getApplication().invokeLater(loader);
            if (loader.getException() != null) {
                throw loader.getException();
            }

        } else {
            if (!getFullPath().toFile().mkdirs()) {
                LOG.error("Could not open project: " + getName());
                throw new IOException("Could not open project");
            }
            isOpen = true;
        }
    }

    public void create() throws IOException {
        if (!exists()) {
            if (!getFullPath().toFile().mkdirs()) {
                LOG.error("Could not open project: " + getName());
                throw new IOException("Could not open project");
            }
        }
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

    private class LoadModuleRunnable implements Runnable {

        public IOException getException() {
            return exception;
        }

        private IOException exception = null;

        @Override
        public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    final String projectFile =
                        path + File.separator + name + DEFAULT_MODULE_EXTENSION;
                    try {
                        List<File> refreshList = new ArrayList<File>();
                        refreshList.add(new File(projectFile));
                        LocalFileSystem.getInstance()
                            .refreshIoFiles(refreshList);
                        ModuleManager.getInstance(project)
                            .loadModule(projectFile);
                    } catch (InvalidDataException e) {
                        exception = new IOException(
                            "invalid data in project file " + projectFile, e);
                    } catch (JDOMException e) {
                        exception = new IOException(
                            "invalid data in project file " + projectFile, e);
                    } catch (ModuleWithNameAlreadyExists e) {
                        exception = new IOException(
                            "module with name already exists for file "
                                + projectFile, e);
                    } catch (IOException e) {
                        exception = e;
                    }
                }
            });
        }
    }

}
