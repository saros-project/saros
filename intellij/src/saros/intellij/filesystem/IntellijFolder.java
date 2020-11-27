package saros.intellij.filesystem;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IResource;
import saros.intellij.runtime.FilesystemRunner;
import saros.util.PathUtils;

/** Intellij implementation of the Saros folder interface. */
public class IntellijFolder extends AbstractIntellijResource implements IFolder {
  private static final Logger log = Logger.getLogger(IntellijFolder.class);

  public IntellijFolder(
      @NotNull IntellijReferencePoint referencePoint, @NotNull Path relativePath) {

    super(referencePoint, relativePath);
  }

  @Override
  public boolean exists() {
    if (!referencePoint.exists()) {
      return false;
    }

    VirtualFile virtualFile = getVirtualFile();

    return existsInternal(virtualFile);
  }

  /**
   * whether the given virtual file is not <code>null</code>, exists, and is a directory.
   *
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is not <code>null</code>, exists, and is a directory
   */
  private static boolean existsInternal(@Nullable VirtualFile virtualFile) {
    return virtualFile != null && virtualFile.exists() && virtualFile.isDirectory();
  }

  @Override
  public boolean exists(@NotNull Path path) {
    return referencePoint.exists(relativePath.resolve(path));
  }

  @Override
  @NotNull
  public List<IResource> members() throws IOException {
    VirtualFile folder = getVirtualFile();

    if (folder == null || !folder.exists()) {
      throw new FileNotFoundException(
          "Could not obtain child resources for " + this + " as it does not exist or is not valid");
    }

    if (!folder.isDirectory()) {
      throw new IOException(
          "Could not obtain child resources for " + this + " as it is not a directory.");
    }

    List<IResource> result = new ArrayList<>();

    VirtualFile[] children = folder.getChildren();

    for (VirtualFile child : children) {
      Path childPath = relativePath.resolve(child.getName());

      result.add(
          child.isDirectory()
              ? new IntellijFolder(referencePoint, childPath)
              : new IntellijFile(referencePoint, childPath));
    }

    return result;
  }

  @Override
  @NotNull
  public IFile getFile(@NotNull String pathString) {
    return getFile(Paths.get(pathString));
  }

  @Override
  @NotNull
  public IFile getFile(@NotNull Path path) {
    if (PathUtils.isEmpty(path)) {
      throw new IllegalArgumentException("Can not create file handle for an empty path");
    }

    Path referencePointRelativeChildPath = relativePath.resolve(path);

    return new IntellijFile(referencePoint, referencePointRelativeChildPath);
  }

  @Override
  @NotNull
  public IFolder getFolder(@NotNull String pathString) {
    return getFolder(Paths.get(pathString));
  }

  @Override
  @NotNull
  public IFolder getFolder(@NotNull Path path) {
    if (PathUtils.isEmpty(path)) {
      throw new IllegalArgumentException("Can not create folder handle for an empty path");
    }

    Path referencePointRelativeChildPath = relativePath.resolve(path);

    return new IntellijFolder(referencePoint, referencePointRelativeChildPath);
  }

  @Override
  public void delete() throws IOException {
    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              deleteInternal();

              return null;
            },
        ModalityState.defaultModalityState());
  }

  /**
   * Deletes the folder in the filesystem.
   *
   * @throws IOException if the resource is a file or the deletion failed
   * @see VirtualFile#delete(Object)
   */
  private void deleteInternal() throws IOException {
    VirtualFile virtualFile = getVirtualFile();

    if (virtualFile == null || !virtualFile.exists()) {
      log.debug("Ignoring file deletion request for " + this + " as folder does not exist");

      return;
    }

    if (!virtualFile.isDirectory()) {
      throw new IOException("Failed to delete " + this + " as it is not a folder");
    }

    virtualFile.delete(IntellijFolder.this);
  }

  @Override
  public void create() throws IOException {
    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              createInternal();

              return null;
            },
        ModalityState.defaultModalityState());
  }

  /**
   * Creates the folder in the local filesystem.
   *
   * @throws FileAlreadyExistsException if a resource with the same name already exists
   * @throws FileNotFoundException if the parent directory of the folder does not exist
   */
  private void createInternal() throws IOException {
    IResource parent = getParent();

    VirtualFile parentFile = referencePoint.findVirtualFile(parent.getReferencePointRelativePath());

    if (parentFile == null || !parentFile.exists()) {
      throw new FileNotFoundException(
          "Could not create "
              + this
              + " as its parent folder "
              + parent
              + " does not exist or is not valid");
    }

    VirtualFile virtualFile = parentFile.findChild(getName());

    if (virtualFile != null && virtualFile.exists()) {
      throw new FileAlreadyExistsException(
          "Could not create "
              + this
              + " as a resource with the same name already exists: "
              + virtualFile);
    }

    parentFile.createChildDirectory(this, getName());
  }
}
