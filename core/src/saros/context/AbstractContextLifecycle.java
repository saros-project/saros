package saros.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import saros.SarosPluginContext;

/**
 * Abstract superclass which specifies and allows customization of the Saros startup and shutdown
 * process. It takes care of creating and disposing the context. Each platform that runs Saros (like
 * IDE plug-ins or the server) instantiates {@link CoreContextFactory the same core components} .
 * But also platform specific components.
 *
 * <p>When the platform is ready to create the context (e.g. the plug-in is loading up), {@link
 * #start()} has to be called. Override {@link #additionalContextFactories()} to determine which
 * platform specific context factories will be added.
 *
 * <p>You may override {@link #initializeContext(ContainerContext)} to perform additional
 * initialization logic <b>before</b> the context is instantiated.
 *
 * <p>To shut down Saros the method {@link #stop()} has to be called. {@link
 * #finalizeContext(ContainerContext)} may be overwritten for that, too.
 */
public abstract class AbstractContextLifecycle {

  private boolean isInitialized;

  private ContainerContext containerContext;

  /**
   * The implementation must return a collection of Saros context factories which are needed for the
   * specific platform. These will be added to the Saros context in the {@link #start() start
   * method}.
   *
   * <p>It may contain additional initialization logic like setting up other components before its
   * context factories are returned.
   *
   * @return a collection of platform specific context factories.
   */
  protected abstract Collection<IContextFactory> additionalContextFactories();

  /**
   * Performs additional initialization logic which will be called in the {@link #start start
   * method}. The call happens <b>after</b> {@link ContainerContext#initialize()} and <b>before</b>
   * {@link SarosPluginContext#setSarosContext(IContainerContext)}.
   *
   * <p>This method can be overwritten by the platform specific subclass but does nothing by
   * default.
   */
  protected void initializeContext(final ContainerContext containerContext) {
    // does nothing by default
  }

  /**
   * * Performs additional finalization logic which will be called in the {@link #stop() stop
   * method}. The call happens <b>before</b> {@link ContainerContext#dispose()}.
   *
   * <p>This method can be overwritten by the platform specific subclass but does nothing by
   * default.
   */
  protected void finalizeContext(final ContainerContext containerContext) {
    // does nothing by default
  }

  /**
   * @return the ContainerContext used by this lifecycle.
   * @throws IllegalStateException if the lifecycle is not running.
   */
  public final ContainerContext getSarosContext() {
    if (!isInitialized || containerContext == null)
      throw new IllegalStateException("context is not initialized yet");

    return containerContext;
  }

  /**
   * If not initialized yet (the Saros platform is starting up), this method will create a {@link
   * CoreContextFactory} plus each context factory {@link #additionalContextFactories()} returns.
   * Additional initialization logic will be called here.
   *
   * @see #initializeContext
   */
  public final void start() {

    if (isInitialized) {
      return;
    }

    List<IContextFactory> factories = new ArrayList<IContextFactory>();
    factories.add(new CoreContextFactory());
    factories.addAll(additionalContextFactories());

    containerContext = new ContainerContext(factories, null);
    containerContext.initialize();

    initializeContext(containerContext);

    SarosPluginContext.setSarosContext(containerContext);

    isInitialized = true;
  }

  /**
   * Disposes all (disposable) components created by the {@link IContextFactory context factories}.
   * Additional finalization logic will be called here.
   *
   * @see #finalizeContext
   */
  public final void stop() {
    try {
      finalizeContext(containerContext);
    } finally {
      /*
       * This will cause dispose() to be called on all components managed
       * by PicoContainer which implement {@link Disposable}.
       */
      containerContext.dispose();
      containerContext = null;
      isInitialized = false;
    }
  }
}
