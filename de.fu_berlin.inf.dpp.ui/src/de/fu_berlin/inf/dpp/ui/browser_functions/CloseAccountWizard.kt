package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager
import de.fu_berlin.inf.dpp.ui.pages.AccountPage

/**
 * Close an open {@link AccountPage} dialog.
 * Created by PicoContainer
 *
 * @param dialogManager
 * @see HTMLUIContextFactory
  */
class CloseAccountWizard (dialogManager: DialogManager?) : TypedJavaScriptFunction(JS_NAME) {
	private val dialogManager: DialogManager? = dialogManager
	
	/**
	 * Close an open {@link AccountPage} dialog.
	 */
	@BrowserFunction
	fun closeAddAccountWizard() {
		dialogManager!!.closeDialogWindow(AccountPage.HTML_DOC_NAME)
	}

	companion object {
		val JS_NAME = "closeAddAccountWizard"
	}
}