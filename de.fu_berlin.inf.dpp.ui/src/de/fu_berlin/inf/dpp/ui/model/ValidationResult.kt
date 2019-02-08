package de.fu_berlin.inf.dpp.ui.model

/**
 * This class is used to report the result of a parameter validation back to
 * Javascript. It is serialized to a JSON string.
 * @param valid
 * true if the validation was successful, false otherwise
 * @param message
 * the message saying why the validation failed
 */
data class ValidationResult(private val valid: Boolean, private val message: String?)