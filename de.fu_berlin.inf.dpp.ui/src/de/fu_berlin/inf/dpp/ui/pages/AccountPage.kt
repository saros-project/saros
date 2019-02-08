package de.fu_berlin.inf.dpp.ui.pages

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings

/**
 * Represents the wizard to manage accounts.
 */
/**
 * Created by PicoContainer
 *
 * @see HTMLUIContextFactory
 */
class AccountPage : AbstractBrowserPage(HTML_DOC_NAME, HTMLUIStrings.TITLE_ADD_ACCOUNT_PAGE) {
	companion object {
		// TODO: NOT USED AT THE MOMENT! Create HTML page and open it in the
// main-page.html by calling "__java_showAccountPage();".
		val HTML_DOC_NAME = "account-page.html"
	}
}// No renderer used, so let renderers list be empty