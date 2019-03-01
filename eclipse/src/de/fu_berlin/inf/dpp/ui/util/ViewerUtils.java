package de.fu_berlin.inf.dpp.ui.util;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for manipulation of {@link Viewer Viewers}.
 *
 * <p>Tries to call alternative methods if a special operation is not supported by the viewer. All
 * calls are automatically done in the synchronous SWT thread.
 *
 * <p><b>Example 1:</b><br>
 * If you try to update a specific element in a viewer and the viewer does not support updating
 * single elements, the whole viewer will be refreshed.
 *
 * <p><b>Example 2:</b><br>
 * One element of your model has been removed. For performance reasons you don't want to refresh the
 * whole viewer but manually remove the element from the viewer in order to reflect the model. If
 * your viewer supports this action the element is removed. Otherwise the viewer is advised to
 * reload the model.
 *
 * @author bkahlert
 */
public class ViewerUtils {

  private ViewerUtils() {
    // no instantiation allowed
  }

  /**
   * Sets a viewer's input and makes sure it runs in the SWT thread
   *
   * @param viewer
   * @param input
   * @see Viewer#setInput(Object)
   */
  public static void setInput(final Viewer viewer, final Object input) {
    Display.getDefault()
        .asyncExec(
            new Runnable() {
              @Override
              public void run() {
                if (viewer == null || viewer.getControl().isDisposed()) return;

                viewer.setInput(input);
              }
            });
  }

  /**
   * Updates a viewer's element and makes sure it runs in the SWT thread. Runs a refresh in case the
   * viewer does not support updates.
   *
   * @param viewer
   * @param element
   * @param properties
   * @see StructuredViewer#update(Object, String[])
   */
  public static void update(final Viewer viewer, final Object element, final String[] properties) {
    Display.getDefault()
        .asyncExec(
            new Runnable() {
              @Override
              public void run() {
                if (viewer == null || viewer.getControl().isDisposed()) return;

                if (viewer instanceof StructuredViewer) {
                  StructuredViewer structuredViewer = (StructuredViewer) viewer;
                  structuredViewer.update(element, properties);
                } else {
                  viewer.refresh();
                }
              }
            });
  }

  /**
   * Refreshes a viewer's display and makes sure it runs in the SWT thread.
   *
   * @param viewer
   * @param updateLabels
   * @see Viewer#refresh()
   * @see StructuredViewer#refresh(boolean)
   */
  public static void refresh(final Viewer viewer, final boolean updateLabels) {
    Display.getDefault()
        .asyncExec(
            new Runnable() {
              @Override
              public void run() {
                if (viewer == null || viewer.getControl().isDisposed()) return;

                if (viewer instanceof StructuredViewer) {
                  StructuredViewer structuredViewer = (StructuredViewer) viewer;
                  structuredViewer.refresh(updateLabels);
                } else {
                  viewer.refresh();
                }
              }
            });
  }

  /** @see AbstractTreeViewer#expandToLevel(int) */
  public static void expandToLevel(final Viewer viewer, final int level) {
    Display.getDefault()
        .asyncExec(
            new Runnable() {
              @Override
              public void run() {
                if (viewer == null || viewer.getControl().isDisposed()) return;

                if (viewer instanceof AbstractTreeViewer) {
                  AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
                  treeViewer.expandToLevel(level);
                }
              }
            });
  }

  /**
   * If supported by the viewer expands all elements and makes sure it runs in the SWT thread.
   *
   * @param viewer
   * @see AbstractTreeViewer#expandAll()
   */
  public static void expandAll(final Viewer viewer) {
    Display.getDefault()
        .asyncExec(
            new Runnable() {
              @Override
              public void run() {
                if (viewer == null || viewer.getControl().isDisposed()) return;

                if (viewer instanceof AbstractTreeViewer) {
                  AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
                  treeViewer.expandAll();
                }
              }
            });
  }
}
