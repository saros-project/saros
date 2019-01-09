/** */
package de.fu_berlin.inf.dpp.filesystem;

import java.util.ArrayList;
import java.util.List;

/** Eclipse implementation of {@link IWorkspaceRoot}. */
public class EclipseWorkspaceRootImpl extends EclipseContainerImpl implements IWorkspaceRoot {

  public EclipseWorkspaceRootImpl(org.eclipse.core.resources.IWorkspaceRoot delegate) {
    super(delegate);
  }

  @Override
  public IProject[] getProjects() {

    final List<IProject> result = new ArrayList<IProject>();

    for (final org.eclipse.core.resources.IProject project :
        ((org.eclipse.core.resources.IWorkspaceRoot) getDelegate()).getProjects()) {
      result.add(ResourceAdapterFactory.create(project));
    }

    return result.toArray(new IProject[result.size()]);
  }

  @Override
  public IFolder_V2[] getReferenceFolders() {

    final List<IFolder_V2> result = new ArrayList<IFolder_V2>();

    for (final org.eclipse.core.resources.IProject project :
        ((org.eclipse.core.resources.IWorkspaceRoot) getDelegate()).getProjects()) {
      result.add(ResourceAdapterFactory.create(project));
    }

    return result.toArray(new IFolder_V2[result.size()]);
  }
}
