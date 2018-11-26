package de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl;

import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.ISelectionRetriever;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;

/**
 * This class implements a generic {@link ISelectionRetriever} which retrieves selections which are
 * adaptable to AdapterType.
 *
 * <p>E.g. if you wish to retrieve all selected {@link IFile}s
 *
 * @param <T> selections need to be adaptable to
 * @author bkahlert
 */
public class SelectionRetriever<T> implements ISelectionRetriever<T> {

  public Class<? extends T> adapter;

  public SelectionRetriever(Class<? extends T> adapter) {
    this.adapter = adapter;
  }

  @Override
  public List<T> getSelection() {
    return getSelection(null);
  }

  @Override
  public List<T> getSelection(final String partId) {
    final ISelection selection =
        (partId == null) ? SelectionUtils.getSelection() : SelectionUtils.getSelection(partId);

    return SelectionUtils.getAdaptableObjects(selection, adapter);
  }

  @Override
  public List<T> getOverallSelection() {
    final List<T> objects = new ArrayList<T>();
    final List<ISelection> selections = SelectionUtils.getOverallSelections();

    for (ISelection selection : selections) {
      objects.addAll(SelectionUtils.getAdaptableObjects(selection, adapter));
    }

    return objects;
  }
}
