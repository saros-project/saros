package saros.server.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IResource;

/**
 * Server implementation of the {@link IContainer} interface. Every type of container is implemented
 * as a simple directory in the file system.
 */
public abstract class ServerContainerImpl extends ServerResourceImpl implements IContainer {

  /**
   * Creates a ServerContainerImpl.
   *
   * @param workspace the containing workspace
   * @param path the container's path relative to the workspace's root
   */
  public ServerContainerImpl(ServerWorkspaceImpl workspace, Path path) {
    super(workspace, path);
  }

  @Override
  public void delete() throws IOException {
    FileUtils.deleteDirectory(getLocation().toFile());
  }

  @Override
  public List<IResource> members() throws IOException {
    List<IResource> members = new ArrayList<>();

    File[] memberFiles = getLocation().toFile().listFiles();
    if (memberFiles == null) {
      throw new NoSuchFileException(getLocation().toString());
    }

    for (File f : memberFiles) {
      Path memberPath = getFullPath().resolve(f.getName());
      IResource member;

      if (f.isDirectory()) {
        member = new ServerFolderImpl(getWorkspace(), memberPath);
      } else {
        member = new ServerFileImpl(getWorkspace(), memberPath);
      }

      members.add(member);
    }

    return members;
  }

  @Override
  public IFile getFile(Path path) {
    return new ServerFileImpl(getWorkspace(), getFullMemberPath(path));
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(Paths.get(pathString));
  }

  @Override
  public IFolder getFolder(Path path) {
    return new ServerFolderImpl(getWorkspace(), getFullMemberPath(path));
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(Paths.get(pathString));
  }

  private Path getFullMemberPath(Path memberPath) {
    return getFullPath().resolve(memberPath);
  }

  @Override
  public boolean exists(Path relativePath) {
    Path childLocation = getLocation().resolve(relativePath);
    return childLocation.toFile().exists();
  }
}
