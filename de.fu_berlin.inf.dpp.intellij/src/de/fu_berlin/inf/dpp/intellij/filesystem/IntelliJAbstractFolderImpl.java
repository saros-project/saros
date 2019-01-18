package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ProjectFileIndexFacade;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJAbstractFolderImpl extends IntelliJResourceImpl implements IFolder {

  /** Relative path from the given source root */
  protected final IPath path;

  /** Source root * */
  protected VirtualFile srcRoot;

  public IntelliJAbstractFolderImpl(VirtualFile srcRoot, IPath path) {
    this.path = path;
    this.srcRoot = srcRoot;
  }

  @Override
  public IResource findMember(IPath path) {
    final VirtualFile file = findVirtualFile(path);

    if (file == null) return null;

    return file.isDirectory()
        ? new IntelliJFolderImpl(srcRoot, path)
        : new IntelliJFileImpl(srcRoot, path);
  }

  /**
   * Returns the virtual file for the given path belonging to this module.
   *
   * <p><b>Note:</b> This method can not return files for derived resources or resources belonging
   * to a sub-module.
   *
   * @param path relative path to the file
   * @return the virtual file or <code>null</code> if it does not exists in the VFS snapshot, is
   *     derived, belongs to a sub-module, or the given path is absolute.
   */
  @Nullable
  public VirtualFile findVirtualFile(final IPath path) {

    return FilesystemUtils.findVirtualFile(srcRoot, path);
  }

  @Override
  public IFile getFile(String name) {
    return getFile(IntelliJPathImpl.fromString(name));
  }

  @Override
  public IFile getFile(IPath path) {
    if (path.segmentCount() == 0)
      throw new IllegalArgumentException("cannot create file handle for an empty path");

    return new IntelliJFileImpl(srcRoot, path);
  }

  @Override
  public IFolder getFolder(String name) {
    return getFolder(IntelliJPathImpl.fromString(name));
  }

  @Override
  public IFolder getFolder(IPath path) {
    if (path.segmentCount() == 0)
      throw new IllegalArgumentException("cannot create folder handle for an empty path");

    return new IntelliJFolderImpl(srcRoot, path);
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

    return relativePath != null ? new IntelliJFolderImpl(srcRoot, relativePath) : null;
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

    return relativePath != null ? new IntelliJFileImpl(srcRoot, relativePath) : null;
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
    Module fileModule =
        ProjectFileIndexFacade.getInstance(FilesystemUtils.getModuleOfFile(srcRoot).getProject())
            .getModuleForFile(file);

    if (fileModule == null
        || !FilesystemUtils.getModuleOfFile(srcRoot).getName().equals(fileModule.getName())) {
      return null;
    }

    try {
      Path relativePath = Paths.get(srcRoot.getPath()).relativize(Paths.get(file.getPath()));

      return IntelliJPathImpl.fromString(relativePath.toString());

    } catch (IllegalArgumentException e) {

      return null;
    }
  }

  public Module getModule() {
    return FilesystemUtils.getModuleOfFile(srcRoot);
  }

  @NotNull
  @Override
  public IResource[] members() throws IOException {
    // TODO run as read action
    Module module = FilesystemUtils.getModuleOfFile(srcRoot);
    final List<IResource> result = new ArrayList<>();

    final VirtualFile[] children = srcRoot.getChildren();

    ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance(module).getFileIndex();

    for (final VirtualFile child : children) {

      if (!moduleFileIndex.isInContent(child)) {

        continue;
      }

      final IPath childPath = IntelliJPathImpl.fromString(child.getName());

      result.add(
          child.isDirectory()
              ? new IntelliJFolderImpl(srcRoot, childPath)
              : new IntelliJFileImpl(srcRoot, childPath));
    }

    return result.toArray(new IResource[result.size()]);
  }

  @NotNull
  @Override
  public IResource[] members(final int memberFlags) throws IOException {
    return members();
  }

  @Override
  public void create(final int updateFlags, final boolean local) throws IOException {
    this.create((updateFlags & IResource.FORCE) != 0, local);
  }

  /**
   * Creates this folder in the local filesystem.
   *
   * <p><b>Note:</b> The force flag is not supported. It does not allow the re-creation of an
   * already existing folder.
   *
   * @param force not supported
   * @param local not supported
   * @throws FileAlreadyExistsException if the folder already exists
   * @throws FileNotFoundException if the parent directory of this folder does not exist
   */
  @Override
  public void create(final boolean force, final boolean local) throws IOException {

    Filesystem.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final IResource parent = getParent();

            final VirtualFile parentFile =
                FilesystemUtils.findVirtualFile(srcRoot, parent.getProjectRelativePath());

            if (parentFile == null)
              throw new FileNotFoundException(
                  parent
                      + " does not exist or is derived, cannot create folder "
                      + IntelliJAbstractFolderImpl.this);

            final VirtualFile file = parentFile.findChild(getName());

            if (file != null) {
              String exceptionText = IntelliJAbstractFolderImpl.this + " already exists";

              if (force) exceptionText += ", force option is not supported";

              throw new FileAlreadyExistsException(exceptionText);
            }

            parentFile.createChildDirectory(IntelliJAbstractFolderImpl.this, getName());

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }
}
