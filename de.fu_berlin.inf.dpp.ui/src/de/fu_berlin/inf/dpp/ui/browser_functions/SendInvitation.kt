package de.fu_berlin.inf.dpp.ui.browser_functions

import java.util.ArrayList
import org.apache.log4j.Logger
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.filesystem.IResource
import de.fu_berlin.inf.dpp.net.xmpp.JID
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager
import de.fu_berlin.inf.dpp.ui.model.Contact
import de.fu_berlin.inf.dpp.ui.model.ProjectTree
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils

/**
 * Send an invitation to a number of {@link Contact}s.
 *
 *
 * Created by PicoContainer
 *
 * @param projectListManager
 * @param collaborationUtils
 * @see HTMLUIContextFactory
 */
class SendInvitation(projectListManager: ProjectListManager?,
	  collaborationUtils: ICollaborationUtils?) : TypedJavaScriptFunction(JS_NAME) {
	
	private val projectListManager: ProjectListManager? = projectListManager
	private val collaborationUtils: ICollaborationUtils? = collaborationUtils

	/**
	 * Send an invitation request with the given resources wrapped by
	 * {@link ProjectTree}s to the given {@link Contact}s.
	 * <p>
	 * Note that this will fail if
	 * {@link ProjectListManager#createProjectModels()} hasn't been called yet.
	 *
	 * @param projectTrees
	 * The models containing the selected resources to start the
	 * session with
	 * @param contactList
	 * The models representing the contacts to start the session with
	 */
	@BrowserFunction(Policy.ASYNC)
	fun sendInvitation(
		projectTrees: Array<ProjectTree>?,
		contactList: Array<Contact>?
	) {
		val usersToInvite = ArrayList<JID>()
		for (contact in contactList!!) {
			val contactJID = JID(contact.jid)
			if (!JID.isValid(contactJID)) {
				LOG!!.error("Received jid is invalid")
				JavaScriptAPI.showError(
					browser,
					HTMLUIStrings.ERR_SESSION_START_CANCELED
				)
				return
			}
			usersToInvite.add(contactJID)
		}
		val resourcesToShare = projectListManager!!
			.getAllResources(projectTrees)
		collaborationUtils!!.startSession(resourcesToShare, usersToInvite)
	}

	companion object {
		private val LOG = Logger.getLogger(SendInvitation::class.java)
		private val JS_NAME = "sendInvitation"
	}
}