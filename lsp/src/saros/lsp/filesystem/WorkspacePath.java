package saros.lsp.filesystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;

/** Implementation of {@link IWorkspacePath}. */
public class WorkspacePath extends LspPath implements IWorkspacePath {
  public WorkspacePath(URI path) {
    super(Paths.get(path));
  }

  @Override
  public IReferencePoint getReferencePoint(String name) {
    return new LspReferencePoint(this, name);
  }

  @Override
  public IFile tryGetFileFromUri(String uri) {
    IPath path;
    try {
      path = LspPath.fromUri(new URI(uri));
    } catch (URISyntaxException e) {
      return null;
    }
    return new LspFile(this, path);
  }
}
