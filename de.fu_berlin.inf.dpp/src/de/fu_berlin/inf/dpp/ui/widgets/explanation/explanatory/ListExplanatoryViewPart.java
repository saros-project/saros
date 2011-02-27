package de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite.ListExplanation;

public abstract class ListExplanatoryViewPart extends ViewPart {
    protected ListExplanatoryComposite explanatoryComposite;

    @Override
    final public void createPartControl(Composite parent) {
        this.explanatoryComposite = new ListExplanatoryComposite(parent,
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
     * @see ListExplanatoryComposite#showExplanation(ListExplanation)
     */
    public void showExplanation(ListExplanation listExplanation) {
        if (this.explanatoryComposite != null)
            this.explanatoryComposite.showExplanation(listExplanation);
    }

    /**
     * @see SimpleExplanatoryComposite#hideExplanation()
     */
    public void hideExplanation() {
        this.showExplanation(null);
    }
}
