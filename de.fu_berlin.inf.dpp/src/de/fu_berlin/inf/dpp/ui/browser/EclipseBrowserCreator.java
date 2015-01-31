package de.fu_berlin.inf.dpp.ui.browser;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.util.StringUtils;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.swt.SWTJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;

/**
 * This class creates the SWT {@link Browser browser} instance as well as the
 * enclosing {@link Composite composite}. It's also set the {@link BrowserPage
 * BrowserPage} which is to displayed.
 */
public class EclipseBrowserCreator {

    private static final Logger LOG = Logger
        .getLogger(EclipseBrowserCreator.class);

    // TODO central place
    private static final String UI_BUNDLE_ID = "de.fu_berlin.inf.dpp.ui";

    private EclipseBrowserCreator() {
        // This private constructor hides the implicit public one
    }

    /**
     * Creates a new browser instance.
     *
     * @param composite
     *            the composite enclosing the browser.
     * @param style
     *            the style of the browser instance.
     * @param page
     *            the page which should be displayed.
     * @return a browser instance which load and render the given
     *         {@link BrowserPage BrowserPage}
     */
    public static IJQueryBrowser createBrowser(Composite composite, int style,
        BrowserPage page) {

        final Bundle bundle = Platform.getBundle(UI_BUNDLE_ID);

        if (bundle == null)
            throw new IllegalStateException("bundle with id: " + UI_BUNDLE_ID
                + " is not available");

        final String resourceName = page.getWebpage();

        assert resourceName != null;

        URL resourceLocation = bundle.getResource(resourceName);

        /*
         * TODO this currently works only in development environment or if the
         * bundle is deflated when installed because the missing CCS and JS
         * script files have to be present as well in the file system
         */

        Exception exception = null;

        try {
            resourceLocation = FileLocator.resolve(resourceLocation);
        } catch (IOException e) {
            LOG.error("failed to resolve resource: " + resourceName, e);
            exception = e;
        }

        IJQueryBrowser browser = SWTJQueryBrowser.createSWTBrowser(composite,
            style);

        if (exception != null) {
            final String escapedStackTrace = StringUtils
                .escapeForXML(ExceptionUtils.getFullStackTrace(exception));

            browser.setText("<html><body><pre>" + escapedStackTrace
                + "</pre></body></html>");

            return browser;

        }

        page.createRenderer(browser);
        page.createBrowserFunctions(browser);

        /*
         * TODO check if all browser work correctly with invalid Windows URLs
         * like file:/C/... instead of file:///C/...
         *
         * Stefan Rossbach: works with default Browser (whatever that is ...
         * IE?) on Windows
         */
        browser.open(resourceLocation.toString(), 5000);
        return browser;

    }
}
