package saros.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;

/**
 * Leaves the current Saros session. Is deactivated if there is no running session.
 *
 * @author rdjemili
 * @author oezbek
 */
@Component(module = "action")
public class LeaveSessionAction extends Action implements Disposable {

  public static final String ACTION_ID = LeaveSessionAction.class.getName();

  @Inject private ISarosSessionManager sessionManager;

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

  public LeaveSessionAction() {
    setId(ACTION_ID);
    setToolTipText(Messages.LeaveSessionAction_leave_session_tooltip);
    setImageDescriptor(
        new ImageDescriptor() {
          @Override
          public ImageData getImageData() {
            return ImageManager.ELCL_SESSION_LEAVE.getImageData();
          }
        });

    SarosPluginContext.initComponent(this);
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    updateEnablement();
  }

  @Override
  public void run() {
    CollaborationUtils.leaveSession();
  }

  @Override
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  private void updateEnablement() {
    ISarosSession session = sessionManager.getSession();

    if (session == null) {
      setEnabled(false);
      return;
    }

    if (session.isHost()) {
      setToolTipText(Messages.LeaveSessionAction_stop_session_tooltip);
      setImageDescriptor(
          new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
              return ImageManager.ELCL_SESSION_TERMINATE.getImageData();
            }
          });
    } else {
      setToolTipText(Messages.LeaveSessionAction_leave_session_tooltip);
      setImageDescriptor(
          new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
              return ImageManager.ELCL_SESSION_LEAVE.getImageData();
            }
          });
    }
    setEnabled(true);
  }
}
