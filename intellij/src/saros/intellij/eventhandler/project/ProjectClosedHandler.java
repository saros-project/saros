package saros.intellij.eventhandler.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IReferencePoint;
import saros.intellij.filesystem.IntellijReferencePoint;
import saros.repackaged.picocontainer.Disposable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;

/**
 * Ends the current session when a project containing shared reference points is closed and disables
 * the activity execution for resources of the closed project. This is done to avoid calls on
 * disposed project objects.
 *
 * <p>The handler is enabled by default after instantiation.
 */
public class ProjectClosedHandler implements Disposable {

  private final ISarosSessionManager sarosSessionManager;
  private final ISarosSession sarosSession;

  private final MessageBusConnection messageBusConnection;

  @SuppressWarnings("FieldCanBeLocal")
  private final ProjectManagerListener projectManagerListener =
      new ProjectManagerListener() {
        @Override
        public void projectClosing(@NotNull Project project) {
          boolean closedProjectContainedSharedResources = false;

          for (IReferencePoint referencePoint : sarosSession.getProjects()) {
            IntellijReferencePoint intellijReferencePoint = (IntellijReferencePoint) referencePoint;

            if (intellijReferencePoint.getIntellijProject().equals(project)) {
              sarosSession.setActivityExecution(intellijReferencePoint, false);

              closedProjectContainedSharedResources = true;
            }
          }

          if (!closedProjectContainedSharedResources) {
            return;
          }

          Thread shutdownThread =
              new Thread(
                  () -> {
                    assert !ApplicationManager.getApplication().isDispatchThread()
                        : "The session must not be shut down from the EDT";

                    sarosSessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
                  });

          shutdownThread.start();
        }
      };

  /**
   * Instantiates the <code>ProjectClosedHandler</code>. Registers the held <code>
   * ProjectManagerListener</code> with the application <code>MessageBus</code>.
   *
   * @param sarosSessionManager the <code>SarosSessionManager</code> instance
   * @param sarosSession the current <code>SarosSession</code>
   * @see MessageBusConnection
   * @see ProjectManagerListener
   */
  public ProjectClosedHandler(
      ISarosSessionManager sarosSessionManager, ISarosSession sarosSession) {

    this.sarosSessionManager = sarosSessionManager;
    this.sarosSession = sarosSession;

    messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect();
    messageBusConnection.subscribe(ProjectManager.TOPIC, projectManagerListener);
  }

  /** Disconnects from the message bus when the plugin context is disposed. */
  @Override
  public void dispose() {
    messageBusConnection.disconnect();
  }
}
