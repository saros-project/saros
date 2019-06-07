package saros.intellij.eventhandler;

/**
 * A handler that can be enabled/disabled at runtime.
 *
 * <p>The handler <b>must</b> be disabled by default. It can be initialized and started through
 * {@link #initialize()}. The handler is stopped and disposed through {@link #dispose()}.
 */
public interface IEventHandler {

  /** Initializes and subsequently enables the event handler. */
  void initialize();

  /** Disables and subsequently disposes the event handler. */
  void dispose();

  /**
   * Disables or enables the handler. Preferably, this should be done by un- and re-registering the
   * held listener. Otherwise, it could occur that the listener is called for an event that took
   * place while the listener was disabled. This is possible as Intellij does not always handles
   * listener dispatching synchronously with the actual event that triggers the listener.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  void setEnabled(boolean enabled);

  /**
   * Returns whether the handler currently is enabled.
   *
   * @return whether the handler currently is enabled
   */
  boolean isEnabled();
}
