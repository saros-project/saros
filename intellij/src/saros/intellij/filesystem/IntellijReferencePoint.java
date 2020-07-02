package saros.intellij.filesystem;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

/** Intellij implementation of the Saros reference point interface. */
public class IntellijReferencePoint implements IReferencePoint {
  private static final Logger log = Logger.getLogger(IntellijReferencePoint.class);

  /** The project instance the reference point is bound to. */
  private final Project project;

  /** The virtual file represented by this reference point. */
  private final VirtualFile virtualFile;

  public IntellijReferencePoint(@NotNull Project project, @NotNull VirtualFile virtualFile) {

    if (!project.isInitialized()) {
      throw new IllegalArgumentException("The given project must be initialized - " + project);
    }

    if (!virtualFile.exists()) {
      throw new IllegalArgumentException("The given virtual file must exist - " + virtualFile);

    } else if (!virtualFile.isDirectory()) {
      throw new IllegalArgumentException(
          "The given virtual file must be a directory - " + virtualFile);
    }

    this.project = project;
    this.virtualFile = virtualFile;
  }

  /**
   * Returns the project instance the reference point is bound to.
   *
   * @return the project instance the reference point is bound to
   */
  @NotNull
  public Project getProject() {
    return project;
  }

  /**
   * Returns the virtual file represented by this reference point.
   *
   * @return the virtual file represented by this reference point
   */
  @NotNull
  public VirtualFile getVirtualFile() {
    return virtualFile;
  }

  @Override
  public boolean exists(@NotNull IPath path) {
    if (!virtualFile.exists()) {
      return false;
    }

    VirtualFile file = findVirtualFile(path);

    return file != null && file.exists();
  }

  @Override
  @NotNull
  public List<IResource> members() {
    List<IResource> result = new ArrayList<>();

    VirtualFile[] children = virtualFile.getChildren();

    for (VirtualFile child : children) {
      IPath childPath = IntellijPath.fromString(child.getName());

      result.add(
          child.isDirectory()
              ? new IntellijFolder(this, childPath)
              : new IntellijFile(this, childPath));
    }

    return result;
  }

  @Override
  public boolean exists() {
    return virtualFile.exists();
  }

  @Override
  @NotNull
  public String getName() {
    return virtualFile.getName();
  }

  @Override
  @NotNull
  public IPath getReferencePointRelativePath() {
    return IntellijPath.EMPTY;
  }

  @Override
  public boolean isNested(IReferencePoint otherReferencePoint) {
    Path p1 = Paths.get(virtualFile.getPath());

    IntellijReferencePoint i2 = (IntellijReferencePoint) otherReferencePoint;
    Path p2 = Paths.get(i2.getVirtualFile().getPath());

    return p1.equals(p2) || p1.startsWith(p2) || p2.startsWith(p1);
  }

  /**
   * Returns the path to the given file relative to the virtual file represented by this reference
   * point.
   *
   * <p><b>Note:</b> This methods expects that the given <code>VirtualFile</code> exists.
   *
   * @param file the <code>VirtualFile</code> to get the relative path for
   * @return a relative path for the given file or <code>null</code> if there is no relative path
   *     from the reference point to the file
   */
  @Nullable
  private IPath getReferencePointRelativePath(@NotNull VirtualFile file) {

    String referencePointPath = virtualFile.getPath();
    String filePath = file.getPath();

    if (!filePath.startsWith(referencePointPath)) {
      return null;
    }

    try {
      Path relativePath = Paths.get(referencePointPath).relativize(Paths.get(filePath));

      return IntellijPath.fromString(relativePath.toString());

    } catch (IllegalArgumentException e) {
      log.warn(
          "Could not find a relative path from the base file "
              + virtualFile
              + " to the file "
              + file,
          e);

      return null;
    }
  }

  @Override
  @NotNull
  public IFile getFile(@NotNull String pathString) {
    return getFile(IntellijPath.fromString(pathString));
  }

  @Override
  @NotNull
  public IFile getFile(@NotNull IPath path) {
    if (path.segmentCount() == 0) {
      throw new IllegalArgumentException("Can not create file handle for an empty path");
    }

    return new IntellijFile(this, path);
  }

  /**
   * Returns an <code>IFile</code> for the given file.
   *
   * @param file the <code>VirtualFile</code> to get the <code>IFile</code> for
   * @return an <code>IFile</code> for the given file or <code>null</code> if the given file is a
   *     directory, does not exist, or the relative path of the file could not be constructed
   */
  @Nullable
  IFile getFile(@NotNull VirtualFile file) {
    if (!file.exists() || file.isDirectory()) {
      return null;
    }

    IPath relativePath = getReferencePointRelativePath(file);

    return relativePath != null ? new IntellijFile(this, relativePath) : null;
  }

  @Override
  @NotNull
  public IFolder getFolder(@NotNull String pathString) {
    return getFolder(IntellijPath.fromString(pathString));
  }

  @Override
  @NotNull
  public IFolder getFolder(@NotNull IPath path) {
    if (path.segmentCount() == 0) {
      throw new IllegalArgumentException("Can not create folder handle for an empty path");
    }

    return new IntellijFolder(this, path);
  }

  /**
   * Returns an <code>IFolder</code> for the given file.
   *
   * @param file the <code>VirtualFile</code> to get the <code>IFolder</code> for
   * @return an <code>IFolder</code> for the given file or <code>null</code> if the given file is
   *     not a directory, does not exist, or the relative path of the file could not be constructed
   * @throws IllegalArgumentException if the given virtual file is the reference point file
   */
  @Nullable
  IFolder getFolder(@NotNull VirtualFile file) {
    if (file.equals(virtualFile)) {
      throw new IllegalArgumentException("Given virtual file must not be the reference point file");

    } else if (!file.exists() || !file.isDirectory()) {
      return null;
    }

    IPath relativePath = getReferencePointRelativePath(file);

    return relativePath != null ? new IntellijFolder(this, relativePath) : null;
  }

  /**
   * Returns an <code>IResource</code> for the given file.
   *
   * @param file the <code>VirtualFile</code> to get the <code>IResource</code> for
   * @return an <code>IResource</code> for the given file or <code>null</code> if the given file
   *     does not exist or the relative path of the file could not be constructed
   * @throws IllegalArgumentException if the given virtual file is the reference point file
   */
  @Nullable
  IResource getResource(@NotNull VirtualFile file) {
    if (file.isDirectory()) {
      return getFolder(file);
    } else {
      return getFile(file);
    }
  }

  /**
   * Returns the virtual file for the given relative path.
   *
   * @param relativePath relative path to the file
   * @return the virtual file or <code>null</code> if there is no such file in the VFS snapshot
   */
  @Nullable
  VirtualFile findVirtualFile(@NotNull IPath relativePath) {
    if (relativePath.isAbsolute()) return null;

    if (relativePath.segmentCount() == 0) return virtualFile;

    return virtualFile.findFileByRelativePath(relativePath.toString());
  }

  @Override
  public int hashCode() {
    return virtualFile.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    IntellijReferencePoint other = (IntellijReferencePoint) obj;

    return this.virtualFile.equals(other.virtualFile);
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + " : " + virtualFile + "]";
  }
}
