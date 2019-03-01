package saros.whiteboard.standalone;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

  // private static final String ID_TABS_FOLDER =
  // "saros.whiteboard.tabs";

  @Override
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(true);

    // String editorArea = layout.getEditorArea();
    // IFolderLayout tabs = layout.createFolder(ID_TABS_FOLDER,
    // IPageLayout.LEFT, 0.3f, editorArea);
    // tabs.addView(IPageLayout.ID_OUTLINE);
  }
}
