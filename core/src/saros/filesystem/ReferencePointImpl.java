package saros.filesystem;

public class ReferencePointImpl implements IReferencePoint {

  private final IPath path;

  public ReferencePointImpl(IPath path) {
    if (path == null) throw new IllegalArgumentException("Path is null");

    this.path = path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + path.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ReferencePointImpl other = (ReferencePointImpl) obj;
    if (!path.equals(other.path)) return false;
    return true;
  }

  @Override
  public String toString() {
    return path.toString();
  }
}
