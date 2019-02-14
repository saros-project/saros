package de.fu_berlin.inf.dpp.ui.model;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Instances of this class are used in conjunction with {@link ITreeElement}s as generic {@link
 * ITreeContentProvider}s
 *
 * @author bkahlert
 */
public class TreeContentProvider implements ITreeContentProvider {

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // do nothing
  }

  @Override
  public void dispose() {
    // do nothing
  }

  @Override
  public Object[] getElements(Object inputElement) {
    return getChildren(inputElement);
  }

  @Override
  public Object getParent(Object element) {
    return (element instanceof ITreeElement) ? ((ITreeElement) element).getParent() : null;
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    return (parentElement instanceof ITreeElement)
        ? ((ITreeElement) parentElement).getChildren()
        : new Object[0];
  }

  @Override
  public boolean hasChildren(Object element) {
    return (element instanceof ITreeElement) ? ((ITreeElement) element).hasChildren() : false;
  }
}
