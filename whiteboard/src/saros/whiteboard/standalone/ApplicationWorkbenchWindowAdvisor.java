package de.fu_berlin.inf.dpp.whiteboard.standalone;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

  private static final int STAND_ALONE_WINDOW_WIDTH = 800;
  private static final int STAND_ALONE_WINDOW_HEIGHT = 600;
  private static final String STAND_ALONE_WINDOW_TITLE = "Saros Whiteboard";

  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    super(configurer);
  }

  @Override
  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
    return new ApplicationActionBarAdvisor(configurer);
  }

  @Override
  public void preWindowOpen() {
    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
    configurer.setInitialSize(new Point(STAND_ALONE_WINDOW_WIDTH, STAND_ALONE_WINDOW_HEIGHT));
    configurer.setShowCoolBar(false);
    configurer.setShowStatusLine(false);
    configurer.setTitle(STAND_ALONE_WINDOW_TITLE);

    configurer.setShowCoolBar(true);
  }
}
