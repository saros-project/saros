package saros.filesystem;

import org.eclipse.core.resources.IWorkspaceRoot;

/** Eclipse {@link IContainer} implementation representing an {@link IWorkspaceRoot}. */
public class EclipseWorkspaceRootImpl extends EclipseContainerImpl {

  public EclipseWorkspaceRootImpl(IWorkspaceRoot delegate) {
    super(delegate);
  }
}
