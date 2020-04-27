package saros.filesystem;

import org.eclipse.core.runtime.Path;

public class EclipsePathFactory implements IPathFactory {

  @Override
  public String fromPath(IPath path) {
    if (path == null) throw new NullPointerException("path is null");

    if (path.isAbsolute()) throw new IllegalArgumentException("path is absolute: " + path);

    return path.toString();
  }

  @Override
  public IPath fromString(String pathString) {
    if (pathString == null) throw new NullPointerException("Given string is null");

    Path path = new Path(pathString);

    if (path.isAbsolute())
      throw new IllegalArgumentException("Given string denotes an absolute path: " + pathString);

    return ResourceAdapterFactory.create(path);
  }
}
