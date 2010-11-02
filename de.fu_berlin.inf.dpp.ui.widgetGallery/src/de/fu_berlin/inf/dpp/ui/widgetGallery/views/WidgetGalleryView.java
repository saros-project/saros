package de.fu_berlin.inf.dpp.ui.widgetGallery.views;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.MainDemo;


public class WidgetGalleryView extends ViewPart {
	public static final String ID = "de.fu_berlin.inf.dpp.ui.widgetGallery.views.WidgetGalleryView";

	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
		
		new MainDemo(parent);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}