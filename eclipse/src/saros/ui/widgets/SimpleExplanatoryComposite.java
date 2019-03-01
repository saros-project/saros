package saros.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import saros.ui.widgets.SimpleExplanationComposite.SimpleExplanation;

/**
 * Instances of this class are controls which are capable of containing exactly <strong>one</strong>
 * content {@link Control} and an implicitly generated {@link SimpleExplanationComposite} to display
 * explanatory information.<br>
 * Although this composite may be subclasses <strong>only the control registered trough {@link
 * SimpleExplanatoryComposite#setContentControl(Control)} can be displayed</strong>.
 *
 * <p>
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>{@link SWT#H_SCROLL}, {@link SWT#V_SCROLL}, {@link SWT#BORDER} - these styles is only
 *       applied on the {@link SimpleExplanationComposite} not on the {@link
 *       SimpleExplanatoryComposite} itself
 *   <dd>Styles supported by {@link Composite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 *   <dt><b>Example:</b>
 *   <dd>
 *       <pre>
 * <code>
 * final SimpleExplanatoryComposite explanatoryComposite
 *      = new SimpleExplanatoryComposite(tabFolder, SWT.NONE);
 *
 * Button contentControl = new Button(explanatoryComposite, SWT.NONE);
 * explanatoryComposite.setContentControl(contentControl);
 * contentControl.setText("Show the explanation...");
 * contentControl.addSelectionListener(new SelectionAdapter() {
 *
 *         public void widgetSelected(SelectionEvent e) {
 *              int icon = SWT.ICON_INFORMATION;
 *              String text = "I'm supposed to tell you how to use this composite.\n"
 *                          + "This message closes in 5 seconds.";
 *              SimpleExplanation expl = new SimpleExplanation(icon, text);
 *              explanatoryComposite.showExplanation(expl);
 *
 *              Display.getCurrent().timerExec(5000, new Runnable() {
 *
 *                      public void run() {
 *                              explanatoryComposite.hideExplanation();
 *                     }
 *
 *              });
 *         }
 *
 * });
 * </code>
 * </pre>
 * </dl>
 *
 * @see SimpleExplanationComposite
 * @see Composite
 * @author bkahlert
 */
public class SimpleExplanatoryComposite extends ExplanatoryComposite {
  protected SimpleExplanationComposite simpleExplanationComposite;

  /**
   * Constructs a new {@link SimpleExplanatoryComposite} with a given parent and the passed style
   * information.
   *
   * @param parent
   * @param style
   */
  public SimpleExplanatoryComposite(Composite parent, int style) {
    super(parent, style & ~(SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));

    this.simpleExplanationComposite =
        new SimpleExplanationComposite(this, style & (SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));
  }

  /**
   * Hides the content and displays an explanation.
   *
   * @param explanation The explanation to be displayed; if null the explanation gets hidden
   */
  public void showExplanation(SimpleExplanation explanation) {
    if (explanation == null) {
      super.hideExplanation();
    } else {
      this.simpleExplanationComposite.setExplanation(explanation);
      super.showExplanation(this.simpleExplanationComposite);
    }
  }

  /** Hides the explanation and displays the content. */
  @Override
  public void hideExplanation() {
    super.showExplanation((SimpleExplanationComposite) null);
  }
}
