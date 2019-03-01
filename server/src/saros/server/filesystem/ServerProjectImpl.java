package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import java.io.File;

/** Server implementation of the {@link IProject} interface. */
public class ServerProjectImpl extends ServerContainerImpl implements IProject {

  private static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Creates a ServerProjectImpl.
   *
   * @param workspace the containing workspace
   * @param name the project's name
   */
  public ServerProjectImpl(IWorkspace workspace, String name) {
    super(workspace, ServerPathImpl.fromString(name));
  }

  @Override
  public int getType() {
    return IResource.PROJECT;
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
      return new ServerFileImpl(getWorkspace(), getFullMemberPath(path));
    } else if (memberFile.isDirectory()) {
      return new ServerFolderImpl(getWorkspace(), getFullMemberPath(path));
    } else {
      return null;
    }
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
}
