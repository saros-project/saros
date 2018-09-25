package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

public class GettingStartedHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        SWTUtils.openInternalBrowser(
            "http://www.saros-project.org/GettingStarted", "Welcome to Saros");

        return null;
    }

}