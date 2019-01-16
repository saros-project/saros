package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import java.io.IOException;

/** IntelliJ implementation of {@link IWorkspaceRoot}. */
public class IntelliJWorkspaceRootImpl implements IWorkspaceRoot {

  @Override
  public IFolder[] getReferenceFolders() {
    /*
     *  FIXME Implement! As IWorkspaceRoot might not be a sufficient concept for the core filesystem,
     *  it is used in the HTML-UI to create a list of files in the workspace that can be shared.
     *  This IWorspaceRoot implementation is needed to avoid IntellJ crash when activating the HTML UI.
     *  Until the core filesystem is reworked, this throws an Exception to indicate that the implementation is missing.
     */
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(IPath path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IResource[] members() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IResource[] members(int memberFlags) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDefaultCharset() throws IOException {
    return null;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public IPath getFullPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFolder getParentFolder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFolder getReferenceFolder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getProjectRelativePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getType() {
    return IResource.ROOT;
  }

  @Override
  public boolean isDerived(boolean checkAncestors) {
    return false;
  }

  @Override
  public boolean isDerived() {
    return false;
  }

  @Override
  public void delete(int updateFlags) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void move(IPath destination, boolean force) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getLocation() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAdapter(Class<? extends IResource> clazz) {
    return null;
  }

  @Override
  public IReferencePoint getReferencePoint() {
    throw new UnsupportedOperationException();
  }
}
