package saros.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * This composite displays a system icon or {@link Image} left to its content.<br>
 * Use this composite like every other composite, especially by setting the layout and adding sub
 * {@link Control}s.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>TOP, CENTER, BOTTOM and those supported by Composite
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * <p>This class may be sub classed by custom control implementors who are building controls that
 * are constructed from aggregates of other controls.
 *
 * @see Composite
 * @author bkahlert
 */
public class IllustratedComposite extends MinSizeComposite {
  public static final int STYLES = SWT.TOP | SWT.CENTER | SWT.BOTTOM;

  /** If set to true a border around the {@link #getClientArea() clientArea} is drawn. */
  protected static final boolean DEBUG = false;

  /** The image containing illustrative information. */
  protected Image image;

  /** The space between illustrative image and content */
  protected int spacing = 10;

  /** Used by the paint listener to correctly place the backgroundImage. */
  protected Rectangle cachedClientArea;

  /**
   * Constructs a new {@link IllustratedComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param systemImage SWT constant that declares a system image (e.g. {@link
   *     SWT#ICON_INFORMATION})
   */
  public IllustratedComposite(Composite parent, int style, int systemImage) {
    this(parent, style, parent.getDisplay().getSystemImage(systemImage));
  }

  /**
   * Constructs a new {@link IllustratedComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param image illustrative image
   */
  public IllustratedComposite(Composite parent, final int style, Image image) {
    super(parent, style & ~STYLES);
    setImage(image);

    /*
     * Adds the illustrative image to the left of the contents.
     */
    this.addPaintListener(
        new PaintListener() {
          @Override
          public void paintControl(PaintEvent e) {
            Image currentImage = IllustratedComposite.this.image;
            if (currentImage == null || currentImage.isDisposed()) return;

            Rectangle clientArea = IllustratedComposite.this.cachedClientArea;
            if (clientArea == null) clientArea = getClientArea();

            if (DEBUG) {
              e.gc.drawRectangle(
                  clientArea.x, clientArea.y, clientArea.width - 1, clientArea.height - 1);
            }

            Rectangle bounds = IllustratedComposite.this.getBounds();
            int x, y = 0;
            if (IllustratedComposite.this.getChildren().length == 0) {
              // If no child elements exist, place the icon in the center
              // of the canvas
              x = (bounds.width - currentImage.getBounds().width) / 2;
              y = (bounds.height - currentImage.getBounds().height) / 2;
            } else {
              x = clientArea.x - currentImage.getBounds().width - getSpacing();
              y = (bounds.height - currentImage.getBounds().height) / 2;
            }

            /*
             * Consider SWT.TOP, SWT.CENTER and SWT.BOTTOM
             */
            if ((style & SWT.TOP) != 0) {
              y = 0;
            } else if ((style & SWT.CENTER) != 0) {
              // y is already centered
            } else if ((style & SWT.BOTTOM) != 0) {
              y = bounds.height - currentImage.getBounds().height;
            }

            /*
             * Consider the scroll bars
             */
            ScrollBar hBar = getHorizontalBar();
            if (hBar != null && hBar.isVisible()) y -= hBar.getSize().y / 2;

            e.gc.drawImage(currentImage, x, y);
          }
        });
  }

  @Override
  public void layout(boolean changed) {
    super.layout(changed);
    if (DEBUG) this.redraw();
  }

  /**
   * Returns the space between illustrative image and content
   *
   * @return
   */
  public int getSpacing() {
    return spacing;
  }

  /**
   * Sets the spacing between illustrative image and content
   *
   * @param spacing
   */
  public void setSpacing(int spacing) {
    this.spacing = spacing;
    this.layout();
  }

  /**
   * Sets the illustrative image on the base of an SWT constant
   *
   * @param systemImage SWT constant that declares a system image (e.g. {@link
   *     SWT#ICON_INFORMATION})
   */
  public void setImage(int systemImage) {
    setImage(this.getDisplay().getSystemImage(systemImage));
  }

  /**
   * Sets the illustrative image on the base of an {@link Image}
   *
   * <p>Note: It is the caller's responsibility to dispose the image correctly. <br>
   * You must not dispose the image before the this {@link IllustratedComposite} is disposed.
   *
   * @param image Explanatory image
   */
  public void setImage(Image image) {
    this.image = image;
    layout();
    redraw();
  }

  @Override
  public Rectangle getClientArea() {
    Rectangle clientArea = super.getClientArea();

    int extraLeftMargin = getExtraLeftMargin();

    /*
     * Check whether preferred client size fits into the available size; if
     * not re-compute client size with available size.
     */
    Point newClientSize = this.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    int wHint = SWT.DEFAULT, hHint = SWT.DEFAULT;
    if (newClientSize.x > clientArea.width) {
      /*
       * Because newClientSize contains the trim, we have to subtract the
       * trim for wHint.
       */
      wHint = Math.max(clientArea.width - extraLeftMargin, getMinWidth());
    }
    if (newClientSize.y > clientArea.height) {
      /*
       * Because newClientSize contains the trim, we have to subtract the
       * trim for hHint.
       */
      hHint = Math.max(clientArea.height, getMinHeight());
    }
    newClientSize = this.computeSizeTrimless(wHint, hHint);

    /*
     * Center the unit of image and child composites.
     */
    clientArea.x += extraLeftMargin;
    clientArea.y += Math.max((clientArea.height - newClientSize.y) / 2, 0);

    clientArea.width = newClientSize.x;
    clientArea.height = newClientSize.y;
    this.cachedClientArea = clientArea;

    return clientArea;
  }

  @Override
  public Rectangle computeTrim(int x, int y, int width, int height) {
    if (isNoTrimComputation()) return new Rectangle(x, y, width, height);

    int extraLeftMargin = getExtraLeftMargin();
    x -= extraLeftMargin;
    width += extraLeftMargin;

    if (getImageHeight() > height) {
      height = getImageHeight();
    }

    return super.computeTrim(x, y, width, height);
  }

  /**
   * Gets the extra left margin needed to correctly display the {@link #image} .
   *
   * @return
   */
  protected int getExtraLeftMargin() {
    Point imageSize = getImageSize();
    return imageSize != null ? imageSize.x + getSpacing() : 0;
  }

  /**
   * Returns the {@link #image}'s dimensions.
   *
   * @return null if illustrative image not set
   */
  protected Point getImageSize() {
    if (this.image != null && !this.image.isDisposed()) {
      Rectangle bounds = this.image.getBounds();
      return new Point(bounds.width, bounds.height);
    } else {
      return null;
    }
  }

  /**
   * Returns the {@link #image}'s height.
   *
   * @return 0 if illustrative image not set
   */
  protected int getImageHeight() {
    Point getExplanationImageSize = getImageSize();
    return (getExplanationImageSize != null) ? getExplanationImageSize.y : 0;
  }
}
