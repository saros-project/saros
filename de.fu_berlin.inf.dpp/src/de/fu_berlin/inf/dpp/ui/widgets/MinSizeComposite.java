package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * This composite guarantees that its client area does not fall below the specified size.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>Styles supported by {@link Composite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * <p>This class may be sub-classed by custom control implementors who are building controls that
 * are constructed from aggregates of other controls.
 *
 * @see Composite
 * @author bkahlert
 */
public class MinSizeComposite extends Composite {

  protected int minHeight = 0;
  protected int minWidth = 0;
  protected boolean noTrimComputation = false;

  /**
   * Constructs a new {@link MinSizeComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   */
  public MinSizeComposite(Composite parent, final int style) {
    super(parent, style);

    /*
     * Make sure child widgets respect transparency
     */
    this.setBackgroundMode(SWT.INHERIT_DEFAULT);
  }

  /**
   * Returns the minimum width of the content control.
   *
   * @return the minimum width
   * @exception SWTException
   *     <ul>
   *       <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *       <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver
   *     </ul>
   */
  public int getMinWidth() {
    checkWidget();
    return minWidth;
  }

  /**
   * Specify the minimum width at which the ScrolledComposite will begin scrolling the content with
   * the horizontal scroll bar. This value is only relevant if setExpandHorizontal(true) has been
   * set.
   *
   * @param width the minimum width or 0 for default width
   * @exception SWTException
   *     <ul>
   *       <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *       <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver
   *     </ul>
   */
  public void setMinWidth(int width) {
    setMinSize(width, minHeight);
  }

  /**
   * Returns the minimum height of the content control.
   *
   * @return the minimum height
   * @exception SWTException
   *     <ul>
   *       <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *       <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver
   *     </ul>
   */
  public int getMinHeight() {
    checkWidget();
    return minHeight;
  }

  /**
   * Specify the minimum height at which the ScrolledComposite will begin scrolling the content with
   * the vertical scroll bar. This value is only relevant if setExpandVertical(true) has been set.
   *
   * @param height the minimum height or 0 for default height
   * @exception SWTException
   *     <ul>
   *       <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *       <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver
   *     </ul>
   */
  public void setMinHeight(int height) {
    setMinSize(minWidth, height);
  }

  /**
   * Specify the minimum width and height at which the ScrolledComposite will begin scrolling the
   * content with the horizontal scroll bar. This value is only relevant if
   * setExpandHorizontal(true) and setExpandVertical(true) have been set.
   *
   * @param size the minimum size or null for the default size
   * @exception SWTException
   *     <ul>
   *       <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *       <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver
   *     </ul>
   */
  public void setMinSize(Point size) {
    if (size == null) {
      setMinSize(0, 0);
    } else {
      setMinSize(size.x, size.y);
    }
  }

  /**
   * Specify the minimum width and height at which the ScrolledComposite will begin scrolling the
   * content with the horizontal scroll bar. This value is only relevant if
   * setExpandHorizontal(true) and setExpandVertical(true) have been set.
   *
   * @param width the minimum width or 0 for default width
   * @param height the minimum height or 0 for default height
   * @exception SWTException
   *     <ul>
   *       <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *       <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver
   *     </ul>
   */
  public void setMinSize(int width, int height) {
    checkWidget();
    if (width == minWidth && height == minHeight) return;
    minWidth = Math.max(0, width);
    minHeight = Math.max(0, height);
    layout(false);
  }

  @Override
  public Rectangle getClientArea() {
    Rectangle clientArea = super.getClientArea();

    /*
     * Check minimum size
     */
    if (clientArea.width < minWidth) clientArea.width = minWidth;
    if (clientArea.height < minHeight) clientArea.height = minHeight;

    return clientArea;
  }

  /**
   * Tries to determine the trim
   *
   * @return determine trim for {@link Rectangle}(0, 0, 0, 0)
   */
  protected Point determineTrim() {
    Rectangle trim = computeTrim(0, 0, 0, 0);
    return new Point(-trim.x + trim.width, -trim.y + trim.height);
  }

  /**
   * Hint for {@link #computeTrim(int, int, int, int)} whether to compute the trim.
   *
   * @return
   */
  protected boolean isNoTrimComputation() {
    return noTrimComputation;
  }

  /**
   * Sets the flag whether {@link #computeTrim(int, int, int, int)} should compute the trim.
   *
   * <p>The flag only works if taken into account by {@link #computeSizeTrimless(int, int)}.
   *
   * @param noTrimComputation
   */
  protected void setNoTrimComputation(boolean noTrimComputation) {
    this.noTrimComputation = noTrimComputation;
  }

  /**
   * In contrast to {@link #computeSize(int, int)} the computed size does not contain the trim.
   *
   * @param wHint
   * @param hHint
   * @return
   */
  public synchronized Point computeSizeTrimless(int wHint, int hHint) {
    this.setNoTrimComputation(true);
    Point size = super.computeSize(wHint, hHint);
    this.setNoTrimComputation(false);
    return size;
  }
}
