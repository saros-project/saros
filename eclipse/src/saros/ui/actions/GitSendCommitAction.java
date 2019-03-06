package saros.ui.actions;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.session.internal.SarosSession;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

public class GitSendCommitAction extends Action implements Disposable {

  public static final String ACTION_ID = GitSendCommitAction.class.getName();

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  public GitSendCommitAction() {
    super(Messages.GitSendCommitAction_title);
    SarosPluginContext.initComponent(this);

    setId(ACTION_ID);
    setToolTipText(Messages.GitSendCommitAction_title);

    setImageDescriptor(ImageManager.getImageDescriptor("icons/elcl16/changecolor.png"));

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    SarosPluginContext.initComponent(this);

    updateEnablement();
  }

  private void updateEnablement() {
    setEnabled(singleParticipantSelected());
  }

  private boolean singleParticipantSelected() {
    List<User> sessionUsers =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    return (sessionUsers.size() == 1 && !sessionUsers.get(0).isLocal());
  }

  @Override
  public void run() {

    SarosSession session = (SarosSession) sessionManager.getSession();
    if (session == null) return;

    session.gitSendCommitRequest();
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
