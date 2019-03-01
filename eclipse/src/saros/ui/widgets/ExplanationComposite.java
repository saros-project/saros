package saros.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This composite displays a descriptive icon and a {@link Control} that displays some information /
 * explication. <br>
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
public class ExplanationComposite extends IllustratedComposite {

  /** The space between illustrative image and content */
  private static final int EXPLANATION_COMPOSITE_SPACING = 20;

  /**
   * Constructs a new {@link ExplanationComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param systemImage SWT constant that declares a system image (e.g. {@link
   *     SWT#ICON_INFORMATION})
   */
  public ExplanationComposite(Composite parent, int style, int systemImage) {
    super(parent, style, parent.getDisplay().getSystemImage(systemImage));
    setSpacing(EXPLANATION_COMPOSITE_SPACING);
  }

  /**
   * Constructs a new {@link ExplanationComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param explanationImage Explanatory image
   */
  public ExplanationComposite(Composite parent, final int style, Image explanationImage) {
    super(parent, style, explanationImage);
    setSpacing(EXPLANATION_COMPOSITE_SPACING);
  }

  @Override
  public Rectangle getClientArea() {
    Rectangle bounds = getBounds();
    Rectangle clientArea = super.getClientArea();
    int extraLeftMargin = getExtraLeftMargin();

    /*
     * Center the unit of explanationImage and child composites.
     */
    clientArea.x =
        Math.max((bounds.width - clientArea.width + extraLeftMargin) / 2, extraLeftMargin);
    clientArea.y = Math.max((bounds.height - clientArea.height) / 2, 0);

    this.cachedClientArea = clientArea;

    return clientArea;
  }
}
