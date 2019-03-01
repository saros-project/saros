package de.fu_berlin.inf.dpp.ui.model;

/**
 * This class is used to report the result of a parameter validation back to Javascript. It is
 * serialized to a JSON string.
 */
public class ValidationResult {

  private final boolean valid;

  private final String message;

  /**
   * @param valid true if the validation was successful, false otherwise
   * @param message the message saying why the validation failed
   */
  public ValidationResult(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }
}
