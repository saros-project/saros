package saros.whiteboard.gef.actions;

import java.util.Iterator;
import java.util.List;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import saros.whiteboard.gef.commands.CopyRecordCommand;
import saros.whiteboard.gef.editor.WhiteboardEditor;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.view.SarosWhiteboardView;

/**
 * Creates and executes a copy command that stores deep copies of the current selection to the
 * clipboard.
 *
 * <p>We provide copies to the clipboard because else the copy would not be a snapshot of the
 * current state but of the state when the copies are created.
 *
 * <p>This action uses a hierarchical list so that only the top-most records are added to the list
 * of ElementRecords that will be put to the Clipboard.
 *
 * @author jurke
 */
public class CopyRecordAction extends SelectionAction {
  public CopyRecordAction(IWorkbenchPart part) {
    super(part);
    // force calculateEnabled() to be called in every context
    setLazyEnablementCalculation(true);
  }

  @Override
  protected void init() {
    super.init();
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setText("Copy");
    setId(ActionFactory.COPY.getId());
    setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    setDisabledImageDescriptor(
        sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
    setEnabled(false);
  }

  @SuppressWarnings("rawtypes")
  private Command createCopyCommand(List selectedObjects) {
    if (selectedObjects == null || selectedObjects.isEmpty()) {
      return null;
    }

    CopyRecordCommand cmd = new CopyRecordCommand();
    Object o;
    Iterator it = selectedObjects.iterator();
    while (it.hasNext()) {
      o = it.next();
      if (!(o instanceof EditPart)) continue;
      EditPart ep = (EditPart) o;
      LayoutElementRecord node = (LayoutElementRecord) ep.getModel();
      if (!cmd.isCopyableNode(node)) // TODO only copy others?
      return null;
      cmd.addElement(node);
    }
    return cmd;
  }

  @Override
  protected boolean calculateEnabled() {
    Command cmd = createCopyCommand(getSelectedObjects());
    if (cmd == null) return false;
    return cmd.canExecute();
  }

  @Override
  public void run() {
    Command cmd = createCopyCommand(getSelectedObjects());
    if (cmd != null && cmd.canExecute()) {
      cmd.execute();
      // force an updateActions for paste command
      if (getWorkbenchPart().getSite().getPage().getActivePart() instanceof SarosWhiteboardView) {
        ((SarosWhiteboardView) getWorkbenchPart().getSite().getPage().getActivePart())
            .getEditor()
            .updateSelectionActions();
      } else if (getWorkbenchPart().getSite().getPage().getActivePart()
          instanceof WhiteboardEditor) {
        ((WhiteboardEditor) getWorkbenchPart().getSite().getPage().getActivePart())
            .updateSelectionActions();
      }
    }
  }
}
