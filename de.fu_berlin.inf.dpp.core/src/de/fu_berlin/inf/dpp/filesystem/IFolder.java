package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

public interface IFolder extends IResource {

  public boolean exists(IPath path);

  public IResource[] members() throws IOException;

  public IResource[] members(int memberFlags) throws IOException;

  public String getDefaultCharset() throws IOException;

  public void create(int updateFlags, boolean local) throws IOException;

  public void create(boolean force, boolean local) throws IOException;

  public IResource findMember(IPath path);

  public IFile getFile(String name);

  public IFile getFile(IPath path);

  public IFolder getFolder(String name);

  public IFolder getFolder(IPath path);
}
