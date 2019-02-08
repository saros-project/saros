package de.fu_berlin.inf.dpp.ui.browser_functions

import org.picocontainer.Startable
import org.picocontainer.annotations.Inject
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator

/**
 * Browser functions that inherit from {@link SelfRegisteringJavascriptFunction}
 * (and are added to the dependency injection context, see
 * {@link HTMLUIContextFactory}) will be automatically added to the
 * {@link BrowserCreator}, i.e. they will be injected to any created browser
 * widget and can be called from the JavaScript context.
 */
public abstract class SelfRegisteringJavascriptFunction(name: String?) : JavascriptFunction(name), Startable {
	
	@Inject
	private val browserCreator: BrowserCreator? = null

	
	override fun start() {
		browserCreator!!.addBrowserFunction(this)
	}

	override fun stop() {
		// do nothing
	}
}