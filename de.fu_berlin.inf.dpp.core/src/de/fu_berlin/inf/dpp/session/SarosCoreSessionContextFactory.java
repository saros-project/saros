package de.fu_berlin.inf.dpp.session;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.session.internal.ActivityHandler;
import de.fu_berlin.inf.dpp.session.internal.ActivitySequencer;

/**
 * Basic {@link ISarosSessionContextFactory} implementation which creates the
 * {@link ISarosSession session} components defined in the core.
 * <p>
 * Applications should extend from this class and override
 * {@link #createNonCoreComponents} to create application-specific session
 * components.
 */
public class SarosCoreSessionContextFactory implements
    ISarosSessionContextFactory {

    @Override
    public final void createComponents(ISarosSession session,
        MutablePicoContainer container) {

        container.addComponent(ActivityHandler.class);
        container.addComponent(ActivitySequencer.class);

        // Non-Core Components
        createNonCoreComponents(session, container);
    }

    /**
     * Override this method in subclasses to add components to the session that
     * are not part of the core.
     * 
     * @param container
     *            DI container to add session components to
     */
    protected void createNonCoreComponents(ISarosSession session,
        MutablePicoContainer container) {
        // Does nothing by default
    }
}
