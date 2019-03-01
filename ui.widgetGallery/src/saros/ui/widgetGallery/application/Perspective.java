package saros.ui.widgetGallery.application;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import saros.ui.widgetGallery.views.WidgetGalleryView;

public class Perspective implements IPerspectiveFactory {
  public static String ID = "saros.ui.widgetGallery.perspective";

  @Override
  public void createInitialLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(false);

    IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 1.00, editorArea);
    left.addView(WidgetGalleryView.ID);
    left.addView("org.eclipse.jdt.ui.PackageExplorer");
  }
}
