package saros.intellij.ui.swt_browser;

import com.intellij.ui.AncestorListenerAdapter;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.ui.pages.MainPage;

/**
 * Saros main panel view. This is a JPanel and encapsulates the browser canvas. This class is
 * responsible for starting the SWT thread and managing the creation and display of the browser.
 */
public class SwtBrowserPanel extends JPanel {

  /**
   * Required for Linux, harmless for other OS.
   *
   * <p><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911">SWT Component Not Displayed
   * Bug</a>
   */
  static {
    System.setProperty("sun.awt.xembedserver", "true");
  }

  private SwtBrowserCanvas browserCanvas;

  private boolean initialized = false;

  @Inject private MainPage mainPage;

  SwtBrowserPanel() {
    super(new BorderLayout());
    SarosPluginContext.initComponent(this);
    /* As the browser gets disposed every time the tool window is hidden,
     * it has to be created again when it is re-shown.
     * The AncestorListener listens for that event. */
    addAncestorListener(
        new AncestorListenerAdapter() {
          @Override
          public void ancestorAdded(AncestorEvent event) {
            // this event is also triggered if the window was opened for the first time
            // we only want to start the shell for the times after that
            if (browserCanvas != null && initialized) {
              browserCanvas.launchBrowser();
            }
          }
        });
    browserCanvas = new SwtBrowserCanvas(mainPage);
  }

  /**
   * This method must be called *after* the enclosing frame has been made visible. Otherwise the SWT
   * AWT bridge will throw a {@link org.eclipse.swt.SWT#ERROR_INVALID_ARGUMENT}
   */
  void initialize() {
    assert browserCanvas != null;
    add(browserCanvas, BorderLayout.CENTER);
    browserCanvas.launchBrowser();
    initialized = true;
  }
}
