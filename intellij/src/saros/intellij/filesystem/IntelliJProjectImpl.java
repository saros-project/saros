package saros.intellij.filesystem;

import static saros.filesystem.IResource.Type.PROJECT;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.intellij.project.filesystem.IntelliJPathImpl;
import saros.intellij.runtime.FilesystemRunner;

/** A <code>IntelliJProjectImpl</code> represents a specific module loaded in a specific project. */
public final class IntelliJProjectImpl extends IntelliJResourceImpl implements IProject {
  private static final Logger log = Logger.getLogger(IntelliJProjectImpl.class);

  private final Project project;

  private final Module module;

  /**
   * Creates a core compatible {@link IProject project} using the given IntelliJ module.
   *
   * <p><b>Note:</b> Only top level modules are fully supported. Modules inside of other modules
   * will be created as top level modules on the receiving side of the session initialization. Inner
   * modules of the shared module will also be transmitted but not registered with IntelliJ as
   * modules.
   *
   * <p><b>Note:</b> Only modules with exactly one content root are currently supported. IProject
   * objects for modules with fewer or more than one content root can not be created.
   *
   * @param module an IntelliJ <i>module</i>
   * @throws IllegalArgumentException if the given module does not have exactly one content root
   */
  public IntelliJProjectImpl(@NotNull final Module module) {
    this.module = module;

    this.project = module.getProject();

    // Still used to ensure that the module has exactly one content root
    getModuleContentRoot(module);
  }

