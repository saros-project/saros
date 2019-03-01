package saros.stf;

import java.rmi.RemoteException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.core.TypeMatcher;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.JPopupMenuFixture;
import saros.intellij.ui.swt_browser.SwtBrowserPanel;
import saros.stf.server.rmi.htmlbot.IHTMLWorkbenchBot;

/**
 * The IntelliJ implementation of the {@link IHTMLWorkbenchBot}. It is used for opening and closing
 * the browser tool window.
 */
public class IntelliJHTMLWorkbenchBot implements IHTMLWorkbenchBot {

  private static final IHTMLWorkbenchBot INSTANCE = new IntelliJHTMLWorkbenchBot();

  private final Robot robot;

  // TODO redundant declaration here and in plugin.xml
  private static final String SAROS_BROWSER_VIEW_ID = "Swt Browser";

  public static IHTMLWorkbenchBot getInstance() {
    return INSTANCE;
  }

  static {
    FailOnThreadViolationRepaintManager.install();
  }

  public IntelliJHTMLWorkbenchBot() {
    super();
    robot = BasicRobot.robotWithCurrentAwtHierarchy();
  }

  @Override
  public void openSarosBrowserView() throws RemoteException {
    if (!isSarosBrowserViewOpen()) {
      toggleBrowserView();
    }
  }

  @Override
  public void closeSarosBrowserView() throws RemoteException {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isSarosBrowserViewOpen() throws RemoteException {
    try {
      return robot.finder().find(new TypeMatcher(SwtBrowserPanel.class)).isShowing();
    } catch (ComponentLookupException e) {
      return false;
    }
  }

  private void toggleBrowserView() {
    JMenu viewMenu =
        robot
            .finder()
            .find(
                new GenericTypeMatcher<JMenu>(JMenu.class) {
                  @Override
                  protected boolean isMatching(JMenu component) {
                    return component.getText().equals("View");
                  }
                });

    robot.click(viewMenu);

    JPopupMenu activePopupMenu = robot.findActivePopupMenu();
    new JPopupMenuFixture(robot, activePopupMenu)
        .menuItem(
            new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
              @Override
              protected boolean isMatching(JMenuItem component) {
                return component.getText().equals("Tool Windows");
              }
            })
        .focus();

    activePopupMenu = robot.findActivePopupMenu();
    new JPopupMenuFixture(robot, activePopupMenu)
        .menuItem(
            new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
              @Override
              protected boolean isMatching(JMenuItem component) {
                return component.getText().equals(SAROS_BROWSER_VIEW_ID);
              }
            })
        .click();
  }
}
