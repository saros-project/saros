package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage

/**
 * Open and display the {@link SessionWizardPage} dialog.
 * Created by PicoContainer
 *
 * @param dialogManager
 * @param sessionWizardPage
 * @see HTMLUIContextFactory
 */
class ShowSessionWizard ( dialogManager: DialogManager?, sessionWizardPage: SessionWizardPage?)
	 					: TypedJavaScriptFunction(JS_NAME) {
	private val dialogManager: DialogManager? = dialogManager
	private val sessionWizardPage : IBrowserPage? = sessionWizardPage

	/**
	 * Open and display the {@link SessionWizardPage} dialog.
	 */
	@BrowserFunction
	fun showSessionWizard() {
		dialogManager!!.showDialogWindow(sessionWizardPage)
	}

	companion object {
		// TODO: Rename to openXYZ for more convenient naming
		val JS_NAME = "showSessionWizard"
	}
}