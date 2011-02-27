package de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.ui.widgets.explanation.ExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;

public abstract class SimpleExplanatoryViewPart extends ViewPart {
    protected SimpleExplanatoryComposite explanatoryComposite;

    @Override
    final public void createPartControl(Composite parent) {
        this.explanatoryComposite = new SimpleExplanatoryComposite(parent,
            SWT.NONE);
        Composite contentComposite = new Composite(this.explanatoryComposite,
            SWT.NONE);
        this.explanatoryComposite.setContentControl(contentComposite);

        createContentPartControl(contentComposite);
    }

    /**
     * @see #createPartControl(Composite)
     */
    abstract public void createContentPartControl(Composite parent);

    /**
     * @see SimpleExplanatoryComposite#showExplanation(ExplanationComposite)
     */
    public void showExplanation(SimpleExplanation simpleExplanation) {
        if (this.explanatoryComposite != null)
            this.explanatoryComposite.showExplanation(simpleExplanation);
    }

    /**
     * @see SimpleExplanatoryComposite#hideExplanation()
     */
    public void hideExplanation() {
        this.showExplanation(null);
    }
}
