package saros.lsp.filesystem;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import saros.filesystem.IPath;

/**
 * Saros language server implementation of {@link IPath}.
 *
 * @implNote Based on the server implementation
 */
public class LspPath implements IPath {
  private Path delegate;

  public static final IPath EMPTY = new LspPath(Paths.get(""));

  public static IPath fromString(String pathString) {
    if (pathString == null || pathString.isEmpty()) {
      return EMPTY;
    }

    return new LspPath(Paths.get(pathString));
  }

  public static IPath fromUri(URI uri) {
    if (uri == null || !uri.getScheme().equals("file")) {
      return EMPTY;
    }

    return new LspPath(Paths.get(uri));
  }

  protected LspPath(Path delegate) {
    // LOG.info(String.format("tostring: %s", delegate.toString()));
    // LOG.info(String.format("absolute: %s", delegate.toAbsolutePath()));
    /*
     * OpenJDK 7 on Linux has a bug which causes normalize() to throw an
     * ArrayIndexOutOfBoundsException if called on the empty path.
     */
    this.delegate = delegate.equals(Paths.get("")) ? delegate : delegate.normalize();
  }

  @Override
  public String[] segments() {
    String[] segments = new String[segmentCount()];
    for (int i = 0; i < segments.length; i++) {
      segments[i] = delegate.getName(i).toString();
    }
    return segments;
  }

  @Override
  public int segmentCount() {
    return (this == EMPTY) ? 0 : delegate.getNameCount();
  }

  @Override
  public String segment(int index) {
    return delegate.getName(index).toString();
  }

  @Override
  public String lastSegment() {
    return (this == EMPTY) ? null : delegate.getFileName().toString();
  }

  @Override
  public IPath removeFirstSegments(int count) {
    if (count == 0) {
      return this;
    } else if (count >= segmentCount()) {
      return EMPTY;
    }
    Path newDelegate = delegate.subpath(count, delegate.getNameCount());
    return new LspPath(newDelegate);
  }

  @Override
  public IPath removeLastSegments(int count) {
    if (count == 0) {
      return this;
    } else if (count >= segmentCount()) {
      return EMPTY;
    }

    int newCount = delegate.getNameCount() - count;
    Path newDelegate = delegate.subpath(0, newCount);

    /*
     * java.nio.Path#subpath always returns a relative path, but we want an
     * absolute path with last segments removed to still be absolute.
     */
    if (isAbsolute()) {
      newDelegate = delegate.getRoot().resolve(newDelegate);
    }

    return new LspPath(newDelegate);
  }

  @Override
  public boolean isAbsolute() {
    return delegate.isAbsolute();
  }

  @Override
  public IPath makeAbsolute() {
    if (isAbsolute()) {
      return this;
    }
    Path root = delegate.toAbsolutePath().getRoot();
    Path newDelegate = root.resolve(delegate);
    return new LspPath(newDelegate);
  }

  @Override
  public boolean isPrefixOf(IPath path) {
    if (this == EMPTY) {
      return true;
    }
    Path other = Paths.get(path.toOSString());
    return other.startsWith(delegate);
  }

  @Override
  public IPath append(IPath path) {
    Path other = Paths.get(path.toOSString());

    /*
     * Appending an absolute path should simply append the path's segments
     * and ignore everything else. To make this work with
     * java.nio.Path#resolve, we need to make the path relative first.
     */
    if (other.isAbsolute()) {
      other = other.getRoot().relativize(other);
    }

    Path newDelegate = delegate.resolve(other);
    return new LspPath(newDelegate);
  }

  @Override
  public IPath append(String pathString) {
    return append(LspPath.fromString(pathString));
  }

  @Override
  public String toPortableString() {
    return toOSString().replace(File.separatorChar, '/');
  }

  @Override
  public String toOSString() {
    return delegate.toString();
  }

  @Override
  public File toFile() {
    return delegate.toFile();
  }

  @Override
  public String toString() {
    return toPortableString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof LspPath)) return false;

    LspPath other = (LspPath) obj;
    return delegate.equals(other.delegate);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  /**
   * Returns the {@link java.nio.files.Path} underlying this path object. For internal use only.
   *
   * @return the underlying {@link java.nio.files.Path}
   */
  Path getDelegate() {
    return delegate;
  }
}
