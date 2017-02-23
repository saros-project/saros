package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.context.AbstractContextLifecycle;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.context.ContainerContext;
import de.fu_berlin.inf.dpp.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.ui.browser.EclipseHTMLUIContextFactory;

/**
 * Extends the {@link AbstractContextLifecycle} for an Eclipse plug-in. It
 * contains additional Eclipse specific fields and methods.
 * 
 * This class is a singleton.
 */
public class EclipsePluginLifecycle extends AbstractContextLifecycle {

    private static EclipsePluginLifecycle instance;

    /**
     * Feature toggle for displaying Saros in a web browser in an additional
     * view. Also checks if required bundle is present.
     * 
     * @return true if this feature is enabled, false otherwise
     */
    private static boolean isSWTBrowserEnabled() {
        // TODO store constant string elsewhere
        return Platform.getBundle("de.fu_berlin.inf.dpp.ui") != null
            && Boolean.getBoolean("saros.swtbrowser");
    }

    /**
     * @param saros
     *            A reference to the Saros/E plug-in class.
     * @return the EclipsePluginLifecycle singleton instance.
     */
    public static synchronized EclipsePluginLifecycle getInstance(Saros saros) {
        if (instance == null) {
            instance = new EclipsePluginLifecycle(saros);
        }
        return instance;
    }

    private Saros saros;

    private EclipsePluginLifecycle(Saros saros) {
        this.saros = saros;
    }

    @Override
    protected Collection<IContextFactory> additionalContextFactories() {
        List<IContextFactory> nonCoreFactories = new ArrayList<IContextFactory>();

        nonCoreFactories.add(new SarosEclipseContextFactory(saros));

        if (isSWTBrowserEnabled()) {
            nonCoreFactories.add(new HTMLUIContextFactory());
            nonCoreFactories.add(new EclipseHTMLUIContextFactory());
        }

        return nonCoreFactories;
    }

    @Override
    protected void initializeContext(final ContainerContext containerContext) {
        FeedbackPreferences.setPreferences(containerContext
            .getComponent(org.osgi.service.prefs.Preferences.class));
    }

    @Override
    protected void finalizeContext(final ContainerContext containerContext) {
        containerContext.getComponent(SarosSessionManager.class).stopSession(
            SessionEndReason.LOCAL_USER_LEFT);
        containerContext.getComponent(ConnectionHandler.class).disconnect();
    }
}
