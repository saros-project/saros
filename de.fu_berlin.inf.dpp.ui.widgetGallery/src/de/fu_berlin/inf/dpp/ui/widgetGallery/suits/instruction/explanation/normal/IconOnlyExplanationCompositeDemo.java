package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ExplanationComposite;

public class IconOnlyExplanationCompositeDemo extends Demo {
	public IconOnlyExplanationCompositeDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}
	
	@Override
	public void createPartControls(Composite parent) {
		new ExplanationComposite(parent, SWT.NONE, SWT.ICON_INFORMATION);
	}
}
