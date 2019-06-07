package saros.intellij.eventhandler;

import org.jetbrains.annotations.NotNull;

/**
 * A handler dealing with global/application-level events.
 *
 * @see IEventHandler
 */
public interface IApplicationEventHandler extends IEventHandler {
  /**
   * Returns the type of the application event handler.
   *
   * @return the type of the application event handler
   */
  @NotNull
  ApplicationEventHandlerType getHandlerType();

  /** The possible types of application event handlers. */
  enum ApplicationEventHandlerType {
    LOCAL_FILESYSTEM_MODIFICATION_HANDLER
  }
}
