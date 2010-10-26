package de.fu_berlin.inf.dpp.ui.widgets.explanation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public abstract class ExplanatoryViewPart extends ViewPart {
    protected ExplanatoryComposite explanatoryComposite;

    @Override
    final public void createPartControl(Composite parent) {
        this.explanatoryComposite = new ExplanatoryComposite(parent, SWT.NONE);
        Composite contentComposite = new Composite(this.explanatoryComposite,
            SWT.NONE);
        this.explanatoryComposite.setContentControl(contentComposite);

        createContentPartControl(this.explanatoryComposite, contentComposite);
    }

    /**
     * @see #createPartControl(Composite)
     */
    abstract public void createContentPartControl(
        ExplanatoryComposite explanatoryComposite, Composite contentComposite);

    /**
     * @see ExplanatoryComposite#showExplanation(ExplanationComposite)
     */
    public void showExplanation(ExplanationComposite explanationComposite) {
        this.explanatoryComposite.showExplanation(explanationComposite);
    }

    /**
     * @see ExplanatoryComposite#hideExplanation()
     */
    public void hideExplanation() {
        this.showExplanation(null);
    }
}
