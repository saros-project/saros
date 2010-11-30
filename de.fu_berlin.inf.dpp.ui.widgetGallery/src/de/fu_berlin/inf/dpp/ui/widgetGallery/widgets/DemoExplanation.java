package de.fu_berlin.inf.dpp.ui.widgetGallery.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;

public class DemoExplanation extends SimpleRoundedComposite {

	public DemoExplanation(Composite parent, String text) {
		super(parent, SWT.NONE);

		this.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		this.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GRAY));

		this.setText(text);
	}

}
