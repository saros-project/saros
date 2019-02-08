package de.fu_berlin.inf.dpp.ui

import org.apache.log4j.Logger
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.fu_berlin.inf.ag_se.browser.IBrowser
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.ui.model.ProjectTree
import de.fu_berlin.inf.dpp.ui.model.State

/**
 * Since the callable JS functions inside the UI.Frontend are hidden for Java
 * developers, this class provides a list of functions that reflects and
 * abstracts the actual JS API. For example updating a data model in the
 * "JavaScriptWorld" or show an error in the fronted using the browser.
 * <p>
 * All provided functions are using the {@link IBrowser#run(String)} method to
 * invoke JavaScript.
 * <p>
 * Changes in the Java-->JavaScript API should be reflected and encapsulated
 * here.
 */
object JavaScriptAPI {
	private val LOG = Logger.getLogger(JavaScriptAPI::class.java)
	private val GSON = Gson()
	/**
	 * Informs the user about an error in the given browser. The actual way how
	 * this message will be shown is handled in Javascript.
	 *
	 * @param browser
	 * the browser instance that should show this error message
	 * @param errorMessage
	 * the message that will be shown to the user
	 */
	@JvmStatic
	fun showError(browser: IBrowser?, errorMessage: String?) {
		LOG!!.debug(
			("Trigger js showError() on browser " + browser!!.getUrl() + ","
					+ errorMessage)
		)
		triggerEvent(browser, "showError", errorMessage)
	}

	/**
	 * Updates the project list in the given browser.
	 *
	 * @param browser
	 * the browser instance in which the model should be updated
	 * @param projectTrees
	 * to update the JS model with
	 *
	 */
	@JvmStatic
	fun updateProjects(
		browser: IBrowser?,
		projectTrees: List<ProjectTree>?
	) {
		LOG!!.debug(("Sending list of ProjectTree JSONs to browser " + browser!!.getUrl()))
		triggerEvent(browser, "updateProjectTrees", toJson(projectTrees))
	}

	/**
	 * Updates the state model in the given browser.
	 *
	 * @param browser
	 * the browser instance in which the model should be updated
	 * @param sarosStateModel
	 * to update the JS model with
	 */
	@JvmStatic
	fun updateState(browser: IBrowser?, sarosStateModel: State?) {
		LOG!!.debug("Sending State JSON to browser " + browser!!.getUrl())
		triggerEvent(browser, "updateState", toJson(sarosStateModel))
	}

	/**
	 * Updates the account models in the given browser.
	 *
	 * @param browser
	 * the browser instance in which the model should be updated
	 * @param accounts
	 * a list of Accounts to update the JS model with
	 */
	@JvmStatic
	fun updateAccounts(
		browser: IBrowser?,
		accounts: List<XMPPAccount>?
	) {
		LOG!!.debug(("Sending list of Account JSONs to browser " + browser!!.getUrl()))
		triggerEvent(browser, "updateAccounts", toJson(accounts))
	}

	/**
	 * Creates the Javascript-String and run it in the browser.
	 *
	 * @param browser
	 * the browser in which the event will be triggered
	 * @param javaScriptEvent
	 * the name of the event in the Javascript implementation. F.e.
	 * "updateAccounts" or "updateState"
	 * @param parameters
	 * the parameters for this event.
	 */
	@JvmStatic
	private fun triggerEvent(
		browser: IBrowser?, javaScriptEvent: String?,
		parameters: String?
	) {
		browser!!.run(
			("SarosApi.trigger('" + javaScriptEvent + "', " + parameters
					+ ");")
		)
	}

	/**
	 * This method serializes the specified object into its equivalent Json
	 * representation.
	 *
	 * @param object
	 * the object to be serialized
	 *
	 * @return a JSON string serialization of the given object
	 *
	 * @see <a
	 * href="http://google.github.io/gson/apidocs/com/google/gson/Gson.html">GsonAPI</a>
	 */
	@JvmStatic
	fun toJson(obj: Any?): String? {
		return GSON.toJson(obj)
	}

	/**
	 * This method deserializes the Json read from the specified parse tree into
	 * an object of the specified type. It is not suitable to use if the
	 * specified class is a generic type since it will not have the generic type
	 * information because of the Type Erasure feature of Java. Therefore, this
	 * method should not be used if the desired type is a generic type.
	 *
	 * @param json
	 * the JSON string serialization to transform
	 * @param classOfT
	 * type of class to
	 * @return an object of type T from the json. Returns null if json is null.
	 *
	 * @throws JsonSyntaxException
	 * if json is not a valid representation for an object of type
	 * typeOfT
	 * @see <a
	 * href="http://google.github.io/gson/apidocs/com/google/gson/Gson.html">GsonAPI</a>
	 */
	@JvmStatic
	@Throws(JsonSyntaxException::class)
	fun <T> fromJson(json: String?, classOfT: Class<T>?): T? {
		return GSON.fromJson(json, classOfT)
	}
}