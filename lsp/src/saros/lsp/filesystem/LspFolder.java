package saros.lsp.filesystem;

import static saros.filesystem.IResource.Type.FOLDER;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;

/**
 * Saros language server implementation of {@link IFolder}.
 *
 * @implNote Based on the server implementation
 */
public class LspFolder extends LspContainer implements IFolder {
  public LspFolder(IWorkspacePath workspace, IPath path) {
    super(workspace, path);
  }

  @Override
  public Type getType() {
    return FOLDER;
  }

  @Override
  public void create() throws IOException {
    try {
      Files.createDirectory(toNioPath());
    } catch (FileAlreadyExistsException e) {
      /*
       * That the resource already exists is only a problem for us if it's
       * not a directory.
       */
      if (!Files.isDirectory(Paths.get(e.getFile()))) {
        throw e;
      }
    }
  }
}
