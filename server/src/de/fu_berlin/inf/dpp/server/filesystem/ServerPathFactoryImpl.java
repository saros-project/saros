package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;

public class ServerPathFactoryImpl implements IPathFactory {

  @Override
  public String fromPath(IPath path) {

    if (path == null) throw new NullPointerException("path is null");

    return checkRelative(path).toString();
  }

  @Override
  public IPath fromString(String name) {

    if (name == null) throw new NullPointerException("name is null");

    return checkRelative(ServerPathImpl.fromString(name));
  }

  private IPath checkRelative(IPath path) {

    if (path.isAbsolute()) throw new IllegalArgumentException("path is absolute: " + path);

    return path;
  }
}
