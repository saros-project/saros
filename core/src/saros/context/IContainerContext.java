package de.fu_berlin.inf.dpp.context;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

/** Interface for accessing components of a (container based) context. */
public interface IContainerContext {

  /**
   * Injects dependencies into the annotated fields of the given object. This method should only be
   * used for objects that cannot be put directly into the context scope, i.e the objects are
   * created by a third party.
   *
   * @throws PicoCompositionException if the initialization fails
   */
  @Deprecated
  public void initComponent(Object object);

  /**
   * This should only be used by SarosSession code. Make sure to release the child container to
   * prevent a memory leak
   *
   * @return Create a new child container
   */
  public MutablePicoContainer createChildContainer();

  /**
   * Remove the given child from this contexts container.
   *
   * @param picoContainer
   * @return
   */
  public boolean removeChildContainer(PicoContainer picoContainer);

  /**
   * Retrieve a component keyed by the component type.
   *
   * @param componentType the type of the component
   * @return the typed resulting object instance or <code>null</code> if the object does not exist.
   */
  public <T> T getComponent(Class<T> componentType);
}
