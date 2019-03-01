package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.AbstractContextLifecycle;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.ui.swt_browser.SwtLibLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  private static IntellijProjectLifecycle instance;

  /**
   * Creates a new IntellijProjectLifecycle singleton instance from a project.
   *
   * @param project
   * @return
   */
  public static synchronized IntellijProjectLifecycle getInstance(Project project) {
    instance = new IntellijProjectLifecycle(project);

    return instance;
  }

  private Project project;

  private IntellijProjectLifecycle(Project project) {
    this.project = project;
  }

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    List<IContextFactory> nonCoreFactories = new ArrayList<>();

    nonCoreFactories.add(new SarosIntellijContextFactory(project));

    if (SarosComponent.isSwtBrowserEnabled()) {
      SwtLibLoader.loadSwtLib();
      nonCoreFactories.add(new HTMLUIContextFactory());
    }

    return nonCoreFactories;
  }
}
