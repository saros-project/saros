package saros.lsp.filesystem;

import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;

/** Saros language server implementation of {@link IPathFactory}. */
public class PathFactory implements IPathFactory {

  @Override
  public String fromPath(IPath path) {

    if (path == null) throw new NullPointerException("path is null");

    return checkRelative(path).toString();
  }

  @Override
  public IPath fromString(String name) {

    if (name == null) throw new NullPointerException("name is null");

    return checkRelative(LspPath.fromString(name));
  }

  private IPath checkRelative(IPath path) {

    if (path.isAbsolute()) throw new IllegalArgumentException("path is absolute: " + path);

    return path;
  }
}
