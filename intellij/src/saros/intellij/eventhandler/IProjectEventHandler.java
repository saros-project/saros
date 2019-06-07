package saros.intellij.eventhandler;

import org.jetbrains.annotations.NotNull;

/**
 * A handler dealing with project specific events.
 *
 * @see IStartableDisableableHandler
 */
public interface IProjectEventHandler extends IStartableDisableableHandler {
  /**
   * Returns the type of the project event handler.
   *
   * @return the type of the project event handler
   */
  @NotNull
  ProjectEventHandlerType getHandlerType();

  /** The possible types of project event handlers. */
  enum ProjectEventHandlerType {
    DOCUMENT_MODIFICATION_HANDLER,
    EDITOR_STATUS_CHANGE_HANDLER,
    TEXT_SELECTION_CHANGE_HANDLER,
    VIEWPORT_CHANGE_HANDLER
  }
}
