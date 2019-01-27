package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/** Server implementation of the {@link IFolder} interface. */
public class ServerFolderImpl extends ServerResourceImplV2 implements IFolder {

  private static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Creates a ServerFolderImpl.
   *
   * @param referencePointsPath the root source's path
   * @param referencePointRelativePath the resource's path relative to root source
   */
  public ServerFolderImpl(IPath referencePointsPath, IPath referencePointRelativePath) {
    super(referencePointsPath, referencePointRelativePath);
  }

  @Override
  public int getType() {
    return IResource.FOLDER;
  }

  @Override
  public void delete(int updateFlags) throws IOException {
    FileUtils.deleteDirectory(getLocation().toFile());
  }

  @Override
  public void move(IPath destination, boolean force) throws IOException {

    IPath destinationBase =
        destination.isAbsolute()
            ? referencePointsPath.removeLastSegments(1)
            : getLocation().removeLastSegments(1);

    IPath absoluteDestination = destinationBase.append(destination);

    FileUtils.moveDirectory(getLocation().toFile(), absoluteDestination.toFile());
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

  @Override
  public boolean exists(IPath path) {
    IPath childLocation = getLocation().append(path);
    return childLocation.toFile().exists();
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
        member = new ServerFolderImpl(referencePointsPath, memberPath);
      } else {
        member = new ServerFileImpl(referencePointsPath, memberPath);
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
  public String getDefaultCharset() {
    // TODO: Read default character set from the project metadata files.
    return DEFAULT_CHARSET;
  }

  @Override
  public IResource findMember(IPath path) {
    if (path.segmentCount() == 0) {
      return this;
    }

    IPath memberLocation = getLocation().append(path);
    File memberFile = memberLocation.toFile();

    if (memberFile.isFile()) {
      return new ServerFileImpl(referencePointsPath, getFullMemberPath(path));
    } else if (memberFile.isDirectory()) {
      return new ServerFolderImpl(referencePointsPath, getFullMemberPath(path));
    } else {
      return null;
    }
  }

  @Override
  public IFile getFile(IPath path) {
    return new ServerFileImpl(referencePointsPath, getFullMemberPath(path));
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(ServerPathImpl.fromString(pathString));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new ServerFolderImpl(referencePointsPath, getFullMemberPath(path));
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(ServerPathImpl.fromString(pathString));
  }

  private IPath getFullMemberPath(IPath memberPath) {
    return getFullPath().append(memberPath);
  }
}
