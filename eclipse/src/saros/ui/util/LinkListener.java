/** */
package de.fu_berlin.inf.dpp.ui.util;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

/**
 * A simple Listener that handles link selection events by opening the link text in an external
 * browser. Is supposed to be used with {@link Link}. <br>
 * <br>
 * Example usage: <br>
 * <code>
 * Link link = new Link(parent, SWT.NONE); <br>
 * link.addListener(SWT.Selection, new LinkListener());
 * </code>
 */
public class LinkListener implements Listener {

  private Logger log = Logger.getLogger(LinkListener.class);

  public LinkListener() {
    // default constructor
  }

  @Override
  public void handleEvent(Event event) {
    if (!SWTUtils.openExternalBrowser(event.text)) {
      log.error("Couldn't open link " + event.text + " in external browser.");
    }
  }
}
