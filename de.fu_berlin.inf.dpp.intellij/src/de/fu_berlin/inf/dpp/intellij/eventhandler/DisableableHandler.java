package de.fu_berlin.inf.dpp.intellij.eventhandler;

/** Provides a method to disable the listener held in the handler, thereby disabling the handler. */
public interface DisableableHandler {
  /**
   * Disables or enables the handler. Preferably, this should be done by un- and re-registering the
   * held listener. Otherwise, it could occur that the listener is called for an event that took
   * place while the listener was disabled. This is possible as Intellij does not always handles
   * listener dispatching synchronously with the actual event that triggers the listener.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  void setEnabled(boolean enabled);
}
