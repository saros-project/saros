package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage

/**
 * Close an open {@link SessionWizardPage} dialog.
 *
 * Created by PicoContainer
 *
 * @param dialogManager
 * @see HTMLUIContextFactory
 */
class CloseSessionInvitationWizard (dialogManager: DialogManager?) : TypedJavaScriptFunction(JS_NAME) {
	private val dialogManager: DialogManager? = dialogManager

	/**
	 * Close an open {@link SessionWizardPage} dialog.
	 */
	@BrowserFunction
	fun closeStartSessionWizard() {
		dialogManager!!.closeDialogWindow(SessionWizardPage.HTML_DOC_NAME)
	}

	companion object {
		val JS_NAME = "closeStartSessionWizard"
	}
}