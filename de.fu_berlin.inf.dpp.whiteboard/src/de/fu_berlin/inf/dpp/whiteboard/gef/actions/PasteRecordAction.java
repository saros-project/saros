package de.fu_berlin.inf.dpp.whiteboard.gef.actions;

import de.fu_berlin.inf.dpp.whiteboard.gef.commands.PasteRecordCommand;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Action used to create and executed paste commands that past content of the clipboard to the
 * document.</br>
 *
 * <p>It maintains a counter to inform the command about how many times these contents were inserted
 * already (for position shifting).
 *
 * @author jurke
 */
public class PasteRecordAction extends SelectionAction {
  /** how many times nodes were pasted */
  private int shiftCount = 1;
  /** only used to verify if the clipboard's content has changed */
  private Object contents = null;

  public PasteRecordAction(IWorkbenchPart part) {
    super(part);
    setLazyEnablementCalculation(true);
  }

  @Override
  protected void init() {
    super.init();
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setText("Paste");
    setId(ActionFactory.PASTE.getId());
    setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
    setDisabledImageDescriptor(
        sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
    setEnabled(false);
  }

  private Command createPasteCommand() {
    return new PasteRecordCommand(shiftCount);
  }

  @Override
  protected boolean calculateEnabled() {
    Command command = createPasteCommand();
    return command != null && command.canExecute();
  }

  @Override
  public void run() {
    Command command = createPasteCommand();
    if (command != null && command.canExecute()) {
      execute(command);
      shiftCount++;
    }
  }

  @Override
  protected void handleSelectionChanged() {
    // TODO has to be reset on next copy only!
    if (Clipboard.getDefault().getContents() != contents) shiftCount = 1;
    super.handleSelectionChanged();
    contents = Clipboard.getDefault().getContents();
  }
}
