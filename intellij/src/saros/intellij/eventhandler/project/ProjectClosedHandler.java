package saros.intellij.eventhandler.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IProject;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.repackaged.picocontainer.Disposable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;

/**
 * Ends the current session when a project containing shared modules is closed and disables the
 * activity execution for resources of the closed project. This is done to avoid calls on disposed
 * project objects.
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
          Project sessionProject = sarosSession.getComponent(SharedIDEContext.class).getProject();

          if (sessionProject.equals(project)) {
            disableActivityExecutionForProjectModules(project);

            Thread shutdownThread =
                new Thread(
                    () -> {
                      assert !ApplicationManager.getApplication().isDispatchThread()
                          : "The session must not be shut down from the EDT";

                      sarosSessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
                    });

            shutdownThread.start();
          }
        }

        /**
         * Disables the activity execution for the modules of the given project.
         *
         * <p>This method is used to report the modules of a closed project to the session in order
         * to prevent it from trying to run any activities on disposed resources.
         *
         * @param project the project whose modules to disable the activity execution for
         * @see ISarosSession#setActivityExecution(saros.filesystem.IReferencePoint,boolean)
         */
        private void disableActivityExecutionForProjectModules(@NotNull Project project) {
          for (Module module : ModuleManager.getInstance(project).getModules()) {
            IProject wrappedModule;

            try {
              wrappedModule = new IntelliJProjectImpl(module);

            } catch (IllegalArgumentException exception) {
              continue;
            }

            if (sarosSession.isShared(wrappedModule)) {
              sarosSession.setActivityExecution(wrappedModule.getReferencePoint(), false);
            }
          }
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
