package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.nebula.explanation.SimpleExplanationComposite;
import de.fu_berlin.inf.nebula.explanation.SimpleExplanationComposite.SimpleExplanation;

@Demo
public class ExplanationOnlySimpleExplanationCompositeDemo extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
        SimpleExplanationComposite simpleExplanationComposite = new SimpleExplanationComposite(
            parent, SWT.NONE);
        SimpleExplanation simpleExplanation = new SimpleExplanation(
            "This is a simple explanation.");
        simpleExplanationComposite.setExplanation(simpleExplanation);
    }
}
