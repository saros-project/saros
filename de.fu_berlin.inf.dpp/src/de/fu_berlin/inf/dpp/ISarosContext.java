package de.fu_berlin.inf.dpp;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

/**
 * Interface of a SarosContext. To be used by unit tests.
 */
public interface ISarosContext {

    /**
     * Injects dependencies into the annotated fields of the given object. This
     * method should be used for objects that were created by Eclipse, which
     * have a different life cycle than the Saros plug-in.
     */
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
}
