package de.fu_berlin.inf.dpp;

/**
 * Provides the possibility to initialize a component with the components hold
 * in the given {@link de.fu_berlin.inf.dpp.SarosContext}. You can set the
 * context via
 * {@link de.fu_berlin.inf.dpp.SarosPluginContext#setSarosContext(SarosContext)}
 * .
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
     * Use this only in eclipse-specific components like actions or views. If
     * you want to initialize a saros-specific component which is located in our
     * 'business-logic' you should use
     * {@link de.fu_berlin.inf.dpp.SarosContext#initComponent(Object)} directly
     * from context of the current Saros.
     * 
     * @param toInjectInto
     *            component which should to be initialized.
     */
    public static void initComponent(Object toInjectInto) {
        Saros.checkInitialized();
        sarosContext.initComponent(toInjectInto);
    }

    /**
     * Use this only in eclipse-specific components like actions or views. If
     * you want to reinject a saros-specific component which is located in our
     * 'business-logic' you should use
     * {@link de.fu_berlin.inf.dpp.SarosContext#reinject(Object)} directly from
     * context of the current Saros.
     * 
     * @param toReinject
     *            component which should to be reinjected.
     */
    public static void reinject(Object toReinject) {
        Saros.checkInitialized();
        sarosContext.reinject(toReinject);
    }
}
