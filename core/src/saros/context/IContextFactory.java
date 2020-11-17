package saros.context;

import saros.repackaged.picocontainer.MutablePicoContainer;

/**
 * Interface for implementing context factories depending on the current platform Saros is running
 * on.
 */
public interface IContextFactory {

  /**
   * Creates the runtime components for the Saros application. It is up to the implementor to ensure
   * to create all necessary components that are needed during runtime on the given platform.
   *
   * @param container the container to insert the components to
   */
  public void createComponents(MutablePicoContainer container);
}
