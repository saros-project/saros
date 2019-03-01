package de.fu_berlin.inf.dpp.ui.commandHandlers;

import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SessionLeaveHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    CollaborationUtils.leaveSession();
    return null;
  }
}
