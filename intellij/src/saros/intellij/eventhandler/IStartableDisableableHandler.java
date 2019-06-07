package saros.intellij.eventhandler;

import saros.repackaged.picocontainer.Startable;

/**
 * A handler that can be enabled/disabled at runtime.
 *
 * <p>The handler is disabled by default and is first started through {@link Startable#start()}. The
 * handler is disposed through {@link Startable#stop()}.
 */
public interface IStartableDisableableHandler extends Startable {

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
