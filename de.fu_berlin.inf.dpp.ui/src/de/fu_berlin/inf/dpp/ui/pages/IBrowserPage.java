package de.fu_berlin.inf.dpp.ui.pages;

import de.fu_berlin.inf.dpp.ui.ide_embedding.IUIResourceLocator;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;
import java.util.List;

/**
 * A browser page encapsulates the location of the HTML page as well as the needed browsers
 * functions and renderers. The browser functions are the Java methods that the page calls inside
 * Javascript. The renderers transfer application state from Java to the page. @JTourBusStop 2,
 * Extending the HTML GUI, Creating a Java abstraction for a page:
 *
 * <p>Each page in Saros has a corresponding implementation of this interface. There is one for the
 * Saros main view and one for each dialog. So if you add a new .html file, you should add a
 * suitable BrowserPage implementation as well.
 *
 * <p>The BrowserPage encapsulates the location of the .html file, which is the relative location
 * inside the resource folder.
 *
 * <p>It further creates the list of renderers and browser functions needed for this page.
 */
public interface IBrowserPage {
  /**
   * Returns the relative path to the corresponding HTML file of this <code>BowserPage</code>.
   *
   * <p>E.g: html/dist/index.html
   *
   * <p>It is up to the caller to resolve the absolute physical location.
   *
   * @return the relative path of this {@link IBrowserPage} which can be used as the resource name
   * @see IUIResourceLocator#getResourceLocation(String resourceName)
   */
  String getRelativePath();

  /**
   * Returns the title of this <code>BrowserPage</code>.
   *
   * @return the title
   */
  String getTitle();

  /**
   * Gets the list of {@link Renderer}s that can display application state in this weppage.
   *
   * @return the list of renderers for this page. If the page don't uses any renderer this can be an
   *     empty list.
   */
  List<Renderer> getRenderers();
}
