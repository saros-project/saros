package saros.intellij.eventhandler;

import com.intellij.openapi.project.Project;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import saros.intellij.eventhandler.IProjectEventHandler.ProjectEventHandlerType;

/**
 * A data holder class containing all project specific event handlers needed during a session.
 * Manages the interaction with the handlers grouped by the domain of the handler.
 *
 * <p>The contained handlers are not initialized/enabled by default. This can be done by calling
 * {@link #start()}.
 *
 * <p>Instances of this class can be obtained through {@link
 * ProjectEventHandlersFactory#createProjectEventHandlers(Project)}.
 */
public class ProjectEventHandlers {

  private final Map<ProjectEventHandlerType, Set<IProjectEventHandler>> handlers;

  /**
   * Initializes a <code>ProjectEventHandlers</code> object with the given handlers. This
   * constructor should not be used directly. Use {@link ProjectEventHandlersFactory} to obtain
   * instances of the class instead.
   */
  ProjectEventHandlers(Set<IProjectEventHandler> projectEventHandlers) {
    handlers = new HashMap<>();

    for (IProjectEventHandler projectEventHandler : projectEventHandlers) {
      handlers
          .computeIfAbsent(projectEventHandler.getHandlerType(), key -> new HashSet<>())
          .add(projectEventHandler);
    }
  }

  /**
   * Initializes and enables all held handlers.
   *
   * @see IProjectEventHandler#initialize()
   */
  public void start() {
    for (Set<IProjectEventHandler> eventHandlers : handlers.values()) {
      eventHandlers.forEach(IProjectEventHandler::initialize);
    }
  }

  /**
   * Disables and disposes all held handlers. Subsequently drops all held handler references.
   *
   * @see IProjectEventHandler#dispose()
   */
  public void stop() {
    for (Set<IProjectEventHandler> eventHandlers : handlers.values()) {
      eventHandlers.forEach(IProjectEventHandler::dispose);
      eventHandlers.clear();
    }

    handlers.clear();
  }

  /**
   * Enables or disables all held handlers of the given type.
   *
   * @param enabled the new state of the held handlers of the given type
   * @see IProjectEventHandler#setEnabled(boolean)
   */
  public void setHandlersEnabled(@NotNull ProjectEventHandlerType type, boolean enabled) {
    for (IProjectEventHandler handler : handlers.get(type)) {
      handler.setEnabled(enabled);

      assert handler.isEnabled() == enabled : handler + " was not set to enabled=" + enabled;
    }
  }

  /**
   * Returns whether the handlers of the given type are enabled.
   *
   * @return whether the handlers of the given type are enabled
   */
  public boolean areHandlersEnabled(@NotNull ProjectEventHandlerType type) {
    return handlers.get(type).stream().allMatch(IProjectEventHandler::isEnabled);
  }
}
