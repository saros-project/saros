package saros.filesystem;

import static saros.filesystem.IResource.Type.FOLDER;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import saros.util.PathUtils;

/** Abstract Eclipse implementation of the Saros resource interface. */
public abstract class AbstractEclipseResource implements IResource {

  /** The reference point this resource belongs to. */
  protected final EclipseReferencePoint referencePoint;

  /** The reference-point-relative path for this resource. */
  protected final Path relativePath;

  /**
   * Instantiates an Eclipse resource object.
   *
   * @param referencePoint the reference point the resource is located beneath
   * @param relativePath the relative path from the reference point
   * @throws NullPointerException if the given reference point or path is <code>null</code>
   * @throws IllegalArgumentException if the given path is absolute or empty
   */
  public AbstractEclipseResource(EclipseReferencePoint referencePoint, Path relativePath) {
    Objects.requireNonNull(referencePoint, "The given reference point must not be null");
    Objects.requireNonNull(relativePath, "The given relative path must not be null");

    if (PathUtils.isEmpty(relativePath)) {
      throw new IllegalArgumentException("Given path must not be empty");

    } else if (relativePath.isAbsolute()) {
      throw new IllegalArgumentException("The given path must not be absolute - " + relativePath);
    }

    this.referencePoint = referencePoint;
    this.relativePath = PathUtils.normalize(relativePath);
  }

  @Override
  public boolean exists() {
    return getDelegate().exists();
  }

  @Override
  public String getName() {
    return relativePath.getFileName().toString();
  }

  @Override
  public IReferencePoint getReferencePoint() {
    return referencePoint;
  }

  @Override
  public Path getReferencePointRelativePath() {
    return relativePath;
  }

  @Override
  public IContainer getParent() {
    if (relativePath.getNameCount() <= 1) {
      return referencePoint;
    }

    return new EclipseFolder(referencePoint, PathUtils.removeLastSegments(relativePath, 1));
  }

  @Override
  public boolean isIgnored() {
    return isGitConfig() || isDerived();
  }

  /**
   * Returns whether this resource is seen as derived by the local Eclipse instance.
   *
   * @return whether this resource is seen as derived by the local Eclipse instance
   * @see org.eclipse.core.resources.IResource#isDerived(int)
   */
  boolean isDerived() {
    return getDelegate().isDerived(org.eclipse.core.resources.IResource.CHECK_ANCESTORS);
  }

  /**
   * Returns whether this resource is part of the git configuration directory.
   *
   * @return whether this resource is part of the git configuration directory
   */
  private boolean isGitConfig() {
    String path = PathUtils.toPortableString(getReferencePointRelativePath());

    return (path.startsWith(".git/")
        || path.contains("/.git/")
        || getType() == FOLDER && (path.endsWith("/.git") || path.equals(".git")));
  }

  @Override
  public void delete() throws IOException {
    try {
      getDelegate().delete(org.eclipse.core.resources.IResource.KEEP_HISTORY, null);

    } catch (CoreException | OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  /**
   * Returns an {@link org.eclipse.core.resources.IResource} object representing this resource.
   *
   * @return an {@link org.eclipse.core.resources.IResource} object representing this resource
   */
  public abstract org.eclipse.core.resources.IResource getDelegate();

  @Override
  public int hashCode() {
    return referencePoint.hashCode() + 31 * relativePath.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    AbstractEclipseResource other = (AbstractEclipseResource) obj;

    return this.referencePoint.equals(other.referencePoint)
        && this.relativePath.equals(other.relativePath);
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + " : " + relativePath + " - " + referencePoint + "]";
  }
}
