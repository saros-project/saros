package saros.intellij.filesystem;

import static saros.filesystem.IResource.Type.FOLDER;

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
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.intellij.project.filesystem.IntelliJPathImpl;
import saros.intellij.runtime.FilesystemRunner;

public final class IntelliJFolderImpl extends IntelliJResourceImpl implements IFolder {
  private static final Logger log = Logger.getLogger(IntelliJFolderImpl.class);

  /** Relative path from the given project */
  private final IPath path;

  private final IntelliJProjectImpl project;

  public IntelliJFolderImpl(@NotNull final IntelliJProjectImpl project, @NotNull final IPath path) {
    this.project = project;
    this.path = path;
  }

  @Override
  public boolean exists(@NotNull IPath relativePath) {
    return project.exists(this.path.append(relativePath));
  }

  @NotNull
  @Override
  public List<IResource> members() throws IOException {
    // TODO run as read action

    final VirtualFile folder = project.findVirtualFile(path);

    if (folder == null || !folder.exists())
      throw new FileNotFoundException(this + " does not exist or is ignored");

    if (!folder.isDirectory()) throw new IOException(this + " is a file");

    final List<IResource> result = new ArrayList<>();

    final VirtualFile[] children = folder.getChildren();

    ModuleFileIndex moduleFileIndex =
        ModuleRootManager.getInstance(project.getModule()).getFileIndex();

    for (final VirtualFile child : children) {

      if (!FilesystemRunner.runReadAction(() -> moduleFileIndex.isInContent(child))) {

        continue;
      }

      final IPath childPath = path.append(IntelliJPathImpl.fromString(child.getName()));

      result.add(
          child.isDirectory()
              ? new IntelliJFolderImpl(project, childPath)
              : new IntelliJFileImpl(project, childPath));
    }

    return result;
  }

  // TODO unify with IntelliJProjectImpl.getFile(...) and getFolder(...)
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

    return new IntelliJFileImpl(project, path);
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

    return new IntelliJFolderImpl(project, path);
  }

  /**
   * Returns whether this folder exists.
   *
   * <p><b>Note:</b> An ignored folder is treated as being nonexistent.
   *
   * @return <code>true</code> if the resource exists, is a folder, and is not ignored, <code>false
   *     </code> otherwise
   * @see #isIgnored()
   */
  @Override
  public boolean exists() {
    final VirtualFile file = project.findVirtualFile(path);

    return file != null && file.exists() && file.isDirectory();
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
  public Type getType() {
    return FOLDER;
  }

  @Override
  public void delete() throws IOException {
    FilesystemRunner.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final VirtualFile file = project.findVirtualFile(path);

            if (file == null) {
              log.debug("Ignoring file deletion request for " + this + " as folder does not exist");

              return null;
            }

            if (!file.isDirectory()) throw new IOException(this + " is not a folder");

            file.delete(IntelliJFolderImpl.this);

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }

  /**
   * Creates this folder in the local filesystem.
   *
   * @throws FileAlreadyExistsException if the folder already exists
   * @throws FileNotFoundException if the parent directory of this folder does not exist
   */
  @Override
  public void create() throws IOException {
    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              final IResource parent = getParent();

              final VirtualFile parentFile =
                  project.findVirtualFile(parent.getProjectRelativePath());

              if (parentFile == null)
                throw new FileNotFoundException(
                    parent
                        + " does not exist or is ignored, cannot create folder "
                        + IntelliJFolderImpl.this);

              final VirtualFile file = parentFile.findChild(getName());

              if (file != null) {
                throw new FileAlreadyExistsException(IntelliJFolderImpl.this + " already exists");
              }

              parentFile.createChildDirectory(IntelliJFolderImpl.this, getName());

              return null;
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
