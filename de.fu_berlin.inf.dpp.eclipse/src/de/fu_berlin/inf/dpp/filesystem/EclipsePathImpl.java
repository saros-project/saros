package de.fu_berlin.inf.dpp.filesystem;

import java.io.File;

public class EclipsePathImpl implements IPath {

  private final org.eclipse.core.runtime.IPath delegate;

  EclipsePathImpl(org.eclipse.core.runtime.IPath delegate) {
    if (delegate == null) throw new NullPointerException("delegate is null");

    this.delegate = delegate;
  }

  @Override
  public IPath append(IPath path) {
    return new EclipsePathImpl(delegate.append(((EclipsePathImpl) path).getDelegate()));
  }

  @Override
  public boolean isAbsolute() {
    return delegate.isAbsolute();
  }

  @Override
  public boolean isPrefixOf(IPath path) {
    return delegate.isPrefixOf(((EclipsePathImpl) path).getDelegate());
  }

  @Override
  public String toOSString() {
    return delegate.toOSString();
  }

  @Override
  public String toPortableString() {
    return delegate.toPortableString();
  }

  @Override
  public String segment(int index) {
    return delegate.segment(index);
  }

  @Override
  public String lastSegment() {
    return delegate.lastSegment();
  }

  @Override
  public int segmentCount() {
    return delegate.segmentCount();
  }

  @Override
  public IPath removeLastSegments(int count) {
    return new EclipsePathImpl(delegate.removeLastSegments(count));
  }

  @Override
  public IPath removeFirstSegments(int count) {
    return new EclipsePathImpl(delegate.removeFirstSegments(count));
  }

  @Override
  public String[] segments() {
    return delegate.segments();
  }

  @Override
  public IPath append(String path) {
    return new EclipsePathImpl(delegate.append(path));
  }

  @Override
  public IPath makeAbsolute() {
    return new EclipsePathImpl(delegate.makeAbsolute());
  }

  @Override
  public File toFile() {
    return delegate.toFile();
  }

  /**
   * Returns the original {@link org.eclipse.core.runtime.IPath IPath} object.
   *
   * @return
   */
  public org.eclipse.core.runtime.IPath getDelegate() {
    return delegate;
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof EclipsePathImpl)) return false;

    return delegate.equals(((EclipsePathImpl) obj).delegate);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
