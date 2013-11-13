package de.fu_berlin.inf.dpp;

import org.picocontainer.MutablePicoContainer;

/**
 * Interface for implementing context factories depending on the current
 * platform Saros is running on.
 */
public interface ISarosContextFactory {

    /**
     * Creates the runtime components for the Saros application. It is up to the
     * implementor to ensure to create all necessary components that are needed
     * during runtime on the given platform.
     * 
     * @param container
     *            the container to insert the components to
     */
    public void createComponents(MutablePicoContainer container);
}
