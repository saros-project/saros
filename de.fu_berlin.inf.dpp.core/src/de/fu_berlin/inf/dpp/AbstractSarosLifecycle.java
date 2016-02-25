package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract superclass which specifies and allows customization of the Saros
 * startup and shutdown process. It takes care of creating and disposing the
 * application context. Each platform that runs Saros (like IDE plug-ins or the
 * server) instantiates {@link SarosCoreContextFactory the same core components}
 * . But also platform specific components.
 * 
 * When the platform is ready to create the application context (e.g. the
 * plug-in is loading up), {@link #start()} has to be called. Override
 * {@link #additionalContextFactories()} to determine which platform specific
 * context factories will be added. You may override
 * {@link #initializeContext(SarosContext)} to perform additional init logic
 * <b>before</b> the context is instantiated.
 * 
 * To shut down Saros the method {@link #stop()} has to be called.
 * {@link #finalizeContext(SarosContext)} may be overwritten for that, too.
 */
public abstract class AbstractSarosLifecycle {

    private boolean isInitialized;

    private SarosContext sarosContext;

    /**
     * The implementation must return a collection of Saros context factories
     * which are needed for the specific platform. These will be added to the
     * Saros context in the {@link #start() start method}.
     * 
     * It may contain additional initialization logic like setting up other
     * components before its context factories are returned.
     * 
     * @return a collection of platform specific context factories.
     */
    protected abstract Collection<ISarosContextFactory> additionalContextFactories();

    /**
     * Performs additional initialization logic which will be called in the
     * {@link #start start method}. The call happens <b>after</b>
     * {@link SarosContext#initialize()} and <b>before</b>
     * {@link SarosPluginContext#setSarosContext(ISarosContext)}.
     * 
     * This method can be overwritten by the platform specific subclass but does
     * nothing by default.
     */
    protected void initializeContext(final SarosContext sarosContext) {
        // does nothing by default
    }

    /***
     * Performs additional finalization logic which will be called in the
     * {@link #stop() stop method}. The call happens <b>before</b>
     * {@link SarosContext#dispose()}.
     * 
     * This method can be overwritten by the platform specific subclass but does
     * nothing by default.
     */
    protected void finalizeContext(final SarosContext sarosContext) {
        // does nothing by default
    }

    /**
     * @return the SarosContext used by this lifecycle.
     * @throws IllegalStateException
     *             if the lifecycle is not running.
     */
    public final SarosContext getSarosContext() {
        if (!isInitialized || sarosContext == null)
            throw new IllegalStateException(
                "The SarosContext is not initialized yet.");

        return sarosContext;
    }

    /**
     * If not initialized yet (the Saros platform is starting up), this method
     * will create a {@link SarosCoreContextFactory} plus each context factory
     * {@link #additionalContextFactories()} returns. Additional initialization
     * logic will be called here.
     * 
     * @see #initializeContext
     */
    public final void start() {

        if (isInitialized) {
            return;
        }

        List<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();
        factories.add(new SarosCoreContextFactory());
        factories.addAll(additionalContextFactories());

        sarosContext = new SarosContext(factories, null);
        sarosContext.initialize();

        initializeContext(sarosContext);

        SarosPluginContext.setSarosContext(sarosContext);

        isInitialized = true;
    }

    /**
     * Disposes all (disposable) components created by the
     * {@link ISarosContextFactory context factories}. Additional finalization
     * logic will be called here.
     * 
     * @see #finalizeContext
     */
    public final void stop() {
        try {
            finalizeContext(sarosContext);
        } finally {
            /*
             * This will cause dispose() to be called on all components managed
             * by PicoContainer which implement {@link Disposable}.
             */
            sarosContext.dispose();
        }

        isInitialized = false;
    }
}