package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;
import de.fu_berlin.inf.dpp.util.BrowserUtils;

/**
 * This class creates the SWT browser instance and the enclosing shell.
 */
class BrowserCreator {
    private Browser browser;

    BrowserCreator() {
    }

    /**
     * This methods creates a SWT shell and browser in the provided AWT canvas.
     * 
     * @param display
     *            the SWT display
     * @param canvas
     *            the AWT canvas to contain the SWT shell
     * @param page
     *            the BrowserPage object of the page to be displayed
     * @return this object
     */
    Browser createBrowser(Display display, final SwtBrowserCanvas canvas,
        BrowserPage page) {
        Shell shell = SWT_AWT.new_Shell(display, canvas);
        browser = new Browser(shell, SWT.NONE);

        browser.setLocation(5, 5);
        /*
         * Ideally the size of browser and shell gets set via a resize listener.
         * This does not work when the tool window is re-openend as no size
         * change event is fired. The if clause below sets the size for this
         * case
         */
        if (canvas.getHeight() > 0 && canvas.getWidth() > 0) {
            shell.setSize(canvas.getWidth(), canvas.getHeight());
            browser.setSize(canvas.getWidth(), canvas.getHeight());
        }

        page.createRenderer(browser);
        page.createBrowserFunctions(browser);

        final URL url = BrowserUtils.getResourceURL(page.getWebpage());

        assert url != null;

        browser.setUrl(url.toString());
        return browser;
    }
}
