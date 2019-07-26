package saros.intellij.ui.util;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

/**
 * Class to provide access to an active project object. This class should only be used for UI
 * purposes. To interact with the project whose module is shared as part of a Saros session, please
 * use the Project object contained in the session context.
 */
public class UIProjectUtils {
  private volatile ISarosSession sarosSession;

  @SuppressWarnings("FieldCanBeLocal")
  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession session) {
          sarosSession = session;
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          sarosSession = null;
        }
      };

  public UIProjectUtils(ISarosSessionManager sarosSessionManager) {
    sarosSessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  /**
   * Returns the <code>Project</code> object contained in the session context or <code>null</code>
   * if there currently is no session.
   *
   * @return the <code>Project</code> object contained in the session context or <code>null</code>
   *     if there currently is no session.
   */
  @Nullable
  Project getSharedProject() {
    return getProjectFromSession();
  }

  /**
   * Returns the <code>Project</code> object contained in the session context or <code>null</code>
   * if there currently is no session.
   *
   * <p><b>NOTE:</b> This method should only be used to for UI purposes. To interact with the
   * project whose module is shared as part of a Saros session, please use the Project object
   * contained in the session context.
   *
   * @return the <code>Project</code> object contained in the session context or <code>null</code>
   *     if there currently is no session.
   */
  @Nullable
  private Project getProjectFromSession() {
    ISarosSession currentSession = sarosSession;

    if (currentSession == null) {
      return null;
    }

    return currentSession.getComponent(Project.class);
  }

  /**
   * Runs the given method with a current project object.
   *
   * <p>If there is currently a session, the given method is run immediately using the project
   * contained in the session context. Otherwise, the currently focused project is requested and the
   * method is run asynchronously when the request is done. In this case, the used project object
   * can be <code>null</code> if the request fails.
   *
   * <p><b>NOTE:</b> This method should only be used to for UI purposes. To interact with the
   * project whose module is shared as part of a Saros session, please use the Project object
   * contained in the session context.
   *
   * @param projectRunner the method to call with the currently focused project
   */
  public void runWithProject(@NotNull ProjectRunner projectRunner) {
    Project sessionProject = getProjectFromSession();

    if (sessionProject != null) {
      projectRunner.run(sessionProject);

      return;
    }

    Promise<DataContext> promisedDataContext =
        DataManager.getInstance().getDataContextFromFocusAsync();
    Promise<Project> promisedProject = promisedDataContext.then(LangDataKeys.PROJECT::getData);

    promisedProject.onProcessed(projectRunner::run);
  }

  /** Interface used to define tasks that need a project to run. */
  public interface ProjectRunner {

    /**
     * Implement this method to define a task that can be run with a current project object using
     * {@link #runWithProject(ProjectRunner)}.
     *
     * @param project the <code>Project</code> object held in the current session context or, if
     *     there currently is no running session, either the currently focused project or <code>null
     *     </code> if there is no such project
     * @see #runWithProject(ProjectRunner)
     */
    void run(@Nullable Project project);
  }
}
