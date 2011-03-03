package de.fu_berlin.inf.dpp;

/**
 * Provides the possibility to initialize a component with the components hold in
 * the given {@link de.fu_berlin.inf.dpp.SarosContext}. You can set the context
 * via {@link de.fu_berlin.inf.dpp.SarosPluginContext#setSarosContext(SarosContext)}.
 *
 * Typically this is the context created by {@link de.fu_berlin.inf.dpp.Saros} while it's initialization.
 *
 * @author philipp.cordes
 */
public class SarosPluginContext {

    private static SarosContext sarosContext;

    public static void setSarosContext(SarosContext sarosContext) {
        SarosPluginContext.sarosContext = sarosContext;
    }

    /**
     * Use this only in eclipse-specific components like actions or views. If you want to
     * initialize a saros-specific component which is located in our 'business-logic'
     * you should use {@link de.fu_berlin.inf.dpp.SarosContext#initComponent(Object)}
     * directly from context of the current Saros. This should establish the idea of
     * a single context per saros-instance (in Test-Mode). 
     *
     * @param toInjectInto component which should to be initialized.
     */
    public static void initComponent(Object toInjectInto) {
        if (sarosContext.isTestContext())
            throw new RuntimeException(
                    "\n\n*** \n\n" +
                    "You want to initiliaze a component with a test-context!\n" +
                    "This Exception should ensure that no eclipse-specific code will be run by a unit-test.\n" +
                    "If the caller is not eclipse-specific you must get the context of the current " +
                    "Saros and use {@link de.fu_berlin.inf.dpp.SarosContext#initComponent(Object)} directly." +
                    "\n\n*** \n\n");

        Saros.checkInitialized();
        sarosContext.initComponent(toInjectInto);
    }

    /**
     * Use this only in eclipse-specific components like actions or views. If you want to
     * reinject a saros-specific component which is located in our 'business-logic'
     * you should use {@link de.fu_berlin.inf.dpp.SarosContext#reinject(Object)}
     * directly from context of the current Saros. This should establish the idea of
     * a single context per saros-instance (in Test-Mode).
     *
     * @param toReinject component which should to be reinjected.
     */
    public static void reinject(Object toReinject) {
        if (sarosContext.isTestContext())
            throw new RuntimeException(
                    "\n\n*** \n\n" +
                    "You want to reinject a component in a test-context!\n" +
                    "This Exception should ensure that no eclipse-specific code will be run by a unit-test.\n" +
                    "If the caller is not eclipse-specific you must get the context of the current " +
                    "Saros and use {@link de.fu_berlin.inf.dpp.SarosContext#initComponent(Object)} directly." +
                    "\n\n*** \n\n");

        Saros.checkInitialized();
        sarosContext.reinject(toReinject);
    }
}
