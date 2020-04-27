package saros.server.filesystem;

import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;

public class ServerPathFactoryImpl implements IPathFactory {

  @Override
  public String fromPath(IPath path) {

    if (path == null) throw new NullPointerException("path is null");

    return checkRelative(path).toString();
  }

  @Override
  public IPath fromString(String pathString) {

    if (pathString == null) throw new NullPointerException("given string is null");

    return checkRelative(ServerPathImpl.fromString(pathString));
  }

  private IPath checkRelative(IPath path) {

    if (path.isAbsolute())
      throw new IllegalArgumentException("given string represents an absolute path: " + path);

    return path;
  }
}
