package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;

public class SharedProjectLeave extends AbstractHandler {

    @Inject
    protected SarosSessionManager sarosSessionManager;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (sarosSessionManager == null)
            SarosPluginContext.initComponent(this);

        CollaborationUtils.leaveSession(sarosSessionManager);
        return null;
    }

}
