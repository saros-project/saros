package saros.intellij.context;

import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.intellij.eventhandler.ApplicationEventHandlers;
import saros.intellij.eventhandler.ApplicationEventHandlersFactory;
import saros.intellij.eventhandler.IApplicationEventHandler.ApplicationEventHandlerType;
import saros.intellij.eventhandler.IEventHandler;
import saros.intellij.eventhandler.IProjectEventHandler.ProjectEventHandlerType;
import saros.intellij.eventhandler.ProjectEventHandlers;
import saros.intellij.eventhandler.ProjectEventHandlersFactory;
import saros.repackaged.picocontainer.Disposable;

/**
 * Singleton class representing the shared IDE context. It contains the session components dependent
 * on IDE specific resources. This mainly consist of the shared project references and the
 * application- and project level IDE event handlers.
 *
 * <p>The IDE event handlers are started when the shared project object is available. This can
 * either happen when the context is initialized if the shared project was set through {@link
 * #preregisterProject(Project)} or when the project is set at runtime through {@link
 * #setProject(Project)}.
 */
public class SharedIDEContext implements Disposable {
  private static Logger log = Logger.getLogger(SharedIDEContext.class);

  private static Project preregisteredProject;

  private final ApplicationEventHandlersFactory applicationEventHandlersFactory;
  private final ProjectEventHandlersFactory projectEventHandlersFactory;

  private Project project;
  private ApplicationEventHandlers applicationEventHandlers;
  private ProjectEventHandlers projectEventHandlers;

  public SharedIDEContext(
      ApplicationEventHandlersFactory applicationEventHandlersFactory,
      ProjectEventHandlersFactory projectEventHandlersFactory,
      Project project) {

    this.applicationEventHandlersFactory = applicationEventHandlersFactory;
    this.projectEventHandlersFactory = projectEventHandlersFactory;

    if (preregisteredProject != null) {
      startProjectComponents(preregisteredProject);
    } else {
      // TODO remove this workaround once the shared project object is correctly initialized
      startProjectComponents(project);
    }
  }

  /**
   * Pre-registers a project object that will be used to initialize the context once it is
   * instantiated.
   *
   * <p>This method should be used to set the shared project object when it is known before the
   * session context is created, i.e. on the host side of the negotiation.
   *
   * @param newPreregisteredProject the shared project
   */
  public static void preregisterProject(@NotNull Project newPreregisteredProject) {
    if (preregisteredProject != null) {
      log.warn(
          "Overwriting existing pre-registered project! old project: "
              + preregisteredProject
              + ", new project: "
              + newPreregisteredProject);
    }

    preregisteredProject = newPreregisteredProject;
  }

  /**
   * Sets the held project object and initializes and starts the application- and project-level
   * event handlers.
   *
   * <p>This method should be used to set the shared project object when it is only known after the
   * session context was created, i.e. on the client of the negotiation.
   *
   * @param newProject the shared project
   * @throws IllegalStateException if the context already has a project object
   */
  public void setProject(@NotNull Project newProject) {
    if (project != null) {
      throw new IllegalStateException(
          "Overwriting existing session project! old session project: "
              + project
              + ", new session project: "
              + newProject);
    }

    startProjectComponents(newProject);
  }

  /**
   * Sets the held project object and initializes and starts the application- and project-level
   * event handlers.
   *
   * @param project the shared project
   */
  private void startProjectComponents(@NotNull Project project) {
    this.project = project;

    startApplicationListeners(project);
    startProjectListeners(project);
  }

  /**
   * Starts all application-level event handlers.
   *
   * @param project the shared project
   */
  // TODO make this project independent and call it in constructor
  private void startApplicationListeners(@NotNull Project project) {
    assert applicationEventHandlers == null : "application level handlers already initialized";

    applicationEventHandlers =
        applicationEventHandlersFactory.createApplicationEventHandler(project);

    applicationEventHandlers.start();
  }

  /**
   * Starts all project-level event handlers for the given project.
   *
   * @param project the shared project to initialize the event handlers for
   */
  private void startProjectListeners(@NotNull Project project) {
    assert projectEventHandlers == null : "project level handlers already initialized";

    projectEventHandlers = projectEventHandlersFactory.createProjectEventHandlers(project);

    projectEventHandlers.start();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Stops and disposes all registered application- and project-level event handlers.
   *
   * @see IEventHandler#dispose()
   */
  @Override
  public void dispose() {
    applicationEventHandlers.stop();
    projectEventHandlers.stop();
  }

  /**
   * Returns the project object for the current session.
   *
   * @return the project object for the current session
   * @throws IllegalStateException if this is called before the context was completely initialized
   */
  @NotNull
  public Project getProject() {
    if (project == null) {
      throw new IllegalStateException("Called the context before it was completely initialized.");
    }

    return project;
  }

  /**
   * Enables or disables the application-level handlers of the given type.
   *
   * @param type the type of the handlers to enable/disable
   * @param enabled the new state of the event handlers
   * @throws IllegalStateException if this is called before the context was completely initialized
   * @see ApplicationEventHandlers#setHandlersEnabled(ApplicationEventHandlerType, boolean)
   */
  public void setApplicationEventHandlersEnabled(
      @NotNull ApplicationEventHandlerType type, boolean enabled) {

    if (applicationEventHandlers == null) {
      throw new IllegalStateException("Called the context before it was completely initialized.");
    }

    applicationEventHandlers.setHandlersEnabled(type, enabled);
  }

  /**
   * Enables or disables the project-level event handlers of the given type.
   *
   * @param type the type of the event handlers to enable/disable
   * @param enabled the new state of the event handlers
   * @throws IllegalStateException if this is called before the context was completely initialized
   * @see ProjectEventHandlers#setHandlersEnabled(ProjectEventHandlerType, boolean)
   */
  public void setProjectEventHandlersEnabled(
      @NotNull ProjectEventHandlerType type, boolean enabled) {
    if (projectEventHandlers == null) {
      throw new IllegalStateException("Called the context before it was completely initialized.");
    }

    projectEventHandlers.setHandlersEnabled(type, enabled);
  }

  /**
   * Returns whether the project-level event handlers of the given type are enabled.
   *
   * @param type the type of the event handlers to enable/disable
   * @return whether the project-level event handlers of the given type are enabled
   * @throws IllegalStateException if this is called before the context was completely initialized
   * @see ProjectEventHandlers#areHandlersEnabled(ProjectEventHandlerType)
   */
  public boolean areProjectEventHandlersEnabled(@NotNull ProjectEventHandlerType type) {
    if (projectEventHandlers == null) {
      throw new IllegalStateException("Called the context before it was completely initialized.");
    }

    return projectEventHandlers.areHandlersEnabled(type);
  }
}
