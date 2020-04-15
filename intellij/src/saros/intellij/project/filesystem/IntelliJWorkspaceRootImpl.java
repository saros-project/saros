package saros.intellij.project.filesystem;

import static saros.filesystem.IResource.Type.ROOT;

import saros.filesystem.IContainer;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspaceRoot;

/** Intellij implementation of {@link IWorkspaceRoot}. */
public class IntelliJWorkspaceRootImpl implements IWorkspaceRoot {

  @Override
  public IProject[] getProjects() {
    /*
     *  FIXME Implement! As IWorkspaceRoot might not be a sufficient concept for the core filesystem,
     *  it is used in the HTML-UI to create a list of files in the workspace that can be shared.
     *  This IWorkspaceRoot implementation is needed to avoid Intellij crash when activating the HTML UI.
     *  Until the core filesystem is reworked, this throws an Exception to indicate that the implementation is missing.
     */
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(IPath path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IResource[] members() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDefaultCharset() {
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
  public IContainer getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProject getProject() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getProjectRelativePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getType() {
    return ROOT;
  }

  @Override
  public boolean isIgnored() {
    return false;
  }

  @Override
  public void delete() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getLocation() {
    throw new UnsupportedOperationException();
  }
}
