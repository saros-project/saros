package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NOTE: A project is something different in Eclipse and in IntelliJ
 * Eventhough this is called ProjectImpl, it is actually used to represent
 * a IntelliJ module, not a project (because of historical reasons)
 * <p/>
 * NOTE: An IntelliJ project is a module too. So it is valid to call
 * this class with a project. There is a difference in behavior:
 * 1) If called with a project: project contains the project path, including
 * the project name, and name is again the project name
 * 2) If called with a module: project contains the path of the parent
 * project, and name is the module name
 */
public class IntelliJProjectImpl implements IProject {
    public static final String DEFAULT_CHARSET = "utf8";

    private static final Logger LOG = Logger
        .getLogger(IntelliJProjectImpl.class);

    private String defaultCharset = DEFAULT_CHARSET;

    private String name;
    private File path;

    //FIXME Replace by handles
    private Map<IPath, IResource> resourceMap = new HashMap<IPath, IResource>();
    private Map<String, IFile> fileMap = new HashMap<String, IFile>();
    private Map<String, IFolder> folderMap = new HashMap<String, IFolder>();

    private IPath fullPath;
    private IPath relativePath;
    private IContainer parent;

    public IntelliJProjectImpl(Project project) {

        if (project == null) {
            throw new NullPointerException(
                "project is null");
        }

        /*
         * Case 1) Called for a project
         * If this is called for the actual project which is a module too,
         * then the project path already includes the name, so there is no need
         * to add it.

         */
        this.name = project.getName();
        setPath(new File(project.getBasePath()));
        scan();
    }

    /**
     * Creates a core compatible IProject using the given IntelliJ project and
     * a specific module.
     * <b>Note:</b> Only top level modules are supported. E.g modules inside
     * modules cannot be represented.
     * @param project
     * @param moduleName
     */
    public IntelliJProjectImpl(Project project, String moduleName) {

        if (project == null) {
            throw new NullPointerException(
                "project is null");
        }

        if (moduleName == null) {
            throw new NullPointerException(
                "moduleName is null");
        }

        /*
         * Case 2) Called for a module
         * If this is called for a module, the module path is the project
         * path with the module name added in the end.
         *
         */
        if (project.getName().equals(moduleName))
            throw new IllegalArgumentException("moduleName cannot be equal to the project name it belongs to");

        this.name = moduleName;
        setPath(new File(project.getBasePath(), moduleName));
        scan();
    }

    private void setPath(File path) {
        this.path = path;

        fullPath = IntelliJPathImpl.fromString(path.getAbsolutePath());
        relativePath = IntelliJPathImpl.fromString(path.getPath());
    }

    private void scan() {
        //clear old
        resourceMap.clear();
        fileMap.clear();
        folderMap.clear();
        if (!path.exists()) {
            LOG.warn(
                "Tries to scan a file that doesn't exist: " + path.toString());
        } else {
            addRecursive(path);
        }

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
        String key = file.getProjectRelativePath().toString();

        fileMap.put(key, file);
    }

    protected void addResource(IFolder folder) {
        addResource((IResource) folder);
        String key = folder.getProjectRelativePath().toString();
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

    /**
     * Removes the resource with the given path from the project mappings.
     *
     * @param resourcePath relative path to the resource
     */
    public void removeResource(IPath resourcePath){
        IResource resource = resourceMap.remove(resourcePath);
        if(resource == null) {
            return;
        }
        String key = resourcePath.toString();
        if(resource.getType() == IResource.FILE) {
            fileMap.remove(key);
        }else if(resource.getType()==IResource.FOLDER) {
            folderMap.remove(key);
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
    public boolean exists(IPath path) {
        return resourceMap.containsKey(path);
    }

    /**
     * Returns <b>true</b> if the resource the passed path is pointing to
     * belongs to this project, <b>false</b> otherwise.
     *
     * @param resourcePath path to the resource
     * @return
     */
    public boolean isMember(IPath resourcePath) {
        if(resourceMap.containsKey(resourcePath)){
            return true;
        }

        if(fullPath.isPrefixOf(resourcePath)){
            return resourceMap.containsKey(
                resourcePath.removeFirstSegments(fullPath.segmentCount()));
        }

        return false;
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
    public boolean isDerived(boolean checkAncestors) {
        return isDerived();
    }

    @Override
    public boolean isDerived() {
        return parent != null;
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
    public Object getAdapter(Class<? extends IResource> clazz) {
        if (clazz.isInstance(this)) {
            return this;
        }

        return null;
    }

    @Override
    public IPath getLocation() {
        return this.fullPath;
    }

    public File toFile() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntelliJProjectImpl that = (IntelliJProjectImpl) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!FileUtil.filesEqual(path, that.path)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + FileUtil.fileHashCode(path);
        return result;
    }

    @Override
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

    @Override
    public IReferencePoint getReferencePoint()
    {
        return null;
    }
}
