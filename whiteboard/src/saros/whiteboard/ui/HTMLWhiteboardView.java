package saros.whiteboard.ui;

import java.io.IOException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import saros.whiteboard.net.WhiteboardManager;
import saros.whiteboard.ui.browser.EclipseWhiteboardBrowser;

/**
 * This is the main view for the HTML Whiteboard, it contains the browser which runs the HTML
 * Whiteboard code
 */
public class HTMLWhiteboardView extends ViewPart {

  private static final String PATH_TO_HTML_WHITEBOARD = "/frontend/dist/index.html";
  private static final Logger LOG = Logger.getLogger(HTMLWhiteboardView.class);

  private Browser browser;

  /** initialises the view part which will hold the browser */
  public HTMLWhiteboardView() {
    super();
  }

  @Override
  public void setFocus() {
    browser.setFocus();
  }

  /**
   * creates a browser and adds it to the view
   *
   * @param parent the parent view to which the browser window will be added
   */
  @Override
  public void createPartControl(Composite parent) {

    browser = new Browser(parent, SWT.NONE);
    browser.setJavascriptEnabled(true);
    LOG.debug("HTML Whiteboard Browser Started");

    // open the whiteboard document in browser
    URL htmlURL = getClass().getResource(PATH_TO_HTML_WHITEBOARD);
    if (htmlURL == null) {
      showErrorMessage(null);
      return;
    }
    try {
      htmlURL = FileLocator.toFileURL(htmlURL);
    } catch (IOException e) {
      showErrorMessage(e);
      return;
    }
    browser.addProgressListener(
        new ProgressListener() {

          @Override
          public void completed(ProgressEvent event) {
            LOG.debug("HTML Whiteboard loaded");
            WhiteboardManager.getInstance().createBridge(new EclipseWhiteboardBrowser(browser));
          }

          @Override
          public void changed(ProgressEvent event) {
            // not needed
          }
        });
    browser.setUrl(htmlURL.toString());
  }

  private void showErrorMessage(Exception e) {
    String errorMessage =
        "can not find the whiteboard's html document at " + PATH_TO_HTML_WHITEBOARD;
    if (e != null) {
      LOG.error(errorMessage, e);
    } else {
      LOG.error(errorMessage);
    }
    browser.setText(
        "<html><body><pre>Resource <b>"
            + PATH_TO_HTML_WHITEBOARD
            + "</b> could not be found.</pre></body></html>");
  }
}
