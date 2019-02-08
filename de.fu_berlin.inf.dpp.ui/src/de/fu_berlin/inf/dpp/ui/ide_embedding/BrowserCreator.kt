package de.fu_berlin.inf.dpp.ui.ide_embedding

import java.util.ArrayList
import org.eclipse.swt.widgets.Composite
import org.jivesoftware.smack.util.StringUtils
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction
import de.fu_berlin.inf.ag_se.browser.swt.SWTJQueryBrowser
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage

/**
 * This class represents the IDE-independent part of the browser creation. It
 * resorts to IDE-specific resource location however by using the correct
 * instance of {@link IUIResourceLocator} which is injected by PicoContainer.
 * <p>
 * During the creation of a {@link IJQueryBrowser} all
 * {@link JavascriptFunction}s that are registered in the
 * {@link HTMLUIContextFactory} are injected into this browser instance.
 */
class BrowserCreator
/**
 * Injected via PicoContainer
 *
 * @param browserManager
 * @param resourceLocator
 * @see HTMLUIContextFactory
 */
	(
	browserManager: BrowserManager?,
	resourceLocator: IUIResourceLocator?
) {
	private val browserManager: BrowserManager? = browserManager
	private val resourceLocator: IUIResourceLocator? = resourceLocator
	private val browserFunctions: ArrayList<JavascriptFunction?>? = ArrayList<JavascriptFunction?>()


	/**
	 * Adds a function to the set of {@link JavascriptFunction} that will be
	 * injected to any new {@link IJQueryBrowser browser} instance created with
	 * {@link #createBrowser(Composite, int, IBrowserPage) createBrowser()}.
	 * This does not affect already created browser instances.
	 */
	fun addBrowserFunction(function: JavascriptFunction?) {
		browserFunctions!!.add(function)
	}

	/**
	 * Creates a new browser instance.
	 *
	 * @param composite
	 * the composite enclosing the browser.
	 * @param style
	 * the style of the browser instance.
	 * @param page
	 * the page which should be displayed.
	 * @return a browser instance which loads and renders the given
	 * {@link IBrowserPage BrowserPage}
	 */
	fun createBrowser(composite: Composite?, style: Int,page: IBrowserPage?): IJQueryBrowser? {
		val resourceName = page!!.relativePath
		val browser = SWTJQueryBrowser.createSWTBrowser(composite, style)
		val resourceLocation = resourceLocator!!.getResourceLocation(resourceName)
		if (resourceLocation == null) {
			browser!!.setText(
				("<html><body><pre>" + "Resource <b>"
						+ StringUtils.escapeForXML(resourceName)
						+ "</b> could not be found.</pre></body></html>")
			)
			return browser
		}
		browser!!.open(resourceLocation, 5000)
		for (function in browserFunctions!!)
			browser.createBrowserFunction(function)
		browserManager!!.setBrowser(page, browser)
		browser.runOnDisposal(object : Runnable {
			override fun run() {
				browserManager.removeBrowser(page)
			}
		})
		return browser
	}
}