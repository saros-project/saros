package de.fu_berlin.inf.dpp

/**
 * This class encapsulates all labels, messages, titles, etc. which will be
 * visible in the HTML UI. <br>
 *
 * TODO: Maybe this feature can be implemented more sophisticated in the future.
 */
object HTMLUIStrings {
	@JvmStatic
	// dialog titles
	val TITLE_MAIN_PAGE: String = "Main page"
	@JvmStatic
	val TITLE_START_SESSION_WIZARD: String = "Share Project"
	@JvmStatic
	val TITLE_ADD_ACCOUNT_PAGE: String = "Add Account"
	@JvmStatic
	val TITLE_CONFIGURATION_PAGE: String = "Configure Saros"
	// error messages
	@JvmStatic
	val ERR_ACCOUNT_ALREADY_PRESENT: String = "Couldn't create account. Account already present"
	@JvmStatic
	val ERR_ACCOUNT_EDIT_FAILED: String = "Couldn't change the account."
	@JvmStatic
	val ERR_ACCOUNT_SET_ACTIVE_FAILED: String = "Error while trying to change the active account."
	@JvmStatic
	val ERR_ACCOUNT_DELETE_ACTIVE: String =
		"Couldn't delete the account. The currently active account cannot be deleted"
	@JvmStatic
	val ERR_CONTACT_INVALID_JID: String = "The Jabber ID must be in the format user@domain."
	@JvmStatic
	val ERR_CONTACT_DELETE_FAILED: String = "Failed to delete contact."
	@JvmStatic
	val ERR_CONTACT_ADD_FAILED: String = "Failed to add contact."
	@JvmStatic
	val ERR_CONTACT_RENAME_FAILED: String = "Failed to rename contact."
	@JvmStatic
	val ERR_SESSION_START_CANCELED: String = "Couldn't send session invitaion."
	@JvmStatic
	val ERR_SESSION_PROJECT_LIST_IOEXCEPTION: String =
		"An error occurred while trying to create a list of all files to share."
}// Hide implicit public constructor