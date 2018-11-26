package de.fu_berlin.inf.dpp.whiteboard.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * This is the main view for the HTML Whiteboard, it contains the browser which runs the HTML
 * Whiteboard code
 */
public class HTMLWhiteboardView extends ViewPart {

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
  }
}
