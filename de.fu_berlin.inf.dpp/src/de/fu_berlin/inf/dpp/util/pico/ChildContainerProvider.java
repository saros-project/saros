package de.fu_berlin.inf.dpp.util.pico;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.Provider;

import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * This provider is responsible for creating ChildContainers of the Root
 * PicoContainer. It should be the only component outside of Saros, aware of the
 * rootContainer.
 */
@Component(module = "pico")
public class ChildContainerProvider implements Provider {

    protected MutablePicoContainer container;

    public ChildContainerProvider(MutablePicoContainer container) {
        this.container = container;
    }

    public ChildContainer provide() {
        return new ChildContainer(container.makeChildContainer());
    }

}
