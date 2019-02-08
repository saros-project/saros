package de.fu_berlin.inf.dpp.ui.pages

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start
 * a Session by sharing Projects with multiple contacts.
 */
class SessionWizardPage
/**
 * Created by PicoContainer
 *
 * @see HTMLUIContextFactory
 */
	(
	stateRenderer: StateRenderer?,
	projectListRenderer: ProjectListRenderer?
) : AbstractBrowserPage(HTML_DOC_NAME, HTMLUIStrings.TITLE_START_SESSION_WIZARD) {
	init {
		renderers.add(stateRenderer!!)
		renderers.add(projectListRenderer!!)
	}

	companion object {
		val HTML_DOC_NAME = "start-session-wizard.html"
	}
}