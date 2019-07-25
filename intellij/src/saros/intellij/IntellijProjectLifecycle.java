package saros.intellij;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import saros.HTMLUIContextFactory;
import saros.context.AbstractContextLifecycle;
import saros.context.IContextFactory;
import saros.intellij.context.SarosIntellijContextFactory;
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

  private static IntellijProjectLifecycle instance;

  /**
   * Returns the current intellij project lifecycle instance. The returned instance is singleton. If
   * no instance is present, a new instance is instantiated.
   *
   * @return the current intellij project lifecycle instance
   */
  public static synchronized IntellijProjectLifecycle getInstance() {
    if (instance == null) {
      instance = new IntellijProjectLifecycle();
    }

    return instance;
  }

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    List<IContextFactory> nonCoreFactories = new ArrayList<>();

    nonCoreFactories.add(new SarosIntellijContextFactory());

    if (SarosComponent.isSwtBrowserEnabled()) {
      SwtLibLoader.loadSwtLib();
      nonCoreFactories.add(new HTMLUIContextFactory());
    }

    return nonCoreFactories;
  }
}
