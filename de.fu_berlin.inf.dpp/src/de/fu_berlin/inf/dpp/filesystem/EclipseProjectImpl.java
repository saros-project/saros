package de.fu_berlin.inf.dpp.filesystem;

public class EclipseProjectImpl extends EclipseAbstractFolderImpl implements IFolder {

  public EclipseProjectImpl(org.eclipse.core.resources.IProject delegate) {
    super(delegate);
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IProject IProject} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IProject getDelegate() {
    return (org.eclipse.core.resources.IProject) delegate;
  }
}