  /**
   * Returns the content root of the given module.
   *
   * <p>This method is used to enforce the current restriction that shared modules must contain
   * exactly one content root.
   *
   * @param module the module to get the content root for
   * @return the content root of the given module
   * @throws IllegalArgumentException if the given module does not have exactly one content root
   */
  @NotNull
  private static VirtualFile getModuleContentRoot(@NotNull Module module) {

    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

    VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

    int numberOfContentRoots = contentRoots.length;

    if (numberOfContentRoots != 1) {
      throw new IllegalArgumentException(
          "Modules shared with Saros currently must contain exactly one content root. The given "
              + "module "
              + module
              + " has "
              + numberOfContentRoots
              + " content roots: "
              + Arrays.toString(contentRoots));
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
   * Returns whether the resource for the given path exists.
   *
   * <p><b>Note:</b> An ignored resource is treated as being nonexistent.
   *
   * @return <code>true</code> if the resource exists and is not ignored, <code>false</code>
   *     otherwise
   * @see #isIgnored()
   */
  @Override
  public boolean exists(final IPath relativePath) {
    final VirtualFile file = findVirtualFile(relativePath);

    return file != null && file.exists();
  }

  @NotNull
  @Override
  public List<IResource> members() {
    final List<IResource> result = new ArrayList<>();

    final VirtualFile[] children = getModuleContentRoot(module).getChildren();

    ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance(module).getFileIndex();

    for (final VirtualFile child : children) {

      if (!FilesystemRunner.runReadAction(() -> moduleFileIndex.isInContent(child))) {
        continue;
      }

      final IPath childPath = IntelliJPathImpl.fromString(child.getName());

      result.add(
          child.isDirectory()
              ? new IntelliJFolderImpl(this, childPath)
              : new IntelliJFileImpl(this, childPath));
    }

    return result;
  }

  @Override
  public boolean exists() {
    return !module.isDisposed() && module.isLoaded();
  }

  @NotNull
  @Override
  public String getName() {
    return module.getName();
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
   * Returns the path to the given file relative to the content root of this module.
   *
   * <p><b>Note:</b> This methods expects that the given <code>VirtualFile</code> exists.
   *
   * @param file the <code>VirtualFile</code> to get the relative path for
   * @return a relative path for the given file or <code>null</code> if the file does not belong to
   *     this module or there is no relative path from the content root to the file
   */
  @Nullable
  private IPath getProjectRelativePath(@NotNull VirtualFile file) {
    ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
    Module fileModule =
        FilesystemRunner.runReadAction(() -> projectFileIndex.getModuleForFile(file));

    if (!module.equals(fileModule)) {
      return null;
    }

    VirtualFile moduleRoot = getModuleContentRoot(module);

    try {
      Path relativePath = Paths.get(moduleRoot.getPath()).relativize(Paths.get(file.getPath()));

      return IntelliJPathImpl.fromString(relativePath.toString());

    } catch (IllegalArgumentException e) {
      log.warn(
          "Could not find a relative path from the content root "
              + moduleRoot
              + " to the file "
              + file,
          e);

      return null;
    }
  }

  @Override
  public Type getType() {
    return PROJECT;
  }

  @Override
  public boolean isIgnored() {
    return false;
  }

  @Override
  public void delete() throws IOException {
    throw new IOException("delete is not supported");
  }

  // TODO unify with IntelliJFolderImpl.getFile(...) and getFolder(...)
  @NotNull
  @Override
  public IFile getFile(final String pathString) {
    return getFile(IntelliJPathImpl.fromString(pathString));
  }

  @NotNull
  @Override
  public IFile getFile(final IPath path) {

    if (path.segmentCount() == 0)
      throw new IllegalArgumentException("cannot create file handle for an empty path");

    return new IntelliJFileImpl(this, path);
  }

  /**
   * Returns an <code>IFile</code> for the given file.
   *
   * @param file the <code>VirtualFile</code> to get the <code>IFile</code> for
   * @return an <code>IFile</code> for the given file or <code>null</code> if the given file is a
   *     directory, does not exist, or the relative path of the file could not be constructed
   */
  @Nullable
  public IFile getFile(@NotNull final VirtualFile file) {
    if (file.isDirectory() || !file.exists()) {
      return null;
    }

    IPath relativePath = getProjectRelativePath(file);

    return relativePath != null ? new IntelliJFileImpl(this, relativePath) : null;
  }

  @NotNull
  @Override
  public IFolder getFolder(final String pathString) {
    return getFolder(IntelliJPathImpl.fromString(pathString));
  }

  @NotNull
  @Override
  public IFolder getFolder(final IPath path) {

    if (path.segmentCount() == 0)
      throw new IllegalArgumentException("cannot create folder handle for an empty path");

    return new IntelliJFolderImpl(this, path);
  }

  /**
   * Returns an <code>IFolder</code> for the given file.
   *
   * @param file the <code>VirtualFile</code> to get the <code>IFolder</code> for
   * @return an <code>IFolder</code> for the given file or <code>null</code> if the given file is
   *     not a directory, does not exist, or the relative path of the file could not be constructed
   */
  @Nullable
  public IFolder getFolder(@NotNull final VirtualFile file) {
    if (!file.isDirectory() || !file.exists()) {
      return null;
    }

    IPath relativePath = getProjectRelativePath(file);

    return relativePath != null ? new IntelliJFolderImpl(this, relativePath) : null;
  }

  /**
   * Returns an <code>IResource</code> for the given file.
   *
   * @param file the <code>VirtualFile</code> to get the <code>IResource</code> for
   * @return an <code>IResource</code> for the given file or <code>null</code> if the given file
   *     does not exist or the relative path of the file could not be constructed
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
   *
   * <p><b>Note:</b> This method can not return files for ignored resources or resources belonging
   * to a sub-module.
   *
   * @param path relative path to the file
   * @return the virtual file or <code>null</code> if it does not exists in the VFS snapshot, is
   *     ignored, belongs to a sub-module, or the given path is absolute.
   * @see #isIgnored()
   */
  @Nullable
  public VirtualFile findVirtualFile(final IPath path) {

    if (path.isAbsolute()) return null;

    VirtualFile moduleRoot = getModuleContentRoot(module);

    if (path.segmentCount() == 0) return moduleRoot;

    VirtualFile virtualFile = moduleRoot.findFileByRelativePath(path.toString());

    if (virtualFile == null) {
      return null;
    }

    ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance(module).getFileIndex();
    boolean isInContent =
        FilesystemRunner.runReadAction(() -> moduleFileIndex.isInContent(virtualFile));

    return isInContent ? virtualFile : null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method operates under the assumption that module objects are handled as a singleton
   * across the IDE lifecycle, i.e. a module will always be represented by a single, unique <code>
   * Module</code> object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return module.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method operates under the assumption that module objects are handled as a singleton
   * across the IDE lifecycle, i.e. a module will always be represented by a single, unique <code>
   * Module</code> object.
   *
   * @return whether the given objects is equal to this object
   */
  @Override
  public boolean equals(final Object obj) {

    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    IntelliJProjectImpl other = (IntelliJProjectImpl) obj;

    return module.equals(other.module);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " : " + project + " - " + module;
  }
}
