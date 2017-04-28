package de.fu_berlin.inf.dpp.intellij.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.roots.ModuleRootManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;

public final class IntelliJProjectImplV2 extends IntelliJResourceImplV2
    implements IProject {

    // Module names are unique (even among different projects)
    private final String moduleName;

    private volatile Module module;

    private final VirtualFile moduleRoot;

    private final VirtualFile[] excludedRoots;

    /**
     * Creates a core compatible {@link IProject project} using the given
     * IntelliJ module.
     * <p>
     * <b>Note:</b> Only top level modules are fully supported. Modules inside
     * of other modules will be created as top level modules on the receiving
     * side of the session initialization. Inner modules of the shared module
     * will also be transmitted but not registered with IntelliJ as modules.
     * <p>
     * <b>Note:</b> Only modules with one or fewer content roots are supported.
     * IProject objects for modules with more than on content root can not be
     * created.
     *
     * @param module
     *            an IntelliJ <i>module</i>
     *
     */
    public IntelliJProjectImplV2(@NotNull final Module module) {
        this.module = module;
        this.moduleName = module.getName();

        ModuleRootManager moduleRootManager = ModuleRootManager
            .getInstance(module);

        final VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

        if (contentRoots.length == 0)
            throw new IllegalArgumentException("module: " + module
                + " does not have a content root");

        if (contentRoots.length > 1)
            throw new IllegalArgumentException("module: " + module
                + " has more than one content root");

        moduleRoot = contentRoots[0];
        excludedRoots = moduleRootManager.getExcludeRoots();
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

    @Override
    public boolean exists(final IPath path) {
        final VirtualFile file = findVirtualFile(path);

        return file != null && file.exists();
    }

    @NotNull
    @Override
    public IResource[] members() throws IOException {
        // TODO run as read action
        // TODO filter files that belong to other modules

        final List<IResource> result = new ArrayList<>();

        final VirtualFile[] children = moduleRoot.getChildren();

        for (final VirtualFile child : children) {

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
        return new IntelliJFileImplV2(this, path);
    }

    @NotNull
    @Override
    public IFolder getFolder(final String name) {
        return getFolder(IntelliJPathImpl.fromString(name));
    }

    @NotNull
    @Override
    public IFolder getFolder(final IPath path) {
        return new IntelliJFolderImplV2(this, path);
    }

    /**
     * Returns the virtual file for the given path belonging to this module.
     * <p>
     * <b>Note:</b> This method can return files for sub modules if the path
     * points to a file of a submodule.
     * 
     * @param path
     *            relative path to the file
     * @return the virtual file or <code>null</code> if it does not exists in
     *         the VFS snapshot, or the path is absolute or empty.
     */
    @Nullable
    VirtualFile findVirtualFile(final IPath path) {

        if (path.segmentCount() == 0 || path.isAbsolute())
            return null;

        return moduleRoot.findFileByRelativePath(path.toString());
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
