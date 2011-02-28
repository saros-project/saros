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

public class IntroductoryTextOnlyListExplanatoryCompositeDemo extends Demo {
	public IntroductoryTextOnlyListExplanatoryCompositeDemo(
			DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		final ListExplanatoryComposite explanatoryComposite = new ListExplanatoryComposite(
				parent, SWT.NONE);

		Button contentControl = new Button(explanatoryComposite, SWT.NONE);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the list explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int icon = SWT.NONE;
				String text = "I tell you how to use this composite.\n"
						+ "This message closes in 5 seconds.";
				ListExplanation expl = new ListExplanation(icon, text);
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
