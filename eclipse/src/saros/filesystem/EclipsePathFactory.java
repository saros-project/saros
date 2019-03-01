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
  public IPath fromString(String name) {
    if (name == null) throw new NullPointerException("name is null");

    Path path = new Path(name);

    if (path.isAbsolute())
      throw new IllegalArgumentException("name denotes an absolute path: " + name);

    return ResourceAdapterFactory.create(path);
  }
}
