package de.fu_berlin.inf.dpp.ui.renderer

import java.util.ArrayList
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser
import de.fu_berlin.inf.dpp.ui.model.State

/**
 * Implementations of this interface can transfer their current state to a list
 * of browsers by calling Javascript functions.
 *
 * As the responsible browser instances may change, all renderers manage a
 * reference to the current browsers and provide methods for its replacement.
 *
 *
 * @JTourBusStop 3, Extending the HTML GUI, Calling Javascript from Java:
 *
 * Each renderer class is used to transfer parts of the
 * application state to the each browser for rendering.
 *
 * They may store state themselves, as {@link StateRenderer} does.
 *
 * There are two ways to get the state from the Saros:
 *
 * 1. use a listener if supported. See {@link StateRenderer} for
 * an example.
 *
 * 2. query the state from the core directly the
 * {@link AccountRenderer} does that.
 *
 * For the management of GUI state create custom GUI model class
 * in the model package, like {@link State}. Those classes should
 * be converted to JSON strings with the GSON library in the
 * renderer classes.
 */
abstract class Renderer {
	private val browserList = ArrayList<IJQueryBrowser?>()
	/**
	 * Renders the current state managed by the renderer in the given browser.
	 *
	 * @param browser
	 * the browser to be rendered
	 */
	abstract fun render(browser: IJQueryBrowser?)

	/**
	 * Renders the current state managed by the renderer for each browser.
	 */
	@Synchronized
	fun render() {
		for (browser in browserList) {
			this.render(browser)
		}
	}

	/**
	 * Adds the given browser to the renderer
	 *
	 * @param browser
	 * the browser to be added
	 */
	@Synchronized
	fun addBrowser(browser: IJQueryBrowser?) {
		this.browserList.add(browser)
		render(browser)
	}

	/**
	 * Removes the given browser from the renderer. This method must be called
	 * every time the browser is disposed.
	 *
	 * @param browser
	 * the browser to be removed
	 */
	@Synchronized
	fun removeBrowser(browser: IJQueryBrowser?) {
		browserList.remove(browser)
	}
}