package saros.ui.handlers.popup;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.util.ThreadUtils;

/** This follow mode action is used to select the person to follow. */
@Component(module = "action")
public class FollowThisPersonHandler {

  public static final String ID = FollowThisPersonHandler.class.getName();

  private static final Logger log = Logger.getLogger(FollowThisPersonHandler.class);

  protected ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {

          followModeManager = newSarosSession.getComponent(FollowModeManager.class);
          followModeManager.addListener(followModeListener);

          updateActionEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          followModeManager.removeListener(followModeListener);
          followModeManager = null;

          updateActionEnablement();
        }
      };

  private IFollowModeListener followModeListener =
      new IFollowModeListener() {

        @Override
        public void stoppedFollowing(Reason reason) {
          updateActionEnablement();
        }

        @Override
        public void startedFollowing(User target) {
          updateActionEnablement();
        }
      };

  protected ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateActionEnablement();
        }
      };

  @Inject protected ISarosSessionManager sessionManager;

  private FollowModeManager followModeManager;

  private ESelectionService selectionService;
  private MDirectMenuItem followThisPersonMenuItem;

  public FollowThisPersonHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      ESelectionService selectionService, MPart sarosView, EModelService modelService) {
    this.selectionService = selectionService;

    MPopupMenu popupMenu = null;

    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }
    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      followThisPersonMenuItem = (MDirectMenuItem) menuItem;
    }

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    selectionService.addSelectionListener(selectionListener);

    updateEnablement();
  }

  /** @review runSafe OK */
  @Execute
  public void execute() {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            List<User> users =
                SelectionUtils.getAdaptableObjects(
                    (ISelection) selectionService.getSelection(), User.class);

            if (!canBeExecuted(users)) {
              log.warn(
                  "could not execute change follow mode action " //$NON-NLS-1$
                      + "because either no session is running, " //$NON-NLS-1$
                      + "more than one user is selected or " //$NON-NLS-1$
                      + "the selected user is the local user"); //$NON-NLS-1$
              return;
            }

            User toFollow = followModeManager.isFollowing(users.get(0)) ? null : users.get(0);

            followModeManager.follow(toFollow);
          }
        });
  }

  protected void updateActionEnablement() {
    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {
          @Override
          public void run() {
            updateEnablement();
          }
        });
  }

  protected void updateEnablement() {

    List<User> users =
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), User.class);

    if (!canBeExecuted(users)) {
      followThisPersonMenuItem.setEnabled(false);
      return;
    }

    if (followModeManager.isFollowing(users.get(0))) {
      followThisPersonMenuItem.setLabel(Messages.FollowThisPersonAction_stop_follow_title);
      followThisPersonMenuItem.setTooltip(Messages.FollowThisPersonAction_stop_follow_tooltip);
    } else {
      followThisPersonMenuItem.setLabel(Messages.FollowThisPersonAction_follow_title);
      followThisPersonMenuItem.setTooltip(Messages.FollowThisPersonAction_follow_tooltip);
    }

    followThisPersonMenuItem.setEnabled(true);
  }

  protected boolean canBeExecuted(List<User> users) {
    ISarosSession sarosSession = sessionManager.getSession();

    return sarosSession != null
        && followModeManager != null
        && users.size() == 1
        && !users.get(0).isLocal();
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }
}
