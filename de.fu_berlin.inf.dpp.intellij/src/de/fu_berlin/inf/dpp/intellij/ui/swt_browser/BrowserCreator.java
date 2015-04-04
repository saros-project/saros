package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.ag_se.browser.swt.SWTJQueryBrowser;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;
import de.fu_berlin.inf.dpp.util.BrowserUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

import java.net.URL;

/**
 * This class creates the SWT browser instance and the enclosing shell.
 */
class BrowserCreator {

    @Inject
    private BrowserManager browserManager;

    BrowserCreator() {
        SarosPluginContext.initComponent(this);
    }

    /**
     * This methods creates a SWT shell and browser in the provided
     * AWT canvas.
     *
     * @param display the SWT display
     * @param canvas  the AWT canvas to contain the SWT shell
     * @param page    the BrowserPage object of the page to be displayed
     * @return this object
     */
    IJQueryBrowser createBrowser(Display display, final SwtBrowserCanvas canvas,
        final BrowserPage page) {
        Shell shell = SWT_AWT.new_Shell(display, canvas);
        IJQueryBrowser browser = SWTJQueryBrowser
            .createSWTBrowser(shell, SWT.NONE);

        /* Ideally the size of browser and shell gets set via a resize listener.
         * This does not work when the tool window is re-openend as no size
         * change event is fired. The if clause below sets the size for this case */
        if (canvas.getHeight() > 0 && canvas.getWidth() > 0) {
            shell.setSize(canvas.getWidth(), canvas.getHeight());
            browser.setSize(canvas.getWidth(), canvas.getHeight());
        }

        final URL url = BrowserUtils.getResourceURL(page.getWebpage());

        assert url != null;

        browser.open(url.toString(), 5000);

        for (JavascriptFunction function : page.getJavascriptFunctions()) {
            browser.createBrowserFunction(function);
        }

        browserManager.setBrowser(page, browser);
        browser.runOnDisposal(new Runnable() {
            @Override
            public void run() {
                browserManager.removeBrowser(page);
            }
        });

        return browser;
    }
}
