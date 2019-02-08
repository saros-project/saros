package de.fu_berlin.inf.dpp.ui.pages

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer

/**
 * Represents the Saros main view.
 */
class MainPage
/**
 * Created by PicoContainer
 *
 * @see HTMLUIContextFactory
 */
	(accountRenderer: AccountRenderer?, stateRenderer: StateRenderer?) :
	AbstractBrowserPage(HTML_DOC_NAME, HTMLUIStrings.TITLE_MAIN_PAGE) {
	init {
		renderers.add(accountRenderer!!)
		renderers.add(stateRenderer!!)
	}

	companion object {
		val HTML_DOC_NAME = "main-page.html"
	}
}