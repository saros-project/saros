package saros.ui.widgets;

import java.util.ResourceBundle.Control;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import saros.ui.util.LayoutUtils;

/**
 * A {@link RoundedComposite} that contains an easily extendible {@link IllustratedComposite}.
 *
 * <p>In order to add your own {@link Control}s you need to:
 *
 * <ol>
 *   <li>Extend this class
 *   <li>Implement {@link #createContent(Composite)}
 * </ol>
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>All styles supported by {@link IllustratedComposite} and {@link RoundedComposite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 */
public abstract class NoteComposite extends RoundedComposite {

  public static final int SPACING = 10;

  protected IllustratedComposite illustratedComposite;

  public NoteComposite(Composite parent, int style) {
    super(parent, style & ~IllustratedComposite.STYLES);

    this.illustratedComposite =
        new IllustratedComposite(this, style & IllustratedComposite.STYLES, null);
    this.illustratedComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    this.illustratedComposite.setLayout(getContentLayout());

    createContent(this.illustratedComposite);

    /*
     * If no background should be drawn we still need to set the background
     * color on the RoundedComposite in order to get the correct border
     * color.
     */
    if ((style & SWT.NO_BACKGROUND) == 0) {
      // this.setForeground(this.getDisplay()
      // .getSystemColor(SWT.COLOR_WHITE));
      this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    } else {
      super.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    }

    this.setSpacing(SPACING);
    this.layout();
  }

  /**
   * Returns the {@link Layout} used to arrange the inner composite's child {@link Control}s.
   *
   * @return
   */
  public abstract Layout getContentLayout();

  /**
   * This method fills the inner composite with child {@link Control}s.
   *
   * @param parent
   */
  public abstract void createContent(Composite parent);

  public void setImage(int systemImage) {
    this.illustratedComposite.setImage(systemImage);
  }

  public void setImage(Image image) {
    this.illustratedComposite.setImage(image);
  }

  @Override
  public void setForeground(Color color) {
    super.setForeground(color);
    this.illustratedComposite.setForeground(color);
  }

  @Override
  public void setBackground(Color color) {
    super.setBackground(color);
    this.illustratedComposite.setBackground(color);
  }

  public void setSpacing(int spacing) {
    super.setLayout(LayoutUtils.createGridLayout(spacing, 0));
    this.illustratedComposite.setSpacing(spacing);
    this.layout();
  }

  @Override
  public void setLayout(Layout layout) {
    // do nothing
  }
}
