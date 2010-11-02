package de.fu_berlin.inf.dpp.ui.widgetGallery.application;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.fu_berlin.inf.dpp.ui.widgetGallery.views.WidgetGalleryView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				(float) 1.00, editorArea);
		left.addView(WidgetGalleryView.ID);
	}
}