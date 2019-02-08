package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager
import de.fu_berlin.inf.dpp.ui.pages.AccountPage
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage

/**
 * Open and display {@link AccountPage} dialog.
 * Created by PicoContainer*
 * @param dialogManager
 * @param accountPage
 * @see HTMLUIContextFactory
 */
class ShowAccountPage (dialogManager: DialogManager?, accountPage: AccountPage?)
						: TypedJavaScriptFunction(JS_NAME) {
	
	private val dialogManager: DialogManager? = dialogManager
	private val accountPage: IBrowserPage? = accountPage

	/**
	 * Open and display {@link AccountPage} dialog.
	 */
	@BrowserFunction
	fun showAccountPage() {
		dialogManager!!.showDialogWindow(accountPage)
	}

	companion object {
		val JS_NAME = "showAccountPage"
	}
}