package saros.whiteboard.standalone;

import org.apache.log4j.Logger;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import saros.ui.actions.ChangeColorAction;
import saros.whiteboard.gef.actions.ChangeBackgroundColorAction;
import saros.whiteboard.gef.actions.ChangeForegroundColorAction;

public class WhiteboardActionBarContributor extends ActionBarContributor {

  private static final Logger log = Logger.getLogger(ChangeColorAction.class);

  @Override
  protected void buildActions() {
    IWorkbenchWindow iww = getPage().getWorkbenchWindow();

    addRetargetAction(new UndoRetargetAction());
    addRetargetAction(new RedoRetargetAction());
    addRetargetAction(new DeleteRetargetAction());

    addAction(new ChangeBackgroundColorAction());
    addAction(new ChangeForegroundColorAction());
    addRetargetAction((RetargetAction) ActionFactory.SELECT_ALL.create(iww));

    addRetargetAction((RetargetAction) ActionFactory.COPY.create(iww));
    addRetargetAction((RetargetAction) ActionFactory.PASTE.create(iww));

    addRetargetAction(new ZoomInRetargetAction());
    addRetargetAction(new ZoomOutRetargetAction());
  }

  @Override
  protected void declareGlobalActionKeys() {}

  @Override
  public void contributeToToolBar(final IToolBarManager toolBarManager) {
    toolBarManager.add(getAction(ActionFactory.UNDO.getId()));
    toolBarManager.add(getAction(ActionFactory.REDO.getId()));
    toolBarManager.add(new Separator());
    toolBarManager.add(getAction(ActionFactory.COPY.getId()));
    toolBarManager.add(getAction(ActionFactory.PASTE.getId()));
    toolBarManager.add(getAction(ActionFactory.SELECT_ALL.getId()));
    toolBarManager.add(getAction(ActionFactory.DELETE.getId()));
    toolBarManager.add(new Separator());
    toolBarManager.add(getAction(ChangeForegroundColorAction.ACTION_ID));
    toolBarManager.add(getAction(ChangeBackgroundColorAction.ACTION_ID));

    toolBarManager.add(new Separator());
    toolBarManager.add(getAction(GEFActionConstants.ZOOM_IN));
    toolBarManager.add(getAction(GEFActionConstants.ZOOM_OUT));
    toolBarManager.add(new ZoomComboContributionItem(getPage()));
  }

  @Override
  public void contributeToMenu(IMenuManager menuManager) {}
}
