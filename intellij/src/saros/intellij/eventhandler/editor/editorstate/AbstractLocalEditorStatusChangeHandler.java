package saros.intellij.eventhandler.editor.editorstate;

import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Startable;
import saros.intellij.eventhandler.DisableableHandler;
import saros.intellij.project.ProjectWrapper;

/**
 * Abstract class defining the base functionality needed to create and register/unregister a
 * disableable local editor status change handler.
 *
 * <p>Each handler extending this class must implement {@link
 * #registerListeners(MessageBusConnection)}, which is called in {@link #subscribe()} with the
 * initialized MessageBusConnection object to register the needed listeners.
 *
 * <p>The handle is enabled and listeners are registered by default. To change this behavior, {@link
 * #start} should be overwritten by the implementation.
 *
 * @see MessageBusConnection#subscribe(Topic, Object)
 */
public abstract class AbstractLocalEditorStatusChangeHandler
    implements DisableableHandler, Startable {

  private final ProjectWrapper projectWrapper;

  private boolean enabled;
  private boolean disposed;

  private MessageBusConnection messageBusConnection;

  /**
   * Abstract class for local editor status change handlers. The handler is enabled by default and
   * the listeners are also registered by default.
   *
   * @param projectWrapper the current Intellij project wrapper instance
   */
  AbstractLocalEditorStatusChangeHandler(ProjectWrapper projectWrapper) {
    this.projectWrapper = projectWrapper;

    this.enabled = false;
    this.disposed = false;
  }

  @Override
  public void start() {
    setEnabled(true);
  }

  @Override
  public void stop() {
    disposed = true;
    setEnabled(false);
  }

  /**
   * Initializes the held MessageBusConnection and calls {@link
   * #registerListeners(MessageBusConnection)} to register any needed handlers.
   */
  private void subscribe() {
    messageBusConnection = projectWrapper.getProject().getMessageBus().connect();

    registerListeners(messageBusConnection);
  }

  /**
   * Registers the needed listeners with the given MessageBusConnection.
   *
   * <p>This method should not be called directly as it is only meant to be used indirectly as part
   * of the {@link #subscribe()} call.
   *
   * @param messageBusConnection the current MessageBusConnection
   */
  abstract void registerListeners(@NotNull MessageBusConnection messageBusConnection);

  /** Disconnects and drops the held MessageBusConnection. */
  private void unsubscribe() {
    messageBusConnection.disconnect();

    messageBusConnection = null;
  }

  /**
   * Enables or disables the handler. This is done by registering or unregistering the held
   * listener.
   *
   * <p>This method does nothing if the given state already matches the current state.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  @Override
  public void setEnabled(boolean enabled) {
    assert !disposed || !enabled : "disposed listeners must not be enabled";

    if (this.enabled && !enabled) {
      unsubscribe();

      this.enabled = false;

    } else if (!this.enabled && enabled) {
      subscribe();

      this.enabled = true;
    }
  }

  /**
   * Returns whether the handler is currently enabled. This also represents whether there currently
   * are any listeners registered.
   *
   * @return whether the handler is currently enabled
   */
  boolean isEnabled() {
    return enabled;
  }
}
