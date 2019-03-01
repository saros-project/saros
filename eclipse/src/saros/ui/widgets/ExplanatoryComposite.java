package saros.ui.widgets;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Instances of this class are controls which are capable of containing exactly <strong>one</strong>
 * content {@link Control} and {@link ExplanationComposite} s to display explanatory information.
 * <br>
 * Although this composite may be subclasses <strong>only the control registered trough {@link
 * ExplanatoryComposite#setContentControl(Control)} can be displayed</strong>.
 *
 * <p>
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>Styles supported by {@link Composite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 *   <dt><b>Example:</b>
 *   <dd>
 *       <pre>
 * <code>
 * final ExplanatoryComposite explanatoryComposite = new ExplanatoryComposite(
 *      tabFolder, SWT.NONE);
 *
 *      final ExplanationComposite expl = new ExplanationComposite(explanatoryComposite, SWT.NONE, SWT.ICON_INFORMATION);
 *      Composite explContent = new Composite(expl, SWT.NONE);
 *      explContent.setLayout(new FillLayout());
 *      Button explContent_hide = new Button(explContent, SWT.PUSH);
 *      explContent_hide.setText("Hide the explanation...");
 *      explContent_hide.addSelectionListener(new SelectionAdapter() {
 *              public void widgetSelected(SelectionEvent e) {
 *                    explanatoryComposite.hideExplanation();
 *              }
 *      });
 *
 *      Button contentControl = new Button(explanatoryComposite, SWT.PUSH);
 *      explanatoryComposite.setContentControl(contentControl);
 *      contentControl.setText("Show the explanation...");
 *      contentControl.addSelectionListener(new SelectionAdapter() {
 *              public void widgetSelected(SelectionEvent e) {
 *                      explanatoryComposite.showExplanation(expl);
 *              }
 *      });
 * </code>
 * </pre>
 * </dl>
 *
 * @see ExplanationComposite
 * @see Composite
 * @author bkahlert
 */
public class ExplanatoryComposite extends Composite {
  /** Uses a {@link StackLayout} to allow the switch between the contents and explanations. */
  protected StackLayout stackLayout = new StackLayout();

  protected Control contentControl;

  /**
   * Because we want the content (and not the explanation) layer to be initially displayed, we add a
   * PaintListener which sets marks the eventually added content layer as the visible layer.
   */
  protected PaintListener showContentControlListener =
      new PaintListener() {
        @Override
        public void paintControl(PaintEvent e) {
          ExplanatoryComposite.this.removePaintListener(showContentControlListener);
          hideExplanation();
        }
      };

  /**
   * Constructs a new {@link ExplanatoryComposite} with a given parent and the passed style
   * information.
   *
   * @param parent
   * @param style
   */
  public ExplanatoryComposite(Composite parent, int style) {
    super(parent, style);

    super.setLayout(stackLayout);
    this.addPaintListener(showContentControlListener);
  }

  /**
   * Hides the content and displays an explanation.
   *
   * @param composite The explanation to be displayed; if null the explanation gets hidden
   */
  public void showExplanation(ExplanationComposite composite) {

    if (composite == null) {
      if (contentControl == null)
        throw new IllegalStateException("The content control was not set!", null);

      this.stackLayout.topControl = contentControl;
      if (contentControl instanceof Composite) {
        ((Composite) contentControl).layout();
      }
    } else {
      this.stackLayout.topControl = composite;
    }

    ExplanatoryComposite.this.removePaintListener(showContentControlListener);

    this.layout();
  }

  /** Hides the explanation and displays the content. */
  public void hideExplanation() {
    this.showExplanation(null);
  }

  /**
   * Sets the control that is visible when the explanation is hidden.
   *
   * @param contentControl
   */
  public void setContentControl(Control contentControl) {
    this.contentControl = contentControl;
  }

  /**
   * Returns the control that is visible when the explanation is hidden.
   *
   * @return
   */
  public Control getContentControl() {
    return this.contentControl;
  }

  @Override
  public void setLayout(Layout layout) {
    // this composite controls its layout itself
  }
}
