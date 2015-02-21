package de.fu_berlin.inf.dpp.ui.browser;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.util.StringUtils;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.swt.SWTJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;

/**
 * This class creates JQuery {@link IJQueryBrowser browser} instances. The
 * displayed content depends on the used {@link BrowserPage} implementation.
 */
public class EclipseBrowserCreator {

    private static final Logger LOG = Logger
        .getLogger(EclipseBrowserCreator.class);

    // TODO central place
    private static final String UI_BUNDLE_ID = "de.fu_berlin.inf.dpp.ui";

    private static final String HTML_ROOT_PATH = "html";

    private static Map<String, String> fileMapping;

    private final BrowserManager browserManager;

    public EclipseBrowserCreator(BrowserManager browserManager) {
        this.browserManager = browserManager;
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
    public IJQueryBrowser createBrowser(Composite composite, int style,
        final BrowserPage page) {

        final Bundle bundle = Platform.getBundle(UI_BUNDLE_ID);

        if (bundle == null)
            throw new IllegalStateException("bundle with id: " + UI_BUNDLE_ID
                + " is not installed");

        synchronized (EclipseBrowserCreator.class) {
            if (fileMapping == null)
                fileMapping = extractBundleResources(UI_BUNDLE_ID,
                    HTML_ROOT_PATH, null, true);
        }

        final String resourceName = page.getWebpage();

        assert resourceName != null;

        final String resourceLocation = fileMapping.get(resourceName);

        final IJQueryBrowser browser = SWTJQueryBrowser.createSWTBrowser(
            composite, style);

        if (resourceLocation == null) {
            browser.setText("<html><body><pre>" + "Resource <b>"
                + StringUtils.escapeForXML(resourceName)
                + "</b> could not be found.</pre></body></html>");
            return browser;
        }

        page.createBrowserFunctions(browser);
        browserManager.setBrowser(page, browser);
        browser.runOnDisposal(new Runnable() {
            @Override
            public void run() {
                browserManager.removeBrowser(page);
            }
        });

        /*
         * TODO check if all browser work correctly with invalid Windows URLs
         * like file:/C:/... instead of file:///C:/...
         * 
         * Stefan Rossbach: works with default Browser (whatever that is ...
         * Internet Explorer ?) on Windows
         */
        browser.open(resourceLocation.toString(), 5000);
        return browser;

    }

    private static Map<String, String> extractBundleResources(
        final String bundleID, final String path, final String filePattern,
        final boolean recursive) {

        final Map<String, String> mapping = new HashMap<String, String>();

        final Bundle bundle = Platform.getBundle(UI_BUNDLE_ID);

        if (bundle == null) {
            LOG.warn("bundle with id: " + UI_BUNDLE_ID + " is not installed");
            return mapping;
        }

        List<URL> resourceLocations;

        resourceLocations = getResourceLocationsPlain(bundle, path,
            filePattern, recursive);

        if (resourceLocations.isEmpty()) {
            LOG.warn("could not find any resources using default bundle find method with path '"
                + path + "' and file pattern '" + filePattern + "'");

            resourceLocations = getResourceLocationsWiring(bundle, path,
                filePattern, recursive);

        }

        if (resourceLocations.isEmpty()) {
            LOG.warn("could not find any resources using wiring bundle find method with path '"
                + path + "' and file pattern '" + filePattern + "'");

            return mapping;
        }

        for (final URL resourceLocation : resourceLocations) {

            // directory
            if (resourceLocation.getPath().endsWith("/"))
                continue;

            if (LOG.isTraceEnabled())
                LOG.trace("extracting resource: " + resourceLocation);

            try {
                final URL fileLocation = FileLocator
                    .toFileURL(resourceLocation);
                String urlPath = resourceLocation.getPath();

                /** see {@linkplain BrowserPage#getPage()  */
                if (urlPath.startsWith("/"))
                    urlPath = urlPath.substring(1);

                mapping.put(urlPath, fileLocation.toString());
            } catch (IOException e) {
                LOG.error("failed to extract resource: " + resourceLocation, e);
            }
        }

        return mapping;
    }

    private static List<URL> getResourceLocationsPlain(final Bundle bundle,
        final String path, final String filePattern, final boolean recursive) {

        final List<URL> result = new ArrayList<URL>();

        final Enumeration<URL> entries = bundle.findEntries(path, filePattern,
            recursive);

        if (entries == null)
            return result;

        while (entries.hasMoreElements())
            result.add(entries.nextElement());

        return result;
    }

    private static List<URL> getResourceLocationsWiring(final Bundle bundle,
        final String path, final String filePattern, final boolean recursive) {

        final List<URL> result = new ArrayList<URL>();

        /*
         * we must use reflection as the required OSGi framework is not
         * available in Eclipse 3.6
         */

        try {

            final Class<?> wiringClass = Class.forName(
                "org.osgi.framework.wiring.BundleWiring", true,
                EclipseBrowserCreator.class.getClassLoader());

            final Method adapt = Bundle.class.getMethod("adapt",
                new Class[] { Class.class });

            final Object wiring = adapt.invoke(bundle, wiringClass);

            if (wiring == null)
                return result;

            // BundleWiring.LISTRESOURCES_RECURSE = 1
            final int recurse = recursive ? 1 : 0;

            // BundleWiring.LISTRESOURCES_LOCAL = 2
            final int local = 2;

            final Method listResources = wiringClass.getMethod("listResources",
                new Class[] { String.class, String.class, int.class });

            final Method getClassLoader = wiringClass
                .getMethod("getClassLoader");

            final ClassLoader classLoader = (ClassLoader) getClassLoader
                .invoke(wiring);

            final Collection<String> entries = (Collection) listResources
                .invoke(wiring, path, filePattern, recurse | local);

            if (entries == null || entries.isEmpty())
                return result;

            for (final String resourceName : entries) {

                final URL resourceLocation = classLoader
                    .getResource(resourceName);

                if (resourceLocation == null) {
                    LOG.error("could not find resource in the bundle classpath: "
                        + resourceName);

                    continue;
                }

                result.add(resourceLocation);

            }
        } catch (Exception e) {
            LOG.error(
                "internal error while finding resources via bundle wiring API",
                e);
        }

        return result;
    }
}
