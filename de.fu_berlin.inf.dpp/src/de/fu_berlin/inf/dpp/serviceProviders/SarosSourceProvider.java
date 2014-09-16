package de.fu_berlin.inf.dpp.serviceProviders;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.communication.connection.IConnectionStateListener;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * Adds variables to Eclipse's Core {@link Expression}s in order to be used in
 * plugin.xml.
 * 
 * @author bkahlert
 */
public class SarosSourceProvider extends AbstractSourceProvider {

    /**
     * Corresponds to a serviceProvider variable as defined in Extension
     * org.eclipse.ui.services.
     */
    public static final String SAROS = "de.fu_berlin.inf.dpp.Saros";

    /**
     * Corresponds to a serviceProvider variable as defined in Extension
     * org.eclipse.ui.services.
     */
    public static final String SAROS_SESSION = "de.fu_berlin.inf.dpp.SarosSession";

    /**
     * Contains the IDs of commands which change their UI elements on the base
     * of the {@link #SAROS} variable.
     */
    public static final String[] SAROS_DEPENDENT_COMMANDS = {};

    /**
     * Contains the IDs of commands which change their UI elements on the base
     * of the {@link #SAROS_SESSION} variable.
     */
    public static final String[] SAROS_SESSION_DEPENDENT_COMMANDS = {};

    @Inject
    protected Saros saros;

    @Inject
    protected ISarosSessionManager sarosSessionManager;

    @Inject
    private ConnectionHandler connectionHandler;

    protected IConnectionStateListener connectionStateListener = new IConnectionStateListener() {
        @Override
        public void connectionStateChanged(final ConnectionState state,
            final Exception error) {
            connectionChanged();
        }
    };

    protected ISarosSessionListener sarosSessionListener = new NullSarosSessionListener() {

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
        connectionHandler.addConnectionStateListener(connectionStateListener);

        sarosSessionManager.addSarosSessionListener(sarosSessionListener);

        fireSourceChanged(ISources.WORKBENCH, SAROS, saros);
        fireSourceChanged(ISources.WORKBENCH, SAROS_SESSION,
            sarosSessionManager.getSarosSession());
    }

    @Override
    public void dispose() {
        sarosSessionManager.removeSarosSessionListener(sarosSessionListener);
        connectionHandler
            .removeConnectionStateListener(connectionStateListener);
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { SAROS, SAROS_SESSION };
    }

    @Override
    public Map<Object, Object> getCurrentState() {
        ISarosSession sarosSession = this.sarosSessionManager.getSarosSession();
        if (sarosSession == null)
            sarosSession = new NullSarosSession();

        Map<Object, Object> map = new HashMap<Object, Object>(2);
        map.put(SAROS, this.saros);
        map.put(SAROS_SESSION, sarosSession);
        return map;
    }

    private final void connectionChanged() {
        SWTUtils.runSafeSWTAsync(null, new Runnable() {
            @Override
            public void run() {
                fireSourceChanged(ISources.WORKBENCH, SAROS, saros);
                refreshUIElements(SAROS_DEPENDENT_COMMANDS);
            }
        });
    }

    private final void sessionChanged(final ISarosSession sarosSession) {
        SWTUtils.runSafeSWTAsync(null, new Runnable() {
            @Override
            public void run() {
                fireSourceChanged(ISources.WORKBENCH, SAROS_SESSION,
                    sarosSession);

                refreshUIElements(SAROS_SESSION_DEPENDENT_COMMANDS);
            }
        });
    }

    /**
     * Refreshes all UI elements that display the given {@link Command}s.
     * 
     * @param commandIDs
     */
    private void refreshUIElements(final String[] commandIDs) {
        ICommandService commandService = (ICommandService) PlatformUI
            .getWorkbench().getActiveWorkbenchWindow()
            .getService(ICommandService.class);

        if (commandService == null)
            return;

        for (String commandID : commandIDs)
            commandService.refreshElements(commandID, null);
    }
}
