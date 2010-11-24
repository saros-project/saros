package de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;

/**
 * Instances of this class are controls which are capable of containing exactly
 * <strong>one</strong> content {@link Control} and an implicitly generated
 * {@link SimpleExplanationComposite} to display explanatory information.<br>
 * Although this composite may be subclasses <strong>only the control registered
 * trough {@link SimpleExplanatoryComposite#setContentControl(Control)} can be
 * displayed</strong>.
 * 
 * <p>
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>{@link SWT#BORDER} - this style is only applied on the
 * {@link SimpleExplanationComposite} not on the
 * {@link SimpleExplanatoryComposite} itself</dd>
 * <dd>Styles supported by {@link Composite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Example:</b></dt>
 * <dd>
 * 
 * <pre>
 * <code>
 * final SimpleExplanatoryComposite explanatoryComposite
 *      = new SimpleExplanatoryComposite(tabFolder, SWT.NONE);
 * 
 * Button contenControl = new Button(explanatoryComposite, SWT.NONE);
 * explanatoryComposite.setContentControl(contentControl);
 * contenControl.setText("Show the explanation...");
 * contenControl.addSelectionListener(new SelectionAdapter() {
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
 * 
 * </dd>
 * </dl>
 * 
 * @see SimpleExplanationComposite
 * @see Composite
 * @author bkahlert
 * 
 */
public class SimpleExplanatoryComposite extends ExplanatoryComposite {
    protected SimpleExplanationComposite explanationComposite;

    /**
     * Constructs a new {@link SimpleExplanatoryComposite} with a given parent
     * and the passed style information.
     * 
     * @param parent
     * @param style
     */
    public SimpleExplanatoryComposite(Composite parent, int style) {
        super(parent, style & ~SWT.BORDER);

        this.explanationComposite = new SimpleExplanationComposite(this, style
            & SWT.BORDER);
    }

    /**
     * Hides the content and displays an explanation.
     * 
     * @param simpleExplanation
     *            The explanation to be displayed; if null the explanation gets
     *            hidden
     */
    public void showExplanation(SimpleExplanation simpleExplanation) {
        if (simpleExplanation == null) {
            super.hideExplanation();
        } else {
            this.explanationComposite.setExplanation(simpleExplanation);
            super.showExplanation(this.explanationComposite);
        }
    }

    /**
     * Hides the explanation and displays the content.
     */
    @Override
    public void hideExplanation() {
        super.showExplanation(null);
    }
}
