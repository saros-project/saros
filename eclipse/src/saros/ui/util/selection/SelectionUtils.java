package saros.ui.util.selection;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import saros.util.ArrayUtils;

/**
 * Utility class for a convenient work with the {@link ISelectionService}.
 *
 * @author bkahlert
 */
public class SelectionUtils {
  private SelectionUtils() {
    // no instantiation allowed
  }

  /**
   * Returns the {@link ISelectionService selection service}. Will always return <code>null</code>
   * if called from a non-UI thread.
   *
   * @return the current selection service or <code>null</code> if it is not available
   */
  public static ISelectionService getSelectionService() {
    final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

    if (workbenchWindow == null) return null;

    return workbenchWindow.getSelectionService();
  }

  /**
   * Returns the current selection. Will always return <code>null</code> if called from a non-UI
   * thread.
   *
   * @return the current selection, or <code>null</code> if undefined
   * @see ISelectionService#getSelection()
   */
  public static ISelection getSelection() {
    final ISelectionService selectionService = getSelectionService();

    if (selectionService == null) return null;

    return selectionService.getSelection();
  }

  /**
   * Returns the current selection in the given part. Will always return <code>null</code> if called
   * from a non-UI thread.
   *
   * @param partId of the part
   * @return the current selection, or <code>null</code> if undefined
   * @see ISelectionService#getSelection(String)
   */
  public static ISelection getSelection(final String partId) {
    final ISelectionService selectionService = getSelectionService();

    if (selectionService == null) return null;

    return selectionService.getSelection(partId);
  }

  /**
   * Returns all selections made in the current perspective.
   *
   * @return
   */
  public static List<ISelection> getOverallSelections() {
    final List<ISelection> selections = new ArrayList<ISelection>();

    final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

    if (workbenchWindow == null) return selections;

    for (IWorkbenchPage workbenchPage : workbenchWindow.getPages()) {
      for (IEditorReference editorReference : workbenchPage.getEditorReferences()) {

        ISelection selection = getSelection(editorReference.getId());

        if (selection != null) selections.add(selection);
      }

      for (IViewReference viewReference : workbenchPage.getViewReferences()) {

        ISelection selection = getSelection(viewReference.getId());

        if (selection != null) selections.add(selection);
      }
    }

    return selections;
  }

  /**
   * Tries to adapt each selection item to adapter and returns all adapted items.
   *
   * @param selection
   * @param adapter to adapt each object to
   * @return
   */
  public static <T> List<T> getAdaptableObjects(ISelection selection, Class<? extends T> adapter) {
    List<Object> objectsToAdapt = new ArrayList<Object>();

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      for (Object structuredSelectionItem : structuredSelection.toArray()) {
        objectsToAdapt.add(structuredSelectionItem);
      }
    }

    return ArrayUtils.getAdaptableObjects(
        objectsToAdapt.toArray(), adapter, Platform.getAdapterManager());
  }
}
