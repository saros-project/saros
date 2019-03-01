package saros.intellij.project.filesystem;

import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;

public class PathFactory implements IPathFactory {
  @Override
  public String fromPath(IPath path) {
    if (path == null) {
      throw new NullPointerException("Given path must not be null.");
    }

    if (path.isAbsolute()) {
      throw new IllegalArgumentException("The given path must not be absolute. path: " + path);
    }

    return path.toPortableString();
  }

  @Override
  public IPath fromString(String name) {
    if (name == null) {
      throw new NullPointerException("Given string must not be null.");
    }

    IPath path = IntelliJPathImpl.fromString(name);

    if (path.isAbsolute()) {
      throw new IllegalArgumentException(
          "The given string must not be represent an absolute path. " + "path: " + path);
    }

    return path;
  }
}
