package saros.ui.handlers.toolbar;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;

/** Leaves the current Saros session. Is deactivated if there is no running session. */
@Component(module = "action")
public class LeaveSessionHandler {

  public static final String ID = LeaveSessionHandler.class.getName();

  private static final String SAROS_ECLIPSE_URI = "platform:/plugin/saros.eclipse/";

  @Inject private ISarosSessionManager sessionManager;

  private MToolItem leaveSessionToolItem;

  private static final String ELCL_SESSION_TERMINATE =
      SAROS_ECLIPSE_URI + "icons/elcl16/session_terminate_tsk.png";
  private static final String ELCL_SESSION_LEAVE =
      SAROS_ECLIPSE_URI + "icons/elcl16/session_leave_tsk.png";

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          updateEnablement();
        }
      };

  public LeaveSessionHandler() {
    SarosPluginContext.initComponent(this);
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @PostConstruct
  public void construct(EModelService service, MPart sarosView) {
    MUIElement toolBarElement = service.find(ID, sarosView.getToolbar());

    if (toolBarElement instanceof MToolItem) {
      leaveSessionToolItem = (MToolItem) toolBarElement;
    }
    updateEnablement();
  }

  @Execute
  public void run() {
    CollaborationUtils.leaveSession();
  }

  @CanExecute
  public boolean canExecute() {
    ISarosSession session = sessionManager.getSession();
    return session != null;
  }

  @PreDestroy
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  private void updateEnablement() {
    ISarosSession session = sessionManager.getSession();

    if (leaveSessionToolItem == null) {
      return;
    }

    if (session == null) {
      leaveSessionToolItem.setEnabled(false);
      return;
    }

    if (session.isHost()) {
      leaveSessionToolItem.setTooltip(Messages.LeaveSessionAction_stop_session_tooltip);
      leaveSessionToolItem.setIconURI(ELCL_SESSION_TERMINATE);
    } else {
      leaveSessionToolItem.setTooltip(Messages.LeaveSessionAction_leave_session_tooltip);
      leaveSessionToolItem.setIconURI(ELCL_SESSION_LEAVE);
    }
    leaveSessionToolItem.setEnabled(true);
  }
}
