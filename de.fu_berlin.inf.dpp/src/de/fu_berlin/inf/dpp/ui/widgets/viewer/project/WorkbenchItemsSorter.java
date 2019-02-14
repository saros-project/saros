package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorter for TreeViews of {@link ResourceSelectionComposite} which sorts the Workbench Resources
 * the same way the Package Explorer/Project Explorer/Navigator does:
 *
 * <ol>
 *   <li>Projects
 *   <li>Folders
 *   <li>Files
 * </ol>
 *
 * All sorted alphabetically, case insensitive (default implementation)
 */
public class WorkbenchItemsSorter extends ViewerSorter {
  @Override
  public int category(Object element) {
    IResource treeItem = (IResource) element;

    // We retrieve the IResource Type which is one of
    // #FILE #FOLDER #PROJECT #ROOT
    // and revert the value so that folders are at the top
    return treeItem.getType() * -1;
  }
}
