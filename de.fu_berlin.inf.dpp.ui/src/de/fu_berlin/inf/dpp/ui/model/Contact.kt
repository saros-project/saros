package de.fu_berlin.inf.dpp.ui.model

/**
 * Represent an entry in a contact list.
 *
 * This class is immutable.
 */
class Contact
/**
 * @param displayName
 * the name of the contact as it should be displayed
 * @param presence
 * a string indicating the online status
 * @param addition
 * a string containing subscription status
 * @param jid
 * a string that represents the {@link #jid}
 */
	(
	val displayName: String?, val presence: String?, val addition: String?,
	val jid: String?
)