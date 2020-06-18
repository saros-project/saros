package saros.ui.util.selection.retriever;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import saros.ui.util.selection.retriever.impl.SelectionRetriever;

/**
 * Instances of this class can return an {@link ISelectionRetriever} which is capable of filtering
 * the {@link IWorkbenchPart}'s {@link ISelection}s.
 *
 * <p>The filtered selection consists only of objects that can adapt to AdapterType.
 *
 * <p>For example,
 *
 * <pre>
 * List&lt;JID&gt; contacts = SelectionRetrieverFactory.getSelectionRetriever(JID.class)
 *     .getSelection();
 * </pre>
 *
 * or,
 *
 * <pre>
 * List&lt;IProject&gt; projects = SelectionRetrieverFactory.getSelectionRetriever(
 *     IProject.class).getOverallSelection();
 * </pre>
 */
public class SelectionRetrieverFactory {

  /**
   * Returns a {@link SelectionRetriever} capable of retrieving selections of a specific type.
   *
   * @param <T> type of the returned list
   * @param adapter type the selections should be adapted to
   * @return a {@link SelectionRetriever} capable of retrieving selections of a specific type
   */
  public static <T> ISelectionRetriever<T> getSelectionRetriever(Class<? extends T> adapter) {

    return new SelectionRetriever<>(adapter);
  }
}
