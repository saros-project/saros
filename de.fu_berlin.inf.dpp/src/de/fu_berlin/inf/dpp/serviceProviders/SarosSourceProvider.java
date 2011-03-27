package de.fu_berlin.inf.dpp.serviceProviders;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.CommandUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * TODO
 * 
 * @author bkahlert
 */
public class SarosSourceProvider extends AbstractSourceProvider {

    /**
     * Corresponds to the serviceProvider variable as defined in Extension
     * org.eclipse.ui.services.
     */
    public static final String SAROS_SESSION = "de.fu_berlin.inf.dpp.SarosSession";

    /**
     * Contains the IDs of commands which change their UI elements on the base
     * of the {@link #SAROS_SESSION} variable.
     */
    public static final String[] SAROS_SESSION_DEPENDENT_COMMANDS = {};

    @Inject
    protected SarosSessionManager sarosSessionManager;

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    protected ISarosSessionListener sarosSessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sessionChanged(newSarosSession);
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            sessionChanged(new NullSarosSession());
        }

    };

    public SarosSourceProvider() {
        SarosPluginContext.initComponent(this);
        sarosSessionManager.addSarosSessionListener(this.sarosSessionListener);
        fireSourceChanged(ISources.WORKBENCH, SAROS_SESSION,
            sarosSessionManager.getSarosSession());
    }

    public void dispose() {
        sarosSessionManager
            .removeSarosSessionListener(this.sarosSessionListener);
    }

    public String[] getProvidedSourceNames() {
        return new String[] { SAROS_SESSION };
    }

    public Map<Object, Object> getCurrentState() {
        ISarosSession sarosSession = sarosSessionManager.getSarosSession();
        if (sarosSession == null)
            sarosSession = new NullSarosSession();

        Map<Object, Object> map = new HashMap<Object, Object>(1);
        map.put(SAROS_SESSION, sarosSession);
        return map;
    }

    private final void sessionChanged(final ISarosSession sarosSession) {
        Utils.runSafeSWTAsync(null, new Runnable() {
            public void run() {
                fireSourceChanged(ISources.WORKBENCH, SAROS_SESSION,
                    sarosSession);
                CommandUtils
                    .refreshUIElements(SAROS_SESSION_DEPENDENT_COMMANDS);
            }
        });
    }

}
