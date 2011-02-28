package de.fu_berlin.inf.dpp.ui.widgetGallery.demos;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.widgets.DemoExplanation;

public abstract class DescriptiveDemo extends Demo {

	public DescriptiveDemo(Composite parent) {
		super(parent);
	}

	public DescriptiveDemo(DemoContainer parent, String title) {
		super(parent, title);
	}

	public void createPartControls(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		String description = getDescription();
		if (description != null) {
			DemoExplanation expl = new DemoExplanation(parent, getDescription());
			expl.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true,
					false));
		}

		Composite content = new Composite(parent, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.setLayout(new FillLayout());

		createContent(content);
	}

	public abstract String getDescription();

	public abstract void createContent(Composite parent);

}
