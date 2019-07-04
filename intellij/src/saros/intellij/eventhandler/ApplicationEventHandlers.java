package saros.intellij.eventhandler;

import com.intellij.openapi.project.Project;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import saros.intellij.eventhandler.IApplicationEventHandler.ApplicationEventHandlerType;

/**
 * A data holder class containing all application specific event handlers needed during a session.
 * Manages the interaction with the handlers grouped by the domain of the handler.
 *
 * <p>The contained handlers are not initialized/enabled by default. This can be done by calling
 * {@link #start()}.
 *
 * <p>Instances of this class can be obtained through {@link
 * ApplicationEventHandlersFactory#createApplicationEventHandler(Project)}.
 */
public class ApplicationEventHandlers {

  private final Map<ApplicationEventHandlerType, Set<IApplicationEventHandler>> handlers;

  /**
   * Initializes a <code>ApplicationEventHandlers</code> object with the given handlers. This
   * constructor should not be used directly. Use {@link ApplicationEventHandlersFactory} to obtain
   * instances of the class instead.
   */
  ApplicationEventHandlers(Set<IApplicationEventHandler> applicationEventHandlers) {
    this.handlers = new HashMap<>();

    for (IApplicationEventHandler applicationEventHandler : applicationEventHandlers) {
      handlers
          .computeIfAbsent(applicationEventHandler.getHandlerType(), key -> new HashSet<>())
          .add(applicationEventHandler);
    }
  }

  /**
   * Initializes and enables all held handlers.
   *
   * @see IEventHandler#initialize()
   */
  public void start() {
    for (Set<IApplicationEventHandler> eventHandlers : handlers.values()) {
      eventHandlers.forEach(IApplicationEventHandler::initialize);
    }
  }

  /**
   * Disables and disposes all held handlers. Subsequently drops all held handler references.
   *
   * @see IEventHandler#dispose()
   */
  public void stop() {
    for (Set<IApplicationEventHandler> eventHandlers : handlers.values()) {
      eventHandlers.forEach(IApplicationEventHandler::dispose);
      eventHandlers.clear();
    }

    handlers.clear();
  }

  /**
   * Enables or disables all held local filesystem modification handlers.
   *
   * @param enabled the new state of the held local filesystem modification handlers
   * @see IEventHandler#setEnabled(boolean)
   */
  public void setHandlersEnabled(@NotNull ApplicationEventHandlerType type, boolean enabled) {
    for (IApplicationEventHandler handler : handlers.get(type)) {
      handler.setEnabled(enabled);

      assert handler.isEnabled() == enabled : handler + " was not set to enabled=" + enabled;
    }
  }
}
