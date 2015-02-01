package de.fu_berlin.inf.dpp;

import org.picocontainer.annotations.Inject;

/**
 * Provides the possibility to initialize a component with the components hold
 * in the given {@link de.fu_berlin.inf.dpp.SarosContext}.
 * 
 * Typically this is the context created by {@link de.fu_berlin.inf.dpp.Saros}
 * while it's initialization.
 * 
 * @author philipp.cordes
 */
public class SarosPluginContext {

    private static SarosContext sarosContext;

    static void setSarosContext(SarosContext sarosContext) {
        SarosPluginContext.sarosContext = sarosContext;
    }

    /**
     * Initializes a given instance of a component (class) by assigning values
     * to all variables that are annotated with an {@linkplain Inject}
     * annotation.
     * <p>
     * <b>Note:</b> If the requested values (components) are not present in the
     * current context a <code>null</code> value will be assigned!
     * 
     * @param instance
     *            instance of a component which should be initialized
     */
    public static void initComponent(Object instance) {
        Saros.checkInitialized();
        sarosContext.initComponent(instance);
    }
}
