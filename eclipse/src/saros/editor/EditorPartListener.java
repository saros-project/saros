package saros.editor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * The <code>EditorPartListener</code> listens to changes to the editors view, e.g. if the view is
 * opened, activated, closed, brought to the top or the source has changed.
 *
 * <p>The Listener notifies the EditorManager about the state of the view, so the Manager can react
 * on it, e.g. to update followers.
 *
 * @see EditorManager#partInputChanged(IEditorPart)
 * @author awaldmann
 * @author nwarnatsch
 */
public final class EditorPartListener implements IPartListener2 {

  private final EditorManager editorManager;

  public EditorPartListener(EditorManager editorManager) {
    this.editorManager = editorManager;
  }

  @Override
  public void partActivated(IWorkbenchPartReference partRef) {
    final IWorkbenchPart part = partRef.getPart(false);

    if (part instanceof IEditorPart) editorManager.partActivated((IEditorPart) part);
  }

  @Override
  public void partOpened(IWorkbenchPartReference partRef) {
    final IWorkbenchPart part = partRef.getPart(false);

    if (part instanceof IEditorPart) editorManager.partOpened((IEditorPart) part);
  }

  @Override
  public void partClosed(IWorkbenchPartReference partRef) {
    final IWorkbenchPart part = partRef.getPart(false);

    if (part instanceof IEditorPart) editorManager.partClosed((IEditorPart) part);
  }

  /*
   * We need to catch partBroughtToTop events because partActivate events are
   * missing if Editors are opened programmatically.
   */
  @Override
  public void partBroughtToTop(IWorkbenchPartReference partRef) {
    final IWorkbenchPart part = partRef.getPart(false);

    if (part instanceof IEditorPart) editorManager.partActivated((IEditorPart) part);
  }

  @Override
  public void partDeactivated(IWorkbenchPartReference partRef) {
    // do nothing
  }

  @Override
  public void partHidden(IWorkbenchPartReference partRef) {
    // do nothing
  }

  @Override
  public void partVisible(IWorkbenchPartReference partRef) {
    // do nothing
  }

  @Override
  public void partInputChanged(IWorkbenchPartReference partRef) {
    final IWorkbenchPart part = partRef.getPart(false);

    if (part instanceof IEditorPart) editorManager.partInputChanged((IEditorPart) part);
  }
}
