package de.fu_berlin.inf.dpp.ui.widgets.explanation.note;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.RoundedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;

public class NoteComposite extends RoundedComposite {

    public static final int SPACING = 10;

    protected SimpleExplanationComposite explanationComposite;

    public NoteComposite(Composite parent, int style) {
        super(parent, style);
        super.setLayout(LayoutUtils.createGridLayout(SPACING, 0));

        explanationComposite = new SimpleExplanationComposite(this, SWT.NONE);
        explanationComposite.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.CENTER, false, true));
        explanationComposite.setSpacing(SPACING);

        this.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        this.setBackground(parent.getDisplay().getSystemColor(
            SWT.COLOR_DARK_GRAY));
    }

    /**
     * @see SimpleExplanationComposite#setExplanation(SimpleExplanation)
     */
    public void setExplanation(SimpleExplanation simpleExplanation) {
        this.explanationComposite.setExplanation(simpleExplanation);
    }

    @Override
    public void setForeground(Color color) {
        super.setForeground(color);
        this.explanationComposite.setForeground(color);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        this.explanationComposite.setBackground(color);
    }

    @Override
    public void setLayout(Layout layout) {
        // this composite controls its layout itself
    }
}
