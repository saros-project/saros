package saros;

import org.picocontainer.PicoCompositionException;
import org.picocontainer.annotations.Inject;
import saros.context.IContainerContext;

/**
 * Provides the possibility to initialize a component with the components hold in the given {@link
 * saros.context.IContainerContext}.
 *
 * <p>Typically this is the context created by Saros while it's initialization.
 */
public class SarosPluginContext {

  private static IContainerContext containerContext;

  public static void setSarosContext(IContainerContext containerContext) {
    SarosPluginContext.containerContext = containerContext;
  }

  /**
   * Initializes a given instance of a component (class) by assigning values to all variables that
   * are annotated with an {@linkplain Inject} annotation.
   *
   * @param instance instance of a component which should be initialized
   * @throws PicoCompositionException if the initialization fails
   */
  public static void initComponent(Object instance) {
    containerContext.initComponent(instance);
  }
}
