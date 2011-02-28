package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite.ListExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.ListExplanatoryComposite;

public class ListExplanatoryCompositeDemo extends Demo {
	public ListExplanatoryCompositeDemo(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	public void createPartControls(Composite parent) {
		final ListExplanatoryComposite explanatoryComposite = new ListExplanatoryComposite(
				parent, SWT.NONE);

		Button contentControl = new Button(explanatoryComposite, SWT.NONE);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the list explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				int icon = SWT.ICON_WORKING;
				String text = "I tell you how to use this composite.\n"
						+ "This message closes in 5 seconds.";
				ListExplanation expl = new ListExplanation(icon, text,
						"List item 1", "List item 2", "List item 3",
						"List item 4", "List item 5", "List item 6",
						"List item 7", "List item 8", "List item 9",
						"List item 10", "List item 11", "List item 12",
						"List item 13", "List item 14", "List item 15");
				explanatoryComposite.showExplanation(expl);

				Display.getCurrent().timerExec(5000, new Runnable() {

					public void run() {
						explanatoryComposite.hideExplanation();
					}

				});
			}

		});
	}
}
