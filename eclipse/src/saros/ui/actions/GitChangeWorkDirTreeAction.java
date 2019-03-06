package saros.ui.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.session.internal.SarosSession;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

public class GitChangeWorkDirTreeAction extends Action implements Disposable {

  public static final String ACTION_ID = GitChangeWorkDirTreeAction.class.getName();

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  public GitChangeWorkDirTreeAction() {
    super(Messages.GitChangeWorkDirTreeAction_title);
    SarosPluginContext.initComponent(this);

    setId(ACTION_ID);
    setToolTipText(Messages.GitChangeWorkDirTreeAction_title);

    setImageDescriptor(ImageManager.getImageDescriptor("icons/elcl16/changecolor.png"));

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    updateEnablement();
  }

  public void updateEnablement() {
    List<User> participants =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    ISarosSession session = sessionManager.getSession();

    setEnabled(
        session != null
            && participants.size() == 1
            && participants.get(0).equals(session.getLocalUser()));
  }

  @Override
  public void run() {

    SarosSession session = (SarosSession) sessionManager.getSession();
    if (session == null) return;

    final DirectoryDialog dg = new DirectoryDialog(SWTUtils.getShell(), SWT.OPEN);
    dg.setText(Messages.GitChangeWorkDirTreeAction_directorydialog_text);

    final String path = dg.open();

    if (path == null) return;

    final File directory = new File(path);

    try {
      session.gitChangeWorkDirTree(directory);
    } catch (IOException e) {
    }
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
