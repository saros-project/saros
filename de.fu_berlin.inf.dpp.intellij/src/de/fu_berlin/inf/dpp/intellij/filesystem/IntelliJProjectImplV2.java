package de.fu_berlin.inf.dpp.intellij.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ProjectFileIndexFacade;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import de.fu_berlin.inf.dpp.exceptions.ModuleNotFoundException;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;

public final class IntelliJProjectImplV2 extends IntelliJResourceImplV2
    implements IProject {

    /*
     * Used to identify module stubs that were created during the project
     * negotiation and have to be reloaded after the correct module file was
     * transferred by the host.
     *
     * Used in AddProjectToSessionWizard#createModuleStub(String) and
     * ModuleInitialization.ModuleReloader#run()
     */
    public static final String RELOAD_STUB_MODULE_TYPE =
        "SAROS_RELOAD_STUB_MODULE";

    private static final Logger LOG = Logger
        .getLogger(IntelliJProjectImplV2.class);

    // Module names are unique (even among different projects)
    private final String moduleName;

    private volatile Module module;

    private final VirtualFile moduleRoot;

    /**
     * Creates a core compatible {@link IProject project} using the given
     * IntelliJ module.
     * <p>
     * <b>Note:</b> Only top level modules are fully supported. Modules inside
     * of other modules will be created as top level modules on the receiving
     * side of the session initialization. Inner modules of the shared module
     * will also be transmitted but not registered with IntelliJ as modules.
     * <p>
     * <b>Note:</b> Only modules with exactly one content root are currently
     * supported. IProject objects for modules with fewer or more than one
     * content root can not be created.
     *
     * @param module
     *            an IntelliJ <i>module</i>
     * @throws IllegalArgumentException if the given module does not have
     *                                  exactly one content root
     */
    public IntelliJProjectImplV2(@NotNull final Module module) {
        this.module = module;
        this.moduleName = module.getName();

        moduleRoot = getModuleContentRoot(module);

        VirtualFile moduleRootParent = moduleRoot.getParent();
        VirtualFile projectRoot = module.getProject().getBaseDir();

        if (projectRoot == null) {
            throw new IllegalStateException("Project " + module.getProject() +
                " has no base dir (null).");
        }

        if (moduleRootParent == null || !moduleRootParent.equals(projectRoot)) {

            throw new IllegalArgumentException("content root " + moduleRoot +
                " of module " + module + " is not located in the project " +
                "root " + projectRoot);
        }
    }

    /**
     * Returns the content root of the given module.
     * <p>
     * This method is used to enforce the current restriction that shared
     * modules must contain exactly one content root.
     * </p>
     *
     * @param module the module to get the content root for
     * @return the content root of the given module
     * @throws IllegalArgumentException if the given module does not have
     *                                  exactly one content root
     */
    @NotNull
    private static VirtualFile getModuleContentRoot(
        @NotNull
            Module module) {

        ModuleRootManager moduleRootManager = ModuleRootManager
            .getInstance(module);

        VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

        int numberOfContentRoots = contentRoots.length;

        if (numberOfContentRoots != 1) {
            throw new IllegalArgumentException(
                "Modules shared with Saros currently must contain exactly one "
                    + "content root. The given module " + module + " has "
                    + numberOfContentRoots + " content roots: " + Arrays
                    .toString(contentRoots));
        }

        return contentRoots[0];
    }

    /**
     * Returns the IntelliJ {@link Module module}.
     *
     * @return the IntelliJ module.
     */
    @NotNull
    public Module getModule() {
        return module;
    }

    /**
     * This method can be used to refresh the held <code>Module</code> object in
     * case the module was reloaded.
     * <p>
     * <b>Note:</b> This method should only be needed in special cases as we can
     * not guarantee a graceful handling of a disposed module object.
     * Any classes or methods still holding a reference to the old module object
     * could lead to a failure of the related logic as operations on disposed
     * modules will result in an exception.
     * </p>
     *
     * @return <code>true</code> if the held <code>Module</code> has the status
     *         disposed and is replaced with a new <code>Module</code> object
     *         with the same name, <code>false</code> otherwise
     *
     * @throws ModuleNotFoundException if the old <code>Module</code> object is
     *                                 disposed but no new module with the same
     *                                 name could be found
     */
    public boolean refreshModule() throws ModuleNotFoundException{
        if (module.isDisposed()) {
            Project project = module.getProject();

            Module newModule = ModuleManager.getInstance(project)
                .findModuleByName(moduleName);

            if (newModule == null) {

                throw new ModuleNotFoundException("The module " + moduleName +
                    " could not be refreshed as no module with the same" +
                    " name could be found in the current project " + project);
            }

            module = newModule;

            return true;
        }

        return false;
    }

    /**
     * Returns whether the resource for the given path exists.
     * <p>
     * <b>Note:</b> A derived resource is treated as being nonexistent.
     *
     * @return <code>true</code> if the resource exists and is not derived,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean exists(final IPath path) {
        final VirtualFile file = findVirtualFile(path);

        return file != null && file.exists();
    }

    @NotNull
    @Override
    public IResource[] members() throws IOException {
        // TODO run as read action

        final List<IResource> result = new ArrayList<>();

        final VirtualFile[] children = moduleRoot.getChildren();

        ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance(module)
            .getFileIndex();

        for (final VirtualFile child : children) {

            if (!moduleFileIndex.isInContent(child)) {

                continue;
            }

            final IPath childPath = IntelliJPathImpl
                .fromString(child.getName());

            result.add(child.isDirectory() ? new IntelliJFolderImplV2(this,
                childPath) : new IntelliJFileImplV2(this, childPath));

        }

        return result.toArray(new IResource[result.size()]);
    }

    @NotNull
    @Override
    public IResource[] members(final int memberFlags) throws IOException {
        return members();
    }

    @Nullable
    @Override
    public String getDefaultCharset() throws IOException {
        // TODO retrieve encoding for the module or use the project settings
        return null;
    }

    @Override
    public boolean exists() {
        return !module.isDisposed() && module.isLoaded();
    }

    @NotNull
    @Override
    public IPath getFullPath() {
        return IntelliJPathImpl.fromString(getName());
    }

    @NotNull
    @Override
    public String getName() {
        return moduleName;
    }

    @Nullable
    @Override
    public IContainer getParent() {
        return null;
    }

    @NotNull
    @Override
    public IProject getProject() {
        return this;
    }

    @NotNull
    @Override
    public IPath getProjectRelativePath() {
        return IntelliJPathImpl.EMPTY;
    }

    /**
     * Returns the path to the given file relative to the content root of this
     * module.
     * <p>
     * <b>Note:</b> This methods expects that the given <code>VirtualFile</code>
     * exists.
     * </p>
     *
     *
     * @param file the <code>VirtualFile</code> to get the relative path for
     *
     * @return a relative path for the given file or <code>null</code> if the
     *         file does not belong to this module or there is no
     *         relative path from the content root to the file
     */
    @Nullable
    private IPath getProjectRelativePath(@NotNull VirtualFile file){
        Module fileModule = ProjectFileIndexFacade
            .getInstance(module.getProject()).getModuleForFile(file);

        if(fileModule == null || !moduleName.equals(fileModule.getName())){
            return null;
        }

        try {
            Path relativePath = Paths.get(moduleRoot.getPath())
                .relativize(Paths.get(file.getPath()));

            return IntelliJPathImpl.fromString(relativePath.toString());

        } catch (IllegalArgumentException e) {
            LOG.warn("Could not find a relative path from the content root " +
                moduleRoot + " to the file " + file, e);

            return null;
        }
    }

    @Override
    public int getType() {
        return IResource.PROJECT;
    }

    @Override
    public boolean isDerived(final boolean checkAncestors) {
        return false;
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    @Override
    public void delete(final int updateFlags) throws IOException {
        throw new IOException("delete is not supported");
    }

    @Override
    public void move(final IPath destination, final boolean force)
        throws IOException {
        throw new IOException("move is not supported");
    }

    @NotNull
    @Override
    public IPath getLocation() {
        return IntelliJPathImpl.fromString(moduleRoot.getPath());
    }

    @Nullable
    @Override
    public IResource findMember(final IPath path) {
        final VirtualFile file = findVirtualFile(path);

        if (file == null)
            return null;

        return file.isDirectory() ? new IntelliJFolderImplV2(this, path)
            : new IntelliJFileImplV2(this, path);
    }

    @NotNull
    @Override
    public IFile getFile(final String name) {
        return getFile(IntelliJPathImpl.fromString(name));
    }

    @NotNull
    @Override
    public IFile getFile(final IPath path) {

        if (path.segmentCount() == 0)
            throw new IllegalArgumentException(
                "cannot create file handle for an empty path");

        return new IntelliJFileImplV2(this, path);
    }

    /**
     * Returns an <code>IFile</code> for the given file.
     *
     * @param file the <code>VirtualFile</code> to get the <code>IFile</code>
     *             for
     *
     * @return an <code>IFile</code> for the given file or <code>null</code> if
     *         the given file is a directory, does not exist, or the relative
     *         path of the file could not be constructed
     */
    @Nullable
    public IFile getFile(@NotNull final VirtualFile file){
        if(file.isDirectory() || !file.exists()){
            return null;
        }

        IPath relativePath = getProjectRelativePath(file);

        return relativePath != null ?
            new IntelliJFileImplV2(this, relativePath) :
            null;
    }

    @NotNull
    @Override
    public IFolder getFolder(final String name) {
        return getFolder(IntelliJPathImpl.fromString(name));
    }

    @NotNull
    @Override
    public IFolder getFolder(final IPath path) {

        if (path.segmentCount() == 0)
            throw new IllegalArgumentException(
                "cannot create folder handle for an empty path");

        return new IntelliJFolderImplV2(this, path);
    }

    /**
     * Returns an <code>IFolder</code> for the given file.
     *
     * @param file the <code>VirtualFile</code> to get the <code>IFolder</code>
     *             for
     *
     * @return an <code>IFolder</code> for the given file or <code>null</code>
     *         if the given file is not a directory, does not exist, or the
     *         relative path of the file could not be constructed
     */
    @Nullable
    public IFolder getFolder(@NotNull final VirtualFile file){
        if(!file.isDirectory() || !file.exists()){
            return null;
        }

        IPath relativePath = getProjectRelativePath(file);

        return relativePath != null ?
            new IntelliJFolderImplV2(this, relativePath) :
            null;
    }

    /**
     * Returns an <code>IResource</code> for the given file.
     *
     * @param file the <code>VirtualFile</code> to get the
     *             <code>IResource</code> for
     *
     * @return an <code>IResource</code> for the given file or <code>null</code>
     *         if the given file does not exist or the relative path of the file
     *         could not be constructed
     */
    @Nullable
    public IResource getResource(@NotNull VirtualFile file) {
        if (file.isDirectory()) {
            return getFolder(file);
        } else {
            return getFile(file);
        }
    }

    /**
     * Returns the virtual file for the given path belonging to this module.
     * <p>
     * <b>Note:</b> This method can not return files for derived resources or
     * resources belonging to a sub-module.
     *
     * @param path
     *            relative path to the file
     * @return the virtual file or <code>null</code> if it does not exists in
     *         the VFS snapshot, is derived, belongs to a sub-module, or the
     *         given path is absolute.
     */
    @Nullable
    public VirtualFile findVirtualFile(final IPath path) {

        if (path.isAbsolute())
            return null;

        if (path.segmentCount() == 0)
            return moduleRoot;

        VirtualFile virtualFile = moduleRoot
            .findFileByRelativePath(path.toString());

        if (virtualFile != null && ModuleRootManager.getInstance(module)
            .getFileIndex().isInContent(virtualFile)) {
            return virtualFile;
        }

        return null;
    }

    @Override
    public int hashCode() {
        return moduleName.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        IntelliJProjectImplV2 other = (IntelliJProjectImplV2) obj;

        return moduleName.equals(other.moduleName);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + moduleName;
    }
}
