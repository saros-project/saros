package de.fu_berlin.inf.dpp.ui.widgetGallery.application;

import de.fu_berlin.inf.dpp.ui.widgetGallery.views.WidgetGalleryView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
  public static String ID = "de.fu_berlin.inf.dpp.ui.widgetGallery.perspective";

  @Override
  public void createInitialLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(false);

    IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 1.00, editorArea);
    left.addView(WidgetGalleryView.ID);
    left.addView("org.eclipse.jdt.ui.PackageExplorer");
  }
}
