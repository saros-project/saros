package de.fu_berlin.inf.dpp.whiteboard.standalone;

import de.fu_berlin.inf.dpp.whiteboard.gef.actions.ChangeBackgroundColorAction;
import de.fu_berlin.inf.dpp.whiteboard.gef.actions.ChangeForegroundColorAction;
import org.apache.log4j.Logger;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

public class WhiteboardContextMenuProvider extends ContextMenuProvider {

  protected static Logger log = Logger.getLogger(WhiteboardContextMenuProvider.class);
  private ActionRegistry actionRegistry;

  public WhiteboardContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
    super(viewer);
    setActionRegistry(registry);
  }

  @Override
  public void buildContextMenu(IMenuManager menu) {
    IAction action;

    GEFActionConstants.addStandardActionGroups(menu);

    action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

    action = getActionRegistry().getAction(ActionFactory.REDO.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

    action = actionRegistry.getAction(ActionFactory.COPY.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

    action = actionRegistry.getAction(ActionFactory.PASTE.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

    action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

    action = getActionRegistry().getAction(ActionFactory.SELECT_ALL.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

    action = getActionRegistry().getAction(ChangeForegroundColorAction.ACTION_ID);
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

    action = getActionRegistry().getAction(ChangeBackgroundColorAction.ACTION_ID);
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
  }

  private ActionRegistry getActionRegistry() {
    return actionRegistry;
  }

  private void setActionRegistry(ActionRegistry registry) {
    actionRegistry = registry;
  }
}
