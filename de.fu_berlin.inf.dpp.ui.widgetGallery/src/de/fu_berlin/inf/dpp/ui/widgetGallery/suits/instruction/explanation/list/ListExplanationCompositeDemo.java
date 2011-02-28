package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite.ListExplanation;

public class ListExplanationCompositeDemo extends DescriptiveDemo {
	public ListExplanationCompositeDemo(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	@Override
	public String getDescription() {
		return ListExplanationComposite.class.getSimpleName();
	}

	@Override
	public void createContent(Composite parent) {
		ListExplanationComposite listExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				SWT.ICON_INFORMATION, "I'm the introductory text...",
				"List item 1", "List item 2", "List item 3", "List item 4",
				"List item 5", "List item 6", "List item 7", "List item 8",
				"List item 9", "List item 10", "List item 11", "List item 12",
				"List item 13", "List item 14", "List item 15");
		listExplanationComposite.setExplanation(listItemExplanation);
	}
}
