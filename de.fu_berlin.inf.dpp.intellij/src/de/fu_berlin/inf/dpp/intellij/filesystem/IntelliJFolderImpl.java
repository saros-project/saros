package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IntelliJFolderImpl extends IntelliJAbstractFolderImpl implements IFolder {

  public IntelliJFolderImpl(@NotNull VirtualFile contentRoot, @NotNull final IPath path) {
    super(contentRoot, path);
  }

  @Override
  public boolean exists(@NotNull IPath path) {
    final VirtualFile file = findVirtualFile(path);

    return file != null && file.exists();
  }

  @NotNull
  @Override
  public IResource[] members() throws IOException {
    // TODO run as read action

    final VirtualFile folder = FilesystemUtils.findVirtualFile(srcRoot, path);

    if (folder == null || !folder.exists())
      throw new FileNotFoundException(this + " does not exist or is " + "derived");

    if (!folder.isDirectory()) throw new IOException(this + " is a file");

    final List<IResource> result = new ArrayList<>();

    final VirtualFile[] children = folder.getChildren();

    ModuleFileIndex moduleFileIndex =
        ModuleRootManager.getInstance(FilesystemUtils.getModuleOfFile(srcRoot)).getFileIndex();

    for (final VirtualFile child : children) {

      if (!moduleFileIndex.isInContent(child)) {

        continue;
      }

      final IPath childPath = path.append(IntelliJPathImpl.fromString(child.getName()));

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

  @Nullable
  @Override
  public String getDefaultCharset() throws IOException {
    // TODO retrieve encoding for the module or use the project settings
    return getParentFolder().getDefaultCharset();
  }

  /**
   * Returns whether this folder exists.
   *
   * <p><b>Note:</b> A derived folder is treated as being nonexistent.
   *
   * @return <code>true</code> if the resource exists, is a folder, and is not derived, <code>false
   *     </code> otherwise
   */
  @Override
  public boolean exists() {
    final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

    return file != null && file.exists() && file.isDirectory();
  }

  @NotNull
  @Override
  public IPath getFullPath() {
    IPath rootPath =
        IntelliJPathImpl.fromString(FilesystemUtils.getModuleOfFile(srcRoot).getName());
    return rootPath.append(path);
  }

  @NotNull
  @Override
  public String getName() {
    return path.lastSegment();
  }

  /**
   * Returns the parent of this folder.
   *
   * <p><b>Note:</b> The interface specification for this method does allow <code>null</code> as a
   * return value. This implementation, however, can not return null values, as suggested by its
   * NotNull tag.
   *
   * @return an <code>IContainer</code> object for the parent of this folder
   */
  @NotNull
  @Override
  public IContainer getParent() {
    if (path.segmentCount() == 1) return new IntelliJProjectImpl(srcRoot);

    return new IntelliJFolderImpl(srcRoot, path.removeLastSegments(1));
  }

  /**
   * Returns the parent of this folder.
   *
   * <p><b>Note:</b> The interface specification for this method does allow <code>null</code> as a
   * return value. This implementation, however, can not return null values, as suggested by its
   * NotNull tag.
   *
   * @return an <code>IFolder</code> object for the parent of this folder
   */
  @NotNull
  @Override
  public IFolder getParentFolder() {
    if (path.segmentCount() == 1) return new IntelliJProjectImpl(srcRoot);

    return new IntelliJFolderImpl(srcRoot, path.removeLastSegments(1));
  }

  @Override
  public IFolder getReferenceFolder() {
    return new IntelliJProjectImpl(srcRoot);
  }

  @NotNull
  @Override
  public IPath getProjectRelativePath() {
    return path;
  }

  @Override
  public int getType() {
    return IResource.FOLDER;
  }

  @Override
  public boolean isDerived(final boolean checkAncestors) {
    return isDerived();
  }

  @Override
  public boolean isDerived() {
    return !exists();
  }

  @Override
  public void delete(final int updateFlags) throws IOException {

    Filesystem.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

            if (file == null)
              throw new FileNotFoundException(
                  IntelliJFolderImpl.this + " does not exist or is derived");

            if (!file.isDirectory()) throw new IOException(this + " is not a folder");

            file.delete(IntelliJFolderImpl.this);

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }

  @Override
  public void move(final IPath destination, final boolean force) throws IOException {
    throw new IOException("NYI");
  }

  @NotNull
  @Override
  public IPath getLocation() {
    // TODO might return a wrong location
    return IntelliJPathImpl.fromString(srcRoot.getPath()).append(path);
  }

  @Override
  public IFolder getFolder(String name) {
    return null;
  }

  @Override
  public int hashCode() {
    return FilesystemUtils.getModuleOfFile(srcRoot).getName().hashCode() + 31 * path.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    IntelliJFolderImpl other = (IntelliJFolderImpl) obj;

    return srcRoot.equals(other.srcRoot) && path.equals(other.path);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " : " + path + " - " + srcRoot;
  }
}
