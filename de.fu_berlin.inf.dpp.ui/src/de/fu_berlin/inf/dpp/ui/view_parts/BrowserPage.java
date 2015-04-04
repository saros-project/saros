package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.List;

/**
 * A browser page encapsulates the location of the HTML page as well as the needed
 * browsers functions and renderers.
 * The browser functions are the Java methods that the webpage calls inside Javascript.
 * The renderers transfer application state from Java to the webpage.
 */
public interface BrowserPage {

    /**
     * Returns the resource name of this <code>BowserPage</code> or <code>null</code> if there is no resource associated with this page.
     * <p/>
     * E.g: html/index.html
     * <p/>
     * It is up to the caller to resolve the absolute physical location.
     *
     * @return the resource name or <code>null</code>
     * @see ClassLoader#getResource(String name)
     */
    String getWebpage();

    /**
     * Creates the needed {@link org.eclipse.swt.browser.BrowserFunction}s for
     * the webpage.
     */
    List<JavascriptFunction> getJavascriptFunctions();

    /**
     * Gets the list of renderers that can display application state in this
     * webpage.
     *
     * @return the list of renderers for this page
     */
    List<Renderer> getRenderer();
}
