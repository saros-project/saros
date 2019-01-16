package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

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
  public ServerContainerImpl(IWorkspace workspace, IPath path) {
    super(workspace, path);
  }

  @Override
  public void delete(int updateFlags) throws IOException {
    FileUtils.deleteDirectory(getLocation().toFile());
  }

  @Override
  public void move(IPath destination, boolean force) throws IOException {
    IPath destinationBase =
        destination.isAbsolute()
            ? getWorkspace().getLocation()
            : getLocation().removeLastSegments(1);

    IPath absoluteDestination = destinationBase.append(destination);
    FileUtils.moveDirectory(getLocation().toFile(), absoluteDestination.toFile());
  }

  @Override
  public IResource[] members() throws IOException {
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

    return members.toArray(new IResource[members.size()]);
  }

  @Override
  public IResource[] members(int memberFlags) throws IOException {
    return members();
  }

  @Override
  public boolean exists(IPath path) {
    IPath childLocation = getLocation().append(path);
    return childLocation.toFile().exists();
  }

  @Override
  public String getDefaultCharset() throws IOException {
    return getParent().getDefaultCharset();
  }
}
