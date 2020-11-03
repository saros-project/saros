package saros.intellij.filesystem;

import static saros.filesystem.IResource.Type.FOLDER;

import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IContainer;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.intellij.editor.ProjectAPI;
import saros.util.PathUtils;

/** Abstract Intellij implementation of the Saros resource interface. */
public abstract class AbstractIntellijResource implements IResource {

  /** Reference point to which this resource belongs. */
  protected final IntellijReferencePoint referencePoint;

  /** Relative path from the given reference point. */
  protected final Path relativePath;

  /**
   * Instantiates an Intellij resource object.
   *
   * @param referencePoint the reference point the resource is located beneath
   * @param relativePath the relative path from the reference point
   * @throws IllegalArgumentException if the given path is absolute or empty
   */
  public AbstractIntellijResource(
      @NotNull IntellijReferencePoint referencePoint, @NotNull Path relativePath) {

    if (PathUtils.isEmpty(relativePath)) {
      throw new IllegalArgumentException("Given path must not be empty");
    }
    if (relativePath.isAbsolute()) {
      throw new IllegalArgumentException("The given path must not be absolute - " + relativePath);
    }

    this.referencePoint = referencePoint;
    this.relativePath = relativePath;
  }

  /**
   * Returns the virtual file represented by this resource.
   *
   * @return the virtual file represented by this resource or <code>null</code> if no such resource
   *     exists in the local VFS snapshot
   */
  @Nullable
  protected VirtualFile getVirtualFile() {
    return referencePoint.findVirtualFile(relativePath);
  }

  @Override
  @NotNull
  public String getName() {
    return relativePath.getFileName().toString();
  }

  @Override
  @NotNull
  public IContainer getParent() {
    if (relativePath.getNameCount() <= 1) {
      return referencePoint;
    }

    return new IntellijFolder(referencePoint, PathUtils.removeLastSegments(relativePath, 1));
  }

  @Override
  @NotNull
  public IReferencePoint getReferencePoint() {
    return referencePoint;
  }

  @Override
  @NotNull
  public Path getReferencePointRelativePath() {
    return relativePath;
  }

  @Override
  public boolean isIgnored() {
    return isGitConfig() || isExcluded();
  }

  /**
   * Returns whether this resource is part of the git configuration directory.
   *
   * @return whether this resource is part of the git configuration directory
   */
  private boolean isGitConfig() {
    String path = PathUtils.toPortableString(relativePath);

    return (path.startsWith(".git/")
        || path.contains("/.git/")
        || getType() == FOLDER && (path.endsWith("/.git") || path.equals(".git")));
  }

  /**
   * Returns whether the resource is located under an excluded root in its project.
   *
   * <p>Nonexistent resources are reported as excluded.
   *
   * @return whether the resource is located under an excluded root in its project
   */
  private boolean isExcluded() {
    VirtualFile virtualFile = getVirtualFile();

    if (virtualFile == null) {
      return true;
    }

    return ProjectAPI.isExcluded(referencePoint.getProject(), virtualFile);
  }

  @Override
  public int hashCode() {
    return referencePoint.hashCode() + 31 * relativePath.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    AbstractIntellijResource other = (AbstractIntellijResource) obj;

    return this.referencePoint.equals(other.referencePoint)
        && this.relativePath.equals(other.relativePath);
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + " : " + relativePath + " - " + referencePoint + "]";
  }
}
