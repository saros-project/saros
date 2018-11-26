package de.fu_berlin.inf.dpp.ui.util.selection.retriever;

import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Implementations of this interface can retrieve {@link ISelection selections} provided through the
 * {@link ISelectionService selection service} and return all selections which are adaptable to the
 * given adapter type.
 *
 * @param <T> selections need to be adaptable to
 * @author bkahlert
 */
public interface ISelectionRetriever<T> {

  /**
   * Returns the currently selected objects in the active {@link IWorkbenchPart} adapted to the
   * provided adapter.
   *
   * @return a list of the selected and adapted objects, never <code>null</code>
   */
  public List<T> getSelection();

  /**
   * Returns the currently selected objects in the given {@link IWorkbenchPart} adapted to the
   * provided adapter.
   *
   * @param partId id of the part
   * @return a list of the selected and adapted objects, never <code>null</code>
   */
  public List<T> getSelection(String partId);

  /**
   * Returns the currently selected objects in all {@link IWorkbenchPart workbench parts} of opened
   * perspective adapted to the provided adapter.
   *
   * @return a list of the selected and adapted objects, never <code>null</code>
   */
  public List<T> getOverallSelection();
}
