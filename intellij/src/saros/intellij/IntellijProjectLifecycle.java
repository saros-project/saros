package saros.intellij;

import com.intellij.openapi.project.Project;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.HTMLUIContextFactory;
import saros.context.AbstractContextLifecycle;
import saros.context.IContextFactory;
import saros.intellij.context.SarosIntellijContextFactory;
import saros.intellij.project.ProjectWrapper;
import saros.intellij.ui.swt_browser.SwtLibLoader;

/**
 * Extends the {@link AbstractContextLifecycle} for an IntelliJ plug-in. It contains additional
 * IntelliJ specific fields and methods.
 *
 * <p>
 *
 * <p>This class is a singleton.
 */
/*
 * FIXME: Project lifecycle causes serious issues, switch back to application lifecycle and correct the file system implementation as needed !
 */
public class IntellijProjectLifecycle extends AbstractContextLifecycle {
  private static final Logger log = Logger.getLogger(IntellijProjectLifecycle.class);

  private static IntellijProjectLifecycle instance;
  private static ProjectWrapper projectWrapper;

  /**
   * Returns the current instance of IntellijProjectLifecycle.
   *
   * <p>IntellijProjectLifecycle is singleton, meaning this call will always return the same object.
   * If a new project object is passed, it will replace the currently held project object in the
   * plugin context.
   *
   * @param project the project object to put into the plugin context
   * @return the singleton instance of the IntellijProjectLifecycle
   */
  public static synchronized IntellijProjectLifecycle getInstance(@NotNull Project project) {
    if (instance != null) {
      if (projectWrapper.getProject().isDisposed()) {
        log.debug(
            "Replacing disposed project currently held in plugin context with project "
                + project.getName());

        projectWrapper.setProject(project);
      }

    } else {
      log.debug("Initializing plugin context with project " + project.getName());
      instance = new IntellijProjectLifecycle(project);
    }

    return instance;
  }

  private IntellijProjectLifecycle(Project project) {
    projectWrapper = new ProjectWrapper(project);
  }

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    List<IContextFactory> nonCoreFactories = new ArrayList<>();

    nonCoreFactories.add(new SarosIntellijContextFactory(projectWrapper));

    if (SarosComponent.isSwtBrowserEnabled()) {
      SwtLibLoader.loadSwtLib();
      nonCoreFactories.add(new HTMLUIContextFactory());
    }

    return nonCoreFactories;
  }
}
