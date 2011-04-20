package de.fu_berlin.inf.dpp.ui.widgetGallery.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.ImageManager;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.MainDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.widgets.BannerComposite;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleIllustratedComposite.IllustratedText;

public class WidgetGalleryView extends ViewPart {
    public static final String ID = "de.fu_berlin.inf.dpp.ui.widgetGallery.views.WidgetGalleryView";
    public static SelectionProviderIntermediate selectionProviderIntermediate = new SelectionProviderIntermediate();

    public void createPartControl(final Composite parent) {
	parent.setLayout(LayoutUtils.createGridLayout());

	this.getSite().setSelectionProvider(selectionProviderIntermediate);

	BannerComposite bannerComposite = new BannerComposite(parent, SWT.NONE);
	bannerComposite.setLayoutData(GridDataFactory.fillDefaults()
		.grab(true, false).create());
	bannerComposite.setContent(new IllustratedText(
		ImageManager.WIDGET_GALLERY_32, "Saros Widget Gallery"));

	MainDemo mainDemo = new MainDemo(parent);
	mainDemo.getControl().setLayoutData(LayoutUtils.createFillGridData());
    }

    @Override
    public void setFocus() {
	// TODO Auto-generated method stub

    }
}