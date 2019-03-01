package saros.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import saros.ui.util.FontUtils;

/**
 * This composite displays a simple {@link IllustratedComposite} that can only display text.
 *
 * <p>This composite does <strong>NOT</strong> handle setting the layout and adding sub {@link
 * Control}s correctly.
 *
 * <p><img src="doc-files/SimpleIllustratedComposite-1.png"/>
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>BOLD and those supported by {@link IllustratedComposite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * @see IllustratedComposite
 * @author bkahlert
 */
public class SimpleIllustratedComposite extends IllustratedComposite {
  public static final int LABEL_STYLES = SWT.BOLD;

  /**
   * Instances of this class are used to set the contents of an {@link SimpleIllustratedComposite}
   * instance.
   *
   * @see SimpleIllustratedComposite#setContent(IllustratedText)
   */
  public static class IllustratedText {
    protected String text;
    protected Image image;

    /**
     * Constructs new content for use with {@link SimpleIllustratedComposite}.
     *
     * @param text to be displayed.
     */
    public IllustratedText(String text) {
      this(null, text);
    }

    /**
     * Constructs new content for use with {@link SimpleIllustratedComposite}.
     *
     * @param systemImage SWT constant that declares a system image (e.g. {@link
     *     SWT#ICON_INFORMATION})
     */
    public IllustratedText(int systemImage) {
      this(Display.getDefault().getSystemImage(systemImage), null);
    }

    /**
     * Constructs new content for use with {@link SimpleIllustratedComposite}.
     *
     * @param image to be displayed.
     */
    public IllustratedText(Image image) {
      this(image, null);
    }

    /**
     * Constructs new content for use with {@link SimpleIllustratedComposite}.
     *
     * @param systemImage SWT constant that declares a system image (e.g. {@link
     *     SWT#ICON_INFORMATION})
     * @param text to be displayed next to the image.
     */
    public IllustratedText(int systemImage, String text) {
      this(Display.getDefault().getSystemImage(systemImage), text);
    }

    /**
     * Constructs new content for use with {@link SimpleIllustratedComposite}.
     *
     * @param image Explanatory image {@link SWT#ICON_INFORMATION})
     * @param text to be displayed next to the image.
     */
    public IllustratedText(Image image, String text) {
      this.text = text;
      this.image = image;
    }
  }

  protected Label contentLabel;
  protected boolean isBold;

  /**
   * Constructs a new {@link SimpleIllustratedComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   */
  public SimpleIllustratedComposite(Composite parent, int style) {
    super(parent, style & ~LABEL_STYLES, null);
    super.setLayout(new FillLayout());
    this.isBold = (style & SWT.BOLD) != 0;
  }

  /**
   * Constructs a new {@link SimpleIllustratedComposite}.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param illustratedText to be displayed by the {@link SimpleIllustratedComposite}
   */
  public SimpleIllustratedComposite(Composite parent, int style, IllustratedText illustratedText) {
    this(parent, style);
    setContent(illustratedText);
  }

  /**
   * Sets the explanation content
   *
   * @param illustratedText Explanation to be displayed by the {@link SimpleIllustratedComposite}
   */
  public void setContent(IllustratedText illustratedText) {
    this.setImage((illustratedText != null) ? illustratedText.image : null);
    if (this.contentLabel != null && !this.contentLabel.isDisposed()) {
      if (illustratedText != null && illustratedText.text != null) {
        // label exists, explanation text exists
        this.contentLabel.setText(illustratedText.text);
      } else {
        // label exists, explanation text not exists
        this.contentLabel.dispose();
      }
    } else {
      if (illustratedText != null && illustratedText.text != null) {
        // label not exists, explanation text exists
        this.contentLabel = new Label(this, SWT.WRAP | SWT.BOLD);
        if (this.isBold) FontUtils.makeBold(this.contentLabel);
        this.contentLabel.setForeground(this.getForeground());
        this.contentLabel.setText(illustratedText.text);
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
