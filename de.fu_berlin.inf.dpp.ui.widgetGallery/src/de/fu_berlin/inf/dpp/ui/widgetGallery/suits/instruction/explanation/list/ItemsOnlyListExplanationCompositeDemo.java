package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite.ListExplanation;

public class ItemsOnlyListExplanationCompositeDemo extends Demo {
	public ItemsOnlyListExplanationCompositeDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		ListExplanationComposite simpleExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				SWT.ICON_INFORMATION, null, "List item 1", "List item 2", "List item 3");
		simpleExplanationComposite.setExplanation(listItemExplanation);
	}
}
