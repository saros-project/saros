package de.fu_berlin.inf.dpp.ui.util

import java.text.MessageFormat
import java.util.ArrayList
import de.fu_berlin.inf.dpp.session.User
import de.fu_berlin.inf.dpp.session.UserFormatUtils

/**
 * Utility class for formatting model elements for display in the UI.
 */
object ModelFormatUtils {
	/**
	 * Retrieves a user's nickname from the XMPP roster. If none is present it
	 * returns the base name.
	 *
	 * @param user
	 * @return the user's nickname, or if none is set JID's base.
	 */
	fun getDisplayName(user: User?): String? {
		return UserFormatUtils.getDisplayName(user)
	}

	/**
	 * This method formats patterns for display in the UI by replacing
	 * occurrences of {@link User} objects by {@link #getDisplayName(User)}.
	 *
	 * @param pattern
	 * @param arguments
	 * occurrences of User objects are replaced by their display name
	 * @return the formatted string
	 */
	fun format(pattern: String?, vararg arguments: Any?): String? {
		val mappedValues = ArrayList<Any?>(arguments.size)
		for (obj in arguments) {
			if (obj is User) {
				val user = obj as User?
				mappedValues.add(getDisplayName(user))
			} else {
				mappedValues.add(obj)
			}
		}
		return MessageFormat.format(pattern, mappedValues.toArray())
	}
}// It's a utility class, no public ctor