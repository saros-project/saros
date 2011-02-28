package de.fu_berlin.inf.dpp.ui.widgetGallery.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.util.FontUtils;

public class SimpleBannerComposite extends Composite {

	protected Label label;

	public SimpleBannerComposite(Composite parent, int style) {
		super(parent, style);
		this.setBackgroundMode(SWT.INHERIT_DEFAULT);
		this.setLayout(LayoutUtils.createGridLayout(10, 0));

		this.label = new Label(this, SWT.NONE);
		this.label.setForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		FontUtils.changeFontSizeBy(label, 3);

		this.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GRAY));
	}

	public void setText(String text) {
		if (this.label != null && !this.label.isDisposed()) {
			this.label.setText(text);
		}
	}

}
