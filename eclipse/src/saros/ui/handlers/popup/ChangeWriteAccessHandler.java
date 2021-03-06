package saros.ui.handlers.popup;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.User.Permission;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.util.ThreadUtils;

/**
 * Change the write access of a session participant (granting write access, restricting to
 * read-only).
 */
public class ChangeWriteAccessHandler {

  private static final Logger log = Logger.getLogger(ChangeWriteAccessHandler.class);

  public static final class WriteAccess {
    public static final String ID =
        ChangeWriteAccessHandler.class.getName() + "." + WriteAccess.class.getSimpleName();

    private ChangeWriteAccessHandler handler;

    @PostConstruct
    public void postConstruct(
        ESelectionService selectionService, EModelService modelService, MPart sarosView) {

      MPopupMenu popupMenu = null;

      for (MMenu menu : sarosView.getMenus()) {
        if (menu instanceof MPopupMenu) {
          popupMenu = (MPopupMenu) menu;
        }
      }
      MUIElement menuItem = modelService.find(ID, popupMenu);
      if (menuItem instanceof MDirectMenuItem) {
        MDirectMenuItem changeWriteAccessItem = (MDirectMenuItem) menuItem;
        handler =
            new ChangeWriteAccessHandler(
                Permission.WRITE_ACCESS, selectionService, changeWriteAccessItem);
      }
    }

    @Execute
    public void execute() {
      handler.execute();
    }

    @PreDestroy
    public void preDestroy() {
      handler.dispose();
    }

    @CanExecute
    public boolean canExecute() {
      return handler.canExecute();
    }
  }

  public static final class ReadOnly {
    public static final String ID =
        ChangeWriteAccessHandler.class.getName() + "." + ReadOnly.class.getSimpleName();

    private ChangeWriteAccessHandler handler;

    @PostConstruct
    public void postConstruct(
        ESelectionService selectionService, EModelService modelService, MPart sarosView) {

      MPopupMenu popupMenu = null;

      for (MMenu menu : sarosView.getMenus()) {
        if (menu instanceof MPopupMenu) {
          popupMenu = (MPopupMenu) menu;
        }
      }
      MUIElement menuItem = modelService.find(ID, popupMenu);
      if (menuItem instanceof MDirectMenuItem) {
        MDirectMenuItem changeWriteAccessItem = (MDirectMenuItem) menuItem;
        handler =
            new ChangeWriteAccessHandler(
                Permission.READONLY_ACCESS, selectionService, changeWriteAccessItem);
      }
    }

    @Execute
    public void execute() {
      handler.execute();
    }

    @PreDestroy
    public void preDestroy() {
      handler.dispose();
    }

    @CanExecute
    public boolean canExecute() {
      return handler.canExecute();
    }
  }

  private Permission permission;

  @Inject private ISarosSessionManager sessionManager;

  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          newSarosSession.addListener(sessionListener);
          updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          oldSarosSession.removeListener(sessionListener);
        }
      };

  private ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void permissionChanged(User user) {
          updateEnablement();
        }
      };

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  private ESelectionService selectionService;
  private MDirectMenuItem changeWriteAccessItem;

  private ChangeWriteAccessHandler(
      final Permission permission,
      ESelectionService selectionService,
      MDirectMenuItem changeWriteAccessItem) {

    SarosPluginContext.initComponent(this);
    this.permission = permission;
    this.selectionService = selectionService;
    this.changeWriteAccessItem = changeWriteAccessItem;

    /*
     * if SessionView is not "visible" on session start up this constructor
     * will be called after session started (and the user uses this view)
     * That's why the method sessionListener.sessionStarted has to be called
     * manually. If the permissionListener is not added to the session and
     * the action enablement cannot be updated.
     */
    if (sessionManager.getSession() != null) {
      sessionLifecycleListener.sessionStarted(sessionManager.getSession());
    }

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    selectionService.addSelectionListener(selectionListener);
    updateEnablement();
  }

  private boolean getEnabledState() {
    List<User> participants =
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), User.class);

    boolean sessionRunning = (sessionManager.getSession() != null);
    boolean selectedOneWithOppositePermission =
        (participants.size() == 1 && participants.get(0).getPermission() != permission);

    return sessionRunning && selectedOneWithOppositePermission;
  }

  private void updateEnablement() {
    if (changeWriteAccessItem == null) return;

    changeWriteAccessItem.setEnabled(getEnabledState());
  }

  public boolean canExecute() {
    return getEnabledState();
  }

  public void dispose() {
    selectionService.removeSelectionListener(selectionListener);
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  public void execute() {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            ISarosSession session = sessionManager.getSession();

            if (session == null) return;

            List<User> participants =
                SelectionUtils.getAdaptableObjects(
                    (ISelection) selectionService.getSelection(), User.class);
            if (participants.size() == 1) {
              User selected = participants.get(0);
              if (selected.getPermission() != permission) {
                performPermissionChange(session, selected, permission);
                updateEnablement();
              } else {
                log.warn(
                    "Did not change write access of " + selected + ", because it's already set.");
              }
            } else {
              log.warn("More than one participant selected."); // $NON-NLS-1$
            }
          }
        });
  }

  // SWT
  private void performPermissionChange(
      final ISarosSession session, final User user, final Permission newPermission) {

    ProgressMonitorDialog dialog = new ProgressMonitorDialog(SWTUtils.getShell());

    try {
      dialog.run(
          true,
          false,
          new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) {

              try {

                monitor.beginTask(Messages.SarosUI_permission_change, IProgressMonitor.UNKNOWN);

                session.changePermission(user, newPermission);
                /*
                 * FIXME run this at least 2 times and if this still
                 * does not succeed kick the user
                 */
                // } catch (CancellationException e) {
              } catch (InterruptedException e) {
                log.error(e); // cannot happen
              } finally {
                monitor.done();
              }
            }
          });
    } catch (InvocationTargetException e) {
      Throwable t = e.getCause();

      if (t instanceof CancellationException) {
        log.warn("permission change failed, user " + user + " did not respond"); // $NON-NLS-1$
        MessageDialog.openWarning(
            SWTUtils.getShell(),
            Messages.SarosUI_permission_canceled,
            Messages.SarosUI_permission_canceled_text);
      } else {
        log.error("permission change failed", e); // $NON-NLS-1$
        MessageDialog.openError(
            SWTUtils.getShell(),
            Messages.SarosUI_permission_failed,
            Messages.SarosUI_permission_failed_text);
      }
    } catch (InterruptedException e) {
      log.error(e); // cannot happen
    }
  }
}
