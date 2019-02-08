package de.fu_berlin.inf.dpp.ui

import de.fu_berlin.inf.dpp.ui.pages.AbstractBrowserPage
import de.fu_berlin.inf.dpp.ui.pages.MainPage
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage

/**
 * This enum make the connection between conceptual View and their technical
 * realization. It allows UI designers, for example, to move a simple form (such
 * as ADD_CONTACT) to a separate browser widget without breaking the tests by
 * just changing the pageClass part of the corresponding Key.
 */
enum class View private constructor(
	pageClass: Class<out AbstractBrowserPage>?,
	viewName: String?, rootId: String?
) {
	/**
	 * The permanently accessible view of Saros, which provides access to most
	 * of its features.
	 */
	MAIN_VIEW(MainPage::class.java, "main-page", "root"),
	/**
	 * The form to add a new contact with
	 */
	ADD_CONTACT(MainPage::class.java, "add-contact", "add-contact-form"),
	/**
	 * The session wizard dialog
	 */
	SESSION_WIZARD(
		SessionWizardPage::class.java, "start-session-wizard",
		"session-wizard"
	),
	/**
	 * The dummy page for testing all html components
	 */
	BASIC_WIDGET_TEST(
		MainPage::class.java, "basic-widget-test",
		"basic-widget-test-root"
	);

	val pageClass: Class<out AbstractBrowserPage>?
	val viewName: String?
	val rootId: String?

	init {
		this.pageClass = pageClass
		this.viewName = viewName
		this.rootId = rootId
	}
}