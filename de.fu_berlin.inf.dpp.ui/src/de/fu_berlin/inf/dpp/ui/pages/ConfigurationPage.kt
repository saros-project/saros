package de.fu_berlin.inf.dpp.ui.pages

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings

/**
 * Represents the Saros Configuration Wizard. This wizard is used to configure
 * Saros
 */
/**
 * Created by PicoContainer
 *
 * @see HTMLUIContextFactory
 */
class ConfigurationPage : AbstractBrowserPage(HTML_DOC_NAME, HTMLUIStrings.TITLE_CONFIGURATION_PAGE) {
	companion object {
		val HTML_DOC_NAME = "configuration-page.html"
	}
}