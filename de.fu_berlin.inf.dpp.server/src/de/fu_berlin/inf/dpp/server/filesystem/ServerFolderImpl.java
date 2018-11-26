package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Server implementation of the {@link IFolder} interface. */
public class ServerFolderImpl extends ServerContainerImpl implements IFolder {

  /**
   * Creates a ServerFolderImpl.
   *
   * @param workspace the containing workspace
   * @param path the folder's path relative to the workspace's root
   */
  public ServerFolderImpl(IWorkspace workspace, IPath path) {
    super(workspace, path);
  }

  @Override
  public int getType() {
    return IResource.FOLDER;
  }

  @Override
  public void create(int updateFlags, boolean local) throws IOException {
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

  @Override
  public void create(boolean force, boolean local) throws IOException {
    create(IResource.NONE, local);
  }
}
