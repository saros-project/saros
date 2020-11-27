package saros.server.filesystem;

import static saros.filesystem.IResource.Type.FOLDER;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import saros.filesystem.IFolder;

/** Server implementation of the {@link IFolder} interface. */
public class ServerFolderImpl extends ServerContainerImpl implements IFolder {

  /**
   * Creates a ServerFolderImpl.
   *
   * @param workspace the containing workspace
   * @param path the folder's path relative to the workspace's root
   */
  public ServerFolderImpl(ServerWorkspaceImpl workspace, Path path) {
    super(workspace, path);
  }

  @Override
  public Type getType() {
    return FOLDER;
  }

  @Override
  public void create() throws IOException {
    try {
      Files.createDirectory(getLocation());
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
