package de.fu_berlin.inf.dpp.ui.renderer

import java.util.ArrayList
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.account.NullAccountStoreListener
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI

/**
 * This class is responsible for sending the account list to the HTML UI.
 */
class AccountRenderer
/**
 * Created by PicoContainer
 *
 * @param accountStore
 * @see HTMLUIContextFactory
 */
	(accountStore: XMPPAccountStore?) : Renderer() {
	private var accounts: List<XMPPAccount>? = ArrayList<XMPPAccount>()

	init {
		accountStore!!.addListener(object : NullAccountStoreListener() {
			override fun accountsChanged(currentAccounts: List<XMPPAccount>?) {
				update(currentAccounts)
				render()
			}
		})
	}

	@Synchronized
	private fun update(allAccounts: List<XMPPAccount>?) {
		accounts = ArrayList<XMPPAccount>(allAccounts)
	}

	
	@Synchronized
	public override fun render(browser: IJQueryBrowser?) {
		JavaScriptAPI.updateAccounts(browser, accounts)
	}
}