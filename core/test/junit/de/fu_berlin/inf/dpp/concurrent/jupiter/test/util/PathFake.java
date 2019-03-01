package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import java.io.File;

public class PathFake implements IPath {

  private final String name;

  public PathFake(String name) {
    this.name = name;
  }

  @Override
  public IPath append(IPath path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAbsolute() {
    return false;
  }

  @Override
  public boolean isPrefixOf(IPath path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toOSString() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toPortableString() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PathFake other = (PathFake) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }

  @Override
  public String segment(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String lastSegment() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int segmentCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath removeFirstSegments(int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath removeLastSegments(int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] segments() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath append(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath makeAbsolute() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }
}
