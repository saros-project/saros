package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.editor.IFollowModeListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

/**
 * This follow mode action is used to select the person to follow.
 *
 * @author Christopher Oezbek
 * @author Edna Rosen
 */
@Component(module = "action")
public class FollowThisPersonAction extends Action implements Disposable {

  public static final String ACTION_ID = FollowThisPersonAction.class.getName();

  private static final Logger LOG = Logger.getLogger(FollowThisPersonAction.class);

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
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateActionEnablement();
        }
      };

  @Inject protected ISarosSessionManager sessionManager;

  private FollowModeManager followModeManager;

  public FollowThisPersonAction() {
    super(Messages.FollowThisPersonAction_follow_title);

    SarosPluginContext.initComponent(this);

    setImageDescriptor(
        new ImageDescriptor() {
          @Override
          public ImageData getImageData() {
            return ImageManager.ICON_USER_SAROS_FOLLOWMODE.getImageData();
          }
        });

    setToolTipText(Messages.FollowThisPersonAction_follow_tooltip);
    setId(ACTION_ID);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    updateEnablement();
  }

  /** @review runSafe OK */
  @Override
  public void run() {
    ThreadUtils.runSafeSync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            List<User> users =
                SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

            if (!canBeExecuted(users)) {
              LOG.warn(
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
        LOG,
        new Runnable() {
          @Override
          public void run() {
            updateEnablement();
          }
        });
  }

  protected void updateEnablement() {

    List<User> users = SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    if (!canBeExecuted(users)) {
      setEnabled(false);
      return;
    }

    if (followModeManager.isFollowing(users.get(0))) {
      setText(Messages.FollowThisPersonAction_stop_follow_title);
      setToolTipText(Messages.FollowThisPersonAction_stop_follow_tooltip);
    } else {
      setText(Messages.FollowThisPersonAction_follow_title);
      setToolTipText(Messages.FollowThisPersonAction_follow_tooltip);
    }

    setEnabled(true);
  }

  protected boolean canBeExecuted(List<User> users) {
    ISarosSession sarosSession = sessionManager.getSession();

    return sarosSession != null
        && followModeManager != null
        && users.size() == 1
        && !users.get(0).isLocal();
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }
}
