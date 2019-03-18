package saros.ui.actions;

import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

@Component(module = "action")
public class GitChangeWorkDirTreeAction extends Action implements Disposable {

  public static final String ACTION_ID = GitChangeWorkDirTreeAction.class.getName();

  private static final Logger LOG = Logger.getLogger(GitChangeWorkDirTreeAction.class);

  protected ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateActionEnablement();
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  // private saros.git.GitManager gitManager;

  public GitChangeWorkDirTreeAction() {
    super(Messages.GitChangeWorkDirTreeAction_title);
    SarosPluginContext.initComponent(this);

    setId(ACTION_ID);
    setToolTipText(Messages.GitChangeWorkDirTreeAction_title);

    setImageDescriptor(ImageManager.getImageDescriptor("icons/elcl16/changecolor.png"));

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    updateActionEnablement();
  }

  protected void updateActionEnablement() {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            updateEnablement();
          }
        });
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

    ISarosSession session = sessionManager.getSession();
    if (session == null) return;

    final DirectoryDialog dg = new DirectoryDialog(SWTUtils.getShell(), SWT.OPEN);
    dg.setText(Messages.GitChangeWorkDirTreeAction_directorydialog_text);

    final String path = dg.open();

    if (path == null) return;

    final File directory = new File(path);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }

  protected ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          // gitManager = newSarosSession.getComponent(GitManager.class);

          updateActionEnablement();
        }
      };
}
