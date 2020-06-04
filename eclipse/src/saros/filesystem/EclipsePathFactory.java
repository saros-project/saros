package saros.filesystem;

public class EclipsePathFactory implements IPathFactory {

  @Override
  public String fromPath(IPath path) {
    if (path == null) throw new NullPointerException("path is null");

    if (path.isAbsolute()) throw new IllegalArgumentException("path is absolute: " + path);

    return path.toString();
  }

  @Override
  public IPath fromString(String pathString) {
    return ResourceConverter.convertToPath(pathString);
  }
}
