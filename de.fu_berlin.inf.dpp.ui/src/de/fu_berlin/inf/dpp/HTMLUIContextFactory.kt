package de.fu_berlin.inf.dpp

import de.fu_berlin.inf.dpp.context.AbstractContextFactory.Component.create
import org.picocontainer.MutablePicoContainer
import de.fu_berlin.inf.dpp.context.AbstractContextFactory
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContact
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseAccountWizard
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseSessionInvitationWizard
import de.fu_berlin.inf.dpp.ui.browser_functions.ConnectAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.DeleteAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.DeleteContact
import de.fu_berlin.inf.dpp.ui.browser_functions.DisconnectAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.EditAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID
import de.fu_berlin.inf.dpp.ui.browser_functions.RenameContact
import de.fu_berlin.inf.dpp.ui.browser_functions.SaveAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.SendInvitation
import de.fu_berlin.inf.dpp.ui.browser_functions.SetActiveAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowAccountPage
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowSessionWizard
import de.fu_berlin.inf.dpp.ui.core_facades.ConnectionFacade
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager
import de.fu_berlin.inf.dpp.ui.pages.AccountPage
import de.fu_berlin.inf.dpp.ui.pages.ConfigurationPage
import de.fu_berlin.inf.dpp.ui.pages.MainPage
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer

/**
 * This is the HTML UI core factory for Saros. All components that are created
 * by this factory <b>must</b> be working on any platform the application is
 * running on.
 *
 * @JTourBusStop 4, Extending the HTML GUI, PicoContainer components:
 *
 * If you created a new class in the ui module that should be
 * initialised by the PicoContainer, you have to add it here.
 */
class HTMLUIContextFactory : AbstractContextFactory() {
	private var container: MutablePicoContainer? = null
	
	override fun createComponents(container: MutablePicoContainer?) {
		this.container = container
		createBrowserfunctions()
		createPages()
		createRenderer()
		createFacades()
		createMisc()
	}

	private fun createBrowserfunctions() {
// please use alphabetic order
		add(
			AddContact::class.java, //
			CloseAccountWizard::class.java, //
			CloseSessionInvitationWizard::class.java, //
			ConnectAccount::class.java, //
			DeleteAccount::class.java, //
			DeleteContact::class.java, //
			DisconnectAccount::class.java, //
			EditAccount::class.java, //
			GetValidJID::class.java, //
			RenameContact::class.java, //
			SaveAccount::class.java, //
			SendInvitation::class.java, //
			SetActiveAccount::class.java, //
			ShowAccountPage::class.java, //
			ShowSessionWizard::class.java
		)
	}

	private fun createPages() {
		add(
			AccountPage::class.java, MainPage::class.java, SessionWizardPage::class.java,
			ConfigurationPage::class.java
		)
	}

	private fun createRenderer() {
		add(
			AccountRenderer::class.java, StateRenderer::class.java,
			ProjectListRenderer::class.java
		)
	}

	private fun createFacades() {
		add(ConnectionFacade::class.java, RosterFacade::class.java)
	}

	/**
	 * For UI components that fits no where else.
	 */
	private fun createMisc() {
// TODO: Dodgy naming
		add(
			BrowserCreator::class.java, BrowserManager::class.java,
			ProjectListManager::class.java
		)
	}

	/**
	 * Add the components to the container
	 *
	 * @param classes
	 * to add
	 */
	private fun add(vararg classes: Class<*>) {
		for (clazz in classes) {
			val component = create(clazz)
			container!!.addComponent(
				component.getBindKey(),
				component.getImplementation()
			)
		}
	}
}