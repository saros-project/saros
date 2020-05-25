package saros.server.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
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
  public ServerContainerImpl(ServerWorkspaceImpl workspace, IPath path) {
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
      throw new NoSuchFileException(getLocation().toOSString());
    }

    for (File f : memberFiles) {
      IPath memberPath = getFullPath().append(f.getName());
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
  public IFile getFile(IPath path) {
    return new ServerFileImpl(getWorkspace(), getFullMemberPath(path));
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(ServerPathImpl.fromString(pathString));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new ServerFolderImpl(getWorkspace(), getFullMemberPath(path));
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(ServerPathImpl.fromString(pathString));
  }

  private IPath getFullMemberPath(IPath memberPath) {
    return getFullPath().append(memberPath);
  }

  @Override
  public boolean exists(IPath relativePath) {
    IPath childLocation = getLocation().append(relativePath);
    return childLocation.toFile().exists();
  }
}
