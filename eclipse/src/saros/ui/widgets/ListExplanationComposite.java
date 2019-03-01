package saros.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * This composite displays a {@link ExplanationComposite} with list items as its content.
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
public class ListExplanationComposite extends ExplanationComposite {
  /**
   * Instances of this class are used to set the contents of an {@link ListExplanationComposite}
   * instance.
   *
   * @see ListExplanationComposite#setExplanation(ListExplanation)
   */
  public static class ListExplanation {
    protected String introductoryText;
    protected String[] listItems;
    protected Image explanationImage;

    /**
     * Constructs a new explanation for use with {@link ListExplanationComposite}.
     *
     * @param introductoryText introduces the list items
     * @param listItems describes the list items
     */
    public ListExplanation(String introductoryText, String... listItems) {
      this(null, introductoryText, listItems);
    }

    /**
     * Constructs a new explanation for use with {@link ListExplanationComposite}.
     *
     * @param systemImage SWT constant that declares a system image (e.g. {@link
     *     SWT#ICON_INFORMATION})
     * @param introductoryText introduces the list items
     * @param listItems describes the list items
     */
    public ListExplanation(int systemImage, String introductoryText, String... listItems) {
      this(Display.getDefault().getSystemImage(systemImage), introductoryText, listItems);
    }

    /**
     * Constructs a new explanation for use with {@link ListExplanationComposite}.
     *
     * @param explanationImage Explanatory image {@link SWT#ICON_INFORMATION})
     * @param introductoryText introduces the list items
     * @param listItems describes the list items
     */
    public ListExplanation(Image explanationImage, String introductoryText, String... listItems) {
      this.introductoryText = introductoryText;
      this.listItems = listItems;
      this.explanationImage = explanationImage;
    }
  }

  /**
   * Instances of this class layout text in the form of an introducing text and a list of items.
   *
   * @see ListExplanationComposite
   */
  protected static class ListExplanationContentComposite extends Composite {
    /** Number of columns used for layout */
    static final int NUM_COLS = 2;

    public ListExplanationContentComposite(
        Composite parent, int style, ListExplanation listExplanation) {
      super(parent, style);

      GridLayout gridLayout = new GridLayout(NUM_COLS, false);
      gridLayout.marginWidth = 0;
      gridLayout.marginHeight = 0;
      this.setLayout(gridLayout);

      /*
       * Introductory text
       */
      if (listExplanation.introductoryText != null) {
        Label introductoryLabel = new Label(this, SWT.WRAP);
        introductoryLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, NUM_COLS, 1));
        introductoryLabel.setText(listExplanation.introductoryText);
      }

      /*
       * List items
       */
      if (listExplanation.listItems != null) {
        for (int i = 0; i < listExplanation.listItems.length; i++) {
          Label stepNumber = new Label(this, SWT.NONE);
          stepNumber.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true));
          stepNumber.setText((i + 1) + ")");

          Label stepContent = new Label(this, SWT.WRAP);
          stepContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
          stepContent.setText(listExplanation.listItems[i]);
        }
      }
    }
  }

  /** This composite holds the contents */
  protected ListExplanationContentComposite contentComposite;

  /**
   * Constructs a new explanation composite.
   *
   * @param parent The parent control
   * @param style Style constants
   */
  public ListExplanationComposite(Composite parent, int style) {
    super(parent, style, null);

    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    super.setLayout(gridLayout);
  }

  /**
   * Constructs a new explanation composite.
   *
   * @param parent The parent control
   * @param style Style constants
   * @param listExplanation Explanation to be displayed by the {@link SimpleExplanationComposite}
   */
  public ListExplanationComposite(Composite parent, int style, ListExplanation listExplanation) {
    this(parent, style);
    setExplanation(listExplanation);
  }

  /**
   * Sets the explanation
   *
   * @param listExplanation Explanation to be displayed by the {@link SimpleExplanationComposite}
   */
  public void setExplanation(ListExplanation listExplanation) {

    /*
     * TODO If we allow listExplanation to be null here, we will get a NPE
     * in the ctor of ListExplanationContentComposite.
     */
    this.setImage((listExplanation != null) ? listExplanation.explanationImage : null);

    if (this.contentComposite != null && !this.contentComposite.isDisposed())
      this.contentComposite.dispose();

    this.contentComposite = new ListExplanationContentComposite(this, SWT.NONE, listExplanation);
    this.contentComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true));

    /*
     * If scrolling is enabled, set minimum size so that scroll bars appear.
     * Because the inner ListExplanationContentComposite uses a GridLayout
     * the composite would collapse without a minimum size.
     */
    if (this.getHorizontalBar() != null || this.getVerticalBar() != null) {
      Point size = this.contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      this.setMinSize(size);
    }

    this.layout();
  }

  @Override
  public void setLayout(Layout layout) {
    // this composite controls its layout itself
  }
}
