package saros.ui.handlers.popup;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import saros.SarosPluginContext;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.User;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.util.CoreUtils;

public class RemoveUserHandler {

  public static final String ID = RemoveUserHandler.class.getName();

  private static final Logger log = Logger.getLogger(RemoveUserHandler.class);

  @Inject private ISarosSessionManager sessionManager;

  private volatile ISarosSession session;

  private ESelectionService selectionService;
  private MDirectMenuItem removeUserItem;

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
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

  public RemoveUserHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      ESelectionService selectionService, MPart sarosView, EModelService modelService) {
    MPopupMenu popupMenu = null;
    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }

    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      removeUserItem = (MDirectMenuItem) menuItem;
    }

    selectionService.addSelectionListener(selectionListener);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    session = sessionManager.getSession();

    updateEnablement();
  }

  @Execute
  public void execute() {

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

              for (User user : users) userNames.add(CoreUtils.determineUserDisplayName(user));

              monitor.beginTask(
                  "Removing user(s): " + StringUtils.join(userNames, ", "),
                  IProgressMonitor.UNKNOWN);

              for (User user : users) session.kickUser(user);

              monitor.done();
            }
          });
    } catch (Exception e) {
      log.error("internal error while removing users", e); // $NON-NLS-1$
    }
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);

    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  private void updateEnablement() {
    ISarosSession currentSession = session;

    List<User> users =
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), User.class);

    removeUserItem.setEnabled(
        currentSession != null && currentSession.isHost() && canRemoveUsers(users));
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
