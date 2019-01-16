/** */
package de.fu_berlin.inf.dpp.filesystem;

import java.util.ArrayList;
import java.util.List;

/** Eclipse implementation of {@link IWorkspaceRoot}. */
public class EclipseWorkspaceRootImpl implements IWorkspaceRoot {

  org.eclipse.core.resources.IWorkspaceRoot delegate;

  public EclipseWorkspaceRootImpl(org.eclipse.core.resources.IWorkspaceRoot delegate) {
    this.delegate = delegate;
  }

  @Override
  public IFolder[] getReferenceFolders() {

    final List<IFolder> result = new ArrayList<IFolder>();

    for (final org.eclipse.core.resources.IProject project : delegate.getProjects()) {
      result.add(ResourceAdapterFactory.create(project));
    }

    return result.toArray(new IFolder[result.size()]);
  }
}
