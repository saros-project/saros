package saros.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Layout;

/**
 * Creates often used {@link Layout}s and {@link Layout} data.
 *
 * <p>The aim of this utility class is to limit layout variations in order to create a more
 * consistent look.
 */
public class LayoutUtils {

  private LayoutUtils() {
    // no instantiation allowed
  }

  /**
   * Creates a {@link GridLayout} with the the given parameters
   *
   * @param numColumns
   * @param makeColumnsEqualWidth
   * @param marginWidth
   * @param marginHeight
   * @param horizontalSpacing
   * @param verticalSpacing
   * @return
   */
  public static GridLayout createGridLayout(
      int numColumns,
      boolean makeColumnsEqualWidth,
      int marginWidth,
      int marginHeight,
      int horizontalSpacing,
      int verticalSpacing) {
    GridLayout layout = new GridLayout(numColumns, makeColumnsEqualWidth);
    layout.marginWidth = marginWidth;
    layout.marginHeight = marginHeight;
    layout.horizontalSpacing = horizontalSpacing;
    layout.verticalSpacing = verticalSpacing;
    return layout;
  }

  /**
   * Creates a {@link GridLayout} with the the given parameters
   *
   * @param numColumns
   * @param makeColumnsEqualWidth
   * @param margin used for marginWidth and marginHeight
   * @param spacing used horizontalSpacing and verticalSpacing
   * @return
   */
  public static GridLayout createGridLayout(
      int numColumns, boolean makeColumnsEqualWidth, int margin, int spacing) {
    return createGridLayout(numColumns, makeColumnsEqualWidth, margin, margin, spacing, spacing);
  }

  /**
   * Creates a {@link GridLayout} with one column and the the given parameters
   *
   * @param margin used for marginWidth and marginHeight
   * @param spacing used horizontalSpacing and verticalSpacing
   * @return
   */
  public static Layout createGridLayout(int margin, int spacing) {
    return createGridLayout(1, false, margin, spacing);
  }

  /**
   * Creates a {@link GridLayout} with one column, no margin and the default {@link
   * GridLayout#horizontalSpacing} and {@link GridLayout#verticalSpacing}.
   *
   * @return
   */
  public static GridLayout createGridLayout() {
    return createGridLayout(
        1, false, 0, 0, new GridLayout().horizontalSpacing, new GridLayout().verticalSpacing);
  }

  /**
   * Create a {@link GridData} that fills its cell and grabs the space.
   *
   * @return
   */
  public static GridData createFillGridData() {
    return new GridData(SWT.FILL, SWT.FILL, true, true);
  }

  /**
   * Create a {@link GridData} that fills its cell and grabs the horizontal space.
   *
   * @return
   */
  public static GridData createFillHGrabGridData() {
    return new GridData(SWT.FILL, SWT.FILL, true, false);
  }
}
