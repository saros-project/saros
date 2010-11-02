package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;

public class SimpleExplanationCompositeDemo extends Demo {
	public SimpleExplanationCompositeDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		SimpleExplanationComposite simpleExplanationComposite = new SimpleExplanationComposite(
				parent, SWT.NONE);
		SimpleExplanation simpleExplanation = new SimpleExplanation(
				SWT.ICON_INFORMATION, "This is a simple explanation.");
		simpleExplanationComposite.setExplanation(simpleExplanation);
	}
}
