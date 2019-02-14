package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * This composite displays a simple {@link ExplanationComposite} and allows it's content to be
 * scrollable if the composite becomes to small.
 *
 * <p>This composite does <strong>NOT</strong> handle setting the layout and adding sub {@link
 * Control}s correctly.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link ExplanationComposite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * @see ExplanationComposite
 * @author bkahlert
 */
public class SimpleExplanationComposite extends ExplanationComposite {

  /**
   * Instances of this class are used to set the contents of an {@link SimpleExplanationComposite}
   * instance.
   *
   * @see SimpleExplanationComposite#setExplanation(SimpleExplanation)
   */
  public static class SimpleExplanation {
    protected String explanationText;
    protected Image explanationImage;

    /**
     * Constructs a new explanation for use with {@link SimpleExplanationComposite}.
     *
     * @param explanationText The explanation to be shown next to the image
     */
    public SimpleExplanation(String explanationText) {
      this(null, explanationText);
    }

    /**
     * Constructs a new explanation for use with {@link SimpleExplanationComposite}.
     *
     * @param systemImage SWT constant that declares a system image (e.g. {@link
     *     SWT#ICON_INFORMATION})
     */
    public SimpleExplanation(int systemImage) {
      this(Display.getDefault().getSystemImage(systemImage), null);
    }

    /**
     * Constructs a new explanation for use with {@link SimpleExplanationComposite}.
     *
     * @param explanationImage Explanatory image {@link SWT#ICON_INFORMATION})
     */
    public SimpleExplanation(Image explanationImage) {
      this(explanationImage, null);
    }

    /**
     * Constructs a new explanation for use with {@link SimpleExplanationComposite}.
     *
     * @param systemImage SWT constant that declares a system image (e.g. {@link
     *     SWT#ICON_INFORMATION})
     * @param explanationText The explanation to be shown next to the image
     */
    public SimpleExplanation(int systemImage, String explanationText) {
      this(Display.getDefault().getSystemImage(systemImage), explanationText);
    }

    /**
     * Constructs a new explanation for use with {@link SimpleExplanationComposite}.
     *
     * @param explanationImage Explanatory image {@link SWT#ICON_INFORMATION})
     * @param explanationText The explanation to be shown next to the image
     */
    public SimpleExplanation(Image explanationImage, String explanationText) {
      this.explanationText = explanationText;
      this.explanationImage = explanationImage;
    }
  }

  protected ScrolledComposite scrolledComposite;
  protected Label explanationLabel;

  /**
   * Constructs a new explanation composite.
   *
   * @param parent The parent control
   * @param style Style constants
   */
  public SimpleExplanationComposite(Composite parent, int style) {
    super(parent, style, null);
    super.setLayout(new FillLayout());
  }

  /**
   * Constructs a new explanation composite.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param simpleExplanation Explanation to be displayed by the {@link SimpleExplanationComposite}
   */
  public SimpleExplanationComposite(
      Composite parent, int style, SimpleExplanation simpleExplanation) {
    this(parent, style);
    setExplanation(simpleExplanation);
  }

  /**
   * Sets the explanation content
   *
   * @param simpleExplanation Explanation to be displayed by the {@link SimpleExplanationComposite}
   */
  public void setExplanation(SimpleExplanation simpleExplanation) {
    this.setImage((simpleExplanation != null) ? simpleExplanation.explanationImage : null);
    if (this.explanationLabel != null && !this.explanationLabel.isDisposed()) {
      if (simpleExplanation != null && simpleExplanation.explanationText != null) {
        // label exists, explanation text exists
        this.explanationLabel.setText(simpleExplanation.explanationText);
      } else {
        // label exists, explanation text not exists
        this.explanationLabel.dispose();
      }
    } else {
      if (simpleExplanation != null && simpleExplanation.explanationText != null) {
        // label not exists, explanation text exists
        this.explanationLabel = new Label(this, SWT.WRAP);
        this.explanationLabel.setForeground(this.getForeground());
        this.explanationLabel.setText(simpleExplanation.explanationText);
      } else {
        // label not exists, explanation text not exists
        // do nothing
      }
    }

    this.layout();
  }

  @Override
  public void setLayout(Layout layout) {
    // this composite controls its layout itself
  }
}
