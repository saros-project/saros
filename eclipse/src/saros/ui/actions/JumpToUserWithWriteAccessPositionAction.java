package saros.ui.actions;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISelectionListener;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.editor.EditorManager;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

@Component(module = "action")
public class JumpToUserWithWriteAccessPositionAction extends Action implements Disposable {

  public static final String ACTION_ID = JumpToUserWithWriteAccessPositionAction.class.getName();

  @Inject private ISarosSessionManager sessionManager;

  @Inject private EditorManager editorManager;

  private final ISelectionListener selectionListener = (part, selection) -> updateEnablement();

  public JumpToUserWithWriteAccessPositionAction() {
    super(Messages.JumpToUserWithWriteAccessPositionAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.JumpToUserWithWriteAccessPositionAction_tooltip);
    setImageDescriptor(ImageManager.getImageDescriptor("icons/elcl16/jump.png")); // $NON-NLS-1$

    SarosPluginContext.initComponent(this);

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  private void updateEnablement() {
    setEnabled(getTarget() != null);
  }

  @Override
  public void run() {

    final User target = getTarget();

    if (target == null) return;

    editorManager.jumpToUser(target);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }

  private User getTarget() {
    final List<User> users =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    if (users.size() == 1 && !users.get(0).isLocal() && sessionManager.getSession() != null)
      return users.get(0);

    return null;
  }
}
