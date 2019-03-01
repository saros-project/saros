package saros.intellij.filesystem;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IContainer;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.intellij.project.filesystem.IntelliJPathImpl;

public final class IntelliJFolderImpl extends IntelliJResourceImpl implements IFolder {

  /** Relative path from the given project */
  private final IPath path;

  private final IntelliJProjectImpl project;

  public IntelliJFolderImpl(@NotNull final IntelliJProjectImpl project, @NotNull final IPath path) {
    this.project = project;
    this.path = path;
  }

  @Override
  public boolean exists(@NotNull IPath path) {
    return project.exists(this.path.append(path));
  }

  @NotNull
  @Override
  public IResource[] members() throws IOException {
    // TODO run as read action

    final VirtualFile folder = project.findVirtualFile(path);

    if (folder == null || !folder.exists())
      throw new FileNotFoundException(this + " does not exist or is " + "derived");

    if (!folder.isDirectory()) throw new IOException(this + " is a file");

    final List<IResource> result = new ArrayList<>();

    final VirtualFile[] children = folder.getChildren();

    ModuleFileIndex moduleFileIndex =
        ModuleRootManager.getInstance(project.getModule()).getFileIndex();

    for (final VirtualFile child : children) {

      if (!moduleFileIndex.isInContent(child)) {

        continue;
      }

      final IPath childPath = path.append(IntelliJPathImpl.fromString(child.getName()));

      result.add(
          child.isDirectory()
              ? new IntelliJFolderImpl(project, childPath)
              : new IntelliJFileImpl(project, childPath));
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
    return getParent().getDefaultCharset();
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
    final VirtualFile file = project.findVirtualFile(path);

    return file != null && file.exists() && file.isDirectory();
  }

  @NotNull
  @Override
  public IPath getFullPath() {
    return project.getFullPath().append(path);
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
    if (path.segmentCount() == 1) return project;

    return new IntelliJFolderImpl(project, path.removeLastSegments(1));
  }

  @NotNull
  @Override
  public IProject getProject() {
    return project;
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

            final VirtualFile file = project.findVirtualFile(path);

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
    return project.getLocation().append(path);
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

            final VirtualFile parentFile = project.findVirtualFile(parent.getProjectRelativePath());

            if (parentFile == null)
              throw new FileNotFoundException(
                  parent
                      + " does not exist or is derived, cannot create folder "
                      + IntelliJFolderImpl.this);

            final VirtualFile file = parentFile.findChild(getName());

            if (file != null) {
              String exceptionText = IntelliJFolderImpl.this + " already exists";

              if (force) exceptionText += ", force option is not supported";

              throw new FileAlreadyExistsException(exceptionText);
            }

            parentFile.createChildDirectory(IntelliJFolderImpl.this, getName());

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }

  @Override
  public int hashCode() {
    return project.hashCode() + 31 * path.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    IntelliJFolderImpl other = (IntelliJFolderImpl) obj;

    return project.equals(other.project) && path.equals(other.path);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " : " + path + " - " + project;
  }
}
