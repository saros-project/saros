package de.fu_berlin.inf.dpp;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Inject;

public interface ISarosContext {

    /**
     * Injects dependencies into the annotated fields of the given object. This
     * method should only be used for objects that cannot be put directly into
     * the context scope, i.e the objects are created by a third party.
     * 
     * @throws PicoCompositionException
     *             if the initialization fails
     * 
     * @see Inject
     */
    @Deprecated
    void initComponent(Object object);

    /**
     * This should only be used by SarosSession code. Make sure to release the
     * child container to prevent a memory leak
     * 
     * @return Create a new child container
     */
    MutablePicoContainer createSimpleChildContainer();

    /**
     * Remove the given child from this contexts container.
     * 
     * @param picoContainer
     * @return
     */
    boolean removeChildContainer(PicoContainer picoContainer);

    /**
     * Retrieve a component keyed by the component type.
     * 
     * @param componentType
     *            the type of the component
     * @return the typed resulting object instance or <code>null</code> if the
     *         object does not exist.
     */
    <T> T getComponent(Class<T> componentType);
}
