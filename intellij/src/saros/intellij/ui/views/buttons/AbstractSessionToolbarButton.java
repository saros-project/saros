package saros.intellij.ui.views.buttons;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import javax.swing.ImageIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.SarosPluginContext;
import saros.intellij.ui.views.SarosMainPanelView;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

/**
 * Abstract parent class representing a session toolbar button, i.e. a button that is only enabled
 * during an active Saros session.
 *
 * <p>The class offers methods to react to a session starting or ending and to update the initial
 * state of the button.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link SarosMainPanelView}. This class offers the
 * method {@link #disposeComponents()} for such a purpose.
 */
abstract class AbstractSessionToolbarButton extends AbstractToolbarButton implements Disposable {
  protected final Project project;

  @Inject protected ISarosSessionManager sarosSessionManager;

  @SuppressWarnings("FieldCanBeLocal")
  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession session) {
          AbstractSessionToolbarButton.this.sessionStarted(session);
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          AbstractSessionToolbarButton.this.sessionEnded(session, reason);
        }
      };

  /**
   * Initializes the button. Also initializes all tagged instance variables with the matching
   * objects from the plugin context.
   *
   * @see AbstractToolbarButton#AbstractToolbarButton(String, String, ImageIcon)
   * @see SarosPluginContext#initComponent(Object)
   */
  AbstractSessionToolbarButton(
      @NotNull Project project,
      @NotNull String actionCommand,
      @Nullable String tooltipText,
      @Nullable ImageIcon icon) {

    super(actionCommand, tooltipText, icon);

    this.project = project;

    Disposer.register(project, this);

    SarosPluginContext.initComponent(this);

    sarosSessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  /**
   * Calls {@link #sessionStarted(ISarosSession)} if there is currently a saros session.
   *
   * <p>This should be called by all implementing classes as the last call of the constructor to
   * ensure the button is correctly enabled and initialized if the class is instantiated while there
   * is already a running session.
   */
  void setInitialState() {
    ISarosSession session = sarosSessionManager.getSession();

    if (session != null) {
      sessionStarted(session);
    }
  }

  @Override
  public final void dispose() {
    sarosSessionManager.removeSessionLifecycleListener(sessionLifecycleListener);

    disposeComponents();
  }

  /**
   * Method to dispose components of the button.
   *
   * <p>This method is called when the project the button belongs to is disposed. It should drop any
   * internal state that would prevent the object from being garbage collected.
   */
  abstract void disposeComponents();

  /**
   * Method to react to the start of a new session.
   *
   * @param session the started session
   */
  abstract void sessionStarted(final ISarosSession session);

  /**
   * Method to react to the end of the current session.
   *
   * @param oldSarosSession the session that just ended
   * @param reason the reason the session ended
   */
  abstract void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason);
}
