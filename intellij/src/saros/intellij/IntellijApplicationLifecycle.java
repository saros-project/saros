package saros.intellij;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import saros.context.AbstractContextLifecycle;
import saros.context.IContextFactory;
import saros.intellij.context.SarosIntellijContextFactory;

/**
 * Extends {@link AbstractContextLifecycle} for the Intellij plug-in. It contains additional
 * Intellij specific fields and methods, defined in {@link SarosIntellijContextFactory}.
 *
 * <p>The lifecycle is singleton an can be obtained through {@link #getInstance()}.
 */
public class IntellijApplicationLifecycle extends AbstractContextLifecycle {

  private static IntellijApplicationLifecycle instance;

  /**
   * Returns the current intellij application lifecycle instance. The returned instance is
   * singleton. If no instance is present, a new instance is instantiated.
   *
   * @return the current intellij application lifecycle instance
   */
  public static synchronized IntellijApplicationLifecycle getInstance() {
    if (instance == null) {
      instance = new IntellijApplicationLifecycle();
    }

    return instance;
  }

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    List<IContextFactory> nonCoreFactories = new ArrayList<>();

    nonCoreFactories.add(new SarosIntellijContextFactory());

    return nonCoreFactories;
  }
}
