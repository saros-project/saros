package saros.ui.actions;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.util.ModelFormatUtils;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

public class RemoveUserAction extends Action {

  public static final String ACTION_ID = RemoveUserAction.class.getName();

  private static final Logger LOG = Logger.getLogger(RemoveUserAction.class);

  @Inject private ISarosSessionManager sessionManager;

  private volatile ISarosSession session;

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          session = newSarosSession;
          SWTUtils.runSafeSWTAsync(
              null,
              new Runnable() {
                @Override
                public void run() {
                  updateEnablement();
                }
              });
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
          session = null;
          SWTUtils.runSafeSWTAsync(
              null,
              new Runnable() {
                @Override
                public void run() {
                  updateEnablement();
                }
              });
        }
      };

  public RemoveUserAction() {
    super("Remove from Session");
    SarosPluginContext.initComponent(this);

    setId(ACTION_ID);
    setImageDescriptor(ImageManager.getImageDescriptor("icons/elcl16/contact_remove_tsk.png"));

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    session = sessionManager.getSession();

    updateEnablement();
  }

  @Override
  public void run() {

    final ISarosSession currentSession = session;

    if (currentSession == null) return;

    final List<User> users =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    if (!canRemoveUsers(users)) return;

    Shell shell = SWTUtils.getShell();

    if (shell == null) shell = new Shell(SWTUtils.getDisplay());

    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

    try {
      dialog.run(
          true,
          false,
          new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InterruptedException {

              List<String> userNames = new ArrayList<String>();

              for (User user : users) userNames.add(ModelFormatUtils.getDisplayName(user));

              monitor.beginTask(
                  "Removing user(s): " + StringUtils.join(userNames, ", "),
                  IProgressMonitor.UNKNOWN);

              for (User user : users) session.kickUser(user);

              monitor.done();
            }
          });
    } catch (Exception e) {
      LOG.error("internal error while removing users", e); // $NON-NLS-1$
    }
  }

  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);

    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  private void updateEnablement() {
    ISarosSession currentSession = session;

    List<User> users = SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    setEnabled(currentSession != null && currentSession.isHost() && canRemoveUsers(users));
  }

  private boolean canRemoveUsers(List<User> users) {

    if (users.size() == 0) return false;

    for (User user : users) {
      if (user.isHost()) {
        return false;
      }
    }
    return true;
  }
}
