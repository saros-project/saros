package de.fu_berlin.inf.dpp.ui.widgets;

import de.fu_berlin.inf.dpp.ui.util.FontUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * This is a convenience version of {@link RoundedComposite} that allows the easy display of
 * multiple text snippets column-like.<br>
 * Instead of manually adding {@link Label}s to the {@link Composite} and manually laying them out,
 * a single call to <code>setTexts</code> is sufficient.
 *
 * <p><img src="doc-files/SimpleRoundedComposite-1.png"/>
 *
 * @see RoundedComposite
 * @see Composite
 * @author bkahlert
 */
public class SimpleRoundedComposite extends RoundedComposite {
  public static final int MARGIN_WIDTH = 4;
  public static final int MARGIN_HEIGHT = 1;

  private Label[] textLabels = null;

  public SimpleRoundedComposite(Composite parent, int style) {
    super(parent, style);
  }

  /**
   * Clears all existing child elements and adds the provided text to the composite by an implicit
   * creation of {@link Label}s.
   *
   * <p>The added text gets layed out adequately by centering all but the first and last created
   * {@link Label}s.<br>
   * The first one becomes left wheras the last one becomes right aligned.
   *
   * @param texts
   */
  public void setTexts(String[] texts) {
    disposeChildren();

    if (texts == null || texts.length == 0) return;

    /*
     * Sets layout Because the number of grid columns depends on the number
     * of text to display, each time you set the texts the layout needs to
     * be recreated.
     */
    GridLayout gridLayout = new GridLayout(texts.length, false);
    gridLayout.horizontalSpacing = 10;
    gridLayout.verticalSpacing = 0;
    gridLayout.marginWidth = MARGIN_WIDTH;
    gridLayout.marginHeight = MARGIN_HEIGHT;
    super.setLayout(gridLayout);

    textLabels = new Label[texts.length];

    // Places the labels
    for (int i = 0; i < texts.length; i++) {
      String text = texts[i];

      // Calculate layout

      int horizontalAlignment = SWT.CENTER;
      if (texts.length > 1 && i == 0) horizontalAlignment = SWT.LEFT;
      else if (texts.length > 1 && i == (texts.length - 1)) horizontalAlignment = SWT.RIGHT;
      boolean grabExcessHorizontalSpace = (i == (texts.length - 1));

      Label label = new Label(this, SWT.WRAP | horizontalAlignment);
      textLabels[i] = label;

      label.setForeground(getForeground());
      label.setBackground(getBackground());
      label.setText(text);

      FontUtils.changeFontSizeBy(label, -1);
      FontUtils.makeBold(label);

      label.setLayoutData(
          new GridData(horizontalAlignment, SWT.BEGINNING, grabExcessHorizontalSpace, false));
    }

    // Explicitly request layout in case the label(s) changed
    layout();
  }

  /**
   * Convenience method call for <code>setTexts</code> in case you only want to provide a single
   * text.
   *
   * @param text
   */
  public void setText(String text) {
    setTexts(new String[] {text});
  }

  /** Disposes the direct child controls. */
  private void disposeChildren() {
    if (textLabels == null) return;

    for (Label textLabel : textLabels) textLabel.dispose();

    textLabels = null;
  }

  @Override
  public void setForeground(Color color) {
    super.setForeground(color);

    if (textLabels == null) return;

    for (Label textLabel : textLabels) textLabel.setForeground(color);
  }

  @Override
  public void setBackground(Color color) {
    super.setBackground(color);

    if (textLabels == null) return;

    for (Label textLabel : textLabels) textLabel.setBackground(color);
  }

  @Override
  public void setLayout(Layout layout) {
    // this composite controls its layout itself
  }
}
