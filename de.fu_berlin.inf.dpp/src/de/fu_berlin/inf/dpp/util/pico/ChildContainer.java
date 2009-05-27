package de.fu_berlin.inf.dpp.util.pico;

import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.containers.AbstractDelegatingMutablePicoContainer;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.Reinjector;

import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * A child container is a marker class which delegates to a given delegate
 * container.
 *
 * It provides re-injection as a utility method.
 */
@Component(module = "pico")
public class ChildContainer extends AbstractDelegatingMutablePicoContainer {

    private static final Logger log = Logger.getLogger(ChildContainer.class
        .getName());

    protected Reinjector reinjector;

    public ChildContainer(MutablePicoContainer delegate) {
        super(delegate);
        reinjector = new Reinjector(delegate);
    }

    public void reinject(Object toInjectInto) {

        MutablePicoContainer container = getDelegate();

        try {
            // Remove the component if an instance of it was already registered
            container.removeComponent(toInjectInto.getClass());

            // Add the given instance to the container
            container.addComponent(toInjectInto.getClass(), toInjectInto);

            /*
             * Ask PicoContainer to inject into the component via fields
             * annotated with @Inject
             */
            reinjector.reinject(toInjectInto.getClass(),
                new AnnotatedFieldInjection());
        } catch (PicoCompositionException e) {
            log.error("Internal error in reinjection:", e);
        }
    }

    @Override
    public MutablePicoContainer makeChildContainer() {
        return super.getDelegate().makeChildContainer();
    }
}