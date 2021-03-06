package saros.ui.handlers.toolbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import saros.SarosPluginContext;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.util.CoreUtils;

/** Action to enter into FollowMode via toolbar. */
public class FollowModeHandler {

  public static final String ID = FollowModeHandler.class.getName();

  static class FollowUserHandler {

    @Execute
    public void execute(MDirectMenuItem item) {
      Map<String, Object> itemData = item.getTransientData();
      Object user = itemData.get("user");

      if (user instanceof User || user == null) {
        followUser((User) user);
      }
    }
  }

  private static final Logger log = Logger.getLogger(FollowModeHandler.class);

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  private ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(final User user) {
          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {

                @Override
                public void run() {
                  currentRemoteSessionUsers.remove(user);

                  if (user.equals(currentlyFollowedUser)) {
                    currentlyFollowedUser = null;
                    updateEnablement();
                  }
                }
              });
        }

        @Override
        public void userJoined(final User user) {
          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {

                @Override
                public void run() {
                  currentRemoteSessionUsers.add(user);
                  updateEnablement();
                }
              });
        }
      };

  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {

          session.addListener(sessionListener);

          followModeManager = session.getComponent(FollowModeManager.class);
          followModeManager.addListener(followModeListener);

          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {

                @Override
                public void run() {
                  currentlyFollowedUser = followModeManager.getFollowedUser();

                  FollowModeHandler.this.session = session;
                  currentRemoteSessionUsers.clear();
                  currentRemoteSessionUsers.addAll(session.getRemoteUsers());
                  updateEnablement();
                }
              });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          oldSarosSession.removeListener(sessionListener);

          followModeManager.removeListener(followModeListener);
          followModeManager = null;

          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {

                @Override
                public void run() {
                  FollowModeHandler.this.session = null;
                  currentRemoteSessionUsers.clear();
                  updateEnablement();
                }
              });
        }
      };

  private IFollowModeListener followModeListener =
      new IFollowModeListener() {

        @Override
        public void stoppedFollowing(Reason reason) {
          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {
                @Override
                public void run() {
                  currentlyFollowedUser = null;
                  updateEnablement();
                }
              });
        }

        @Override
        public void startedFollowing(final User target) {
          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {
                @Override
                public void run() {
                  currentlyFollowedUser = target;
                  updateEnablement();
                }
              });
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  private static FollowModeManager followModeManager;

  private ISarosSession session;

  private User currentlyFollowedUser;

  private final Set<User> currentRemoteSessionUsers = new LinkedHashSet<User>();

  private final String followModeEnabledImageURI =
      "platform:/plugin/saros.eclipse/icons/merged16/user_saros_followmode_enabled.png";

  private final String followModeDisabledImageURI =
      "platform:/plugin/saros.eclipse/icons/merged16/user_saros_followmode_disabled.png";

  public FollowModeHandler() {

    SarosPluginContext.initComponent(this);

    session = sessionManager.getSession();

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  private MDirectToolItem followModeItem;

  @PostConstruct
  public void postConstruct(
      EModelService service, MPart sarosView, ESelectionService selectionService) {
    MUIElement toolbarElement = service.find(ID, sarosView.getToolbar());

    if (toolbarElement instanceof MDirectToolItem) {
      followModeItem = (MDirectToolItem) toolbarElement;
    }

    selectionService.addSelectionListener(selectionListener);

    if (session != null) currentRemoteSessionUsers.addAll(session.getRemoteUsers());

    updateEnablement();
  }

  @Execute
  public void run(MDirectToolItem toolBarElement) {
    if (session == null) return;

    if (!toolBarElement.isEnabled()) return;

    followUser(getNextUserToFollow());
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
    selectionService.removeSelectionListener(selectionListener);
  }

  @AboutToShow
  public void createMenu(List<MMenuElement> items, EModelService service) {
    if (session == null) return;

    List<User> users = new ArrayList<User>(currentRemoteSessionUsers);

    for (User user : users) {
      // The additional @ is needed because @ has special meaning in
      // Action#setText(), see JavaDoc of Action().

      String followUserMessage = getFollowUserMessage(user);

      if (followUserMessage.contains("@")) followUserMessage += "@";

      // Create menu item
      MDirectMenuItem userMenuItem = service.createModelElement(MDirectMenuItem.class);
      userMenuItem.setLabel(followUserMessage);
      userMenuItem.setContributionURI(
          "bundleclass://saros.eclipse/saros.ui.e4.toolbar.FollowModeHandler$FollowUserHandler");
      userMenuItem.setIconURI(followModeEnabledImageURI);

      items.add(userMenuItem);
    }
  }

  private static void followUser(User user) {
    if (followModeManager != null) followModeManager.follow(user);
  }

  /** Returns the next user to follow or <code>null</code> if follow mode should be disabled. */
  private User getNextUserToFollow() {
    if (currentRemoteSessionUsers.isEmpty()) return null;

    if (currentlyFollowedUser == null) return currentRemoteSessionUsers.iterator().next();

    User nextUser = null;

    for (Iterator<User> it = currentRemoteSessionUsers.iterator(); it.hasNext(); ) {
      User user = it.next();
      if (user.equals(currentlyFollowedUser)) {
        if (it.hasNext()) nextUser = it.next();

        break;
      }
    }

    return nextUser;
  }

  @CanExecute
  public boolean canExecute() {
    return session != null && !currentRemoteSessionUsers.isEmpty();
  }

  private void updateEnablement() {
    if (followModeItem == null) {
      return;
    }

    followModeItem.setEnabled(session != null && !currentRemoteSessionUsers.isEmpty());

    if (currentlyFollowedUser != null) {
      followModeItem.setIconURI(followModeEnabledImageURI);
    } else {
      followModeItem.setIconURI(followModeDisabledImageURI);
    }

    if (followModeItem.isEnabled()) {
      User nextUserToFollow = getNextUserToFollow();
      followModeItem.setTooltip(
          nextUserToFollow == null
              ? Messages.FollowModeAction_leave_followmode
              : getFollowUserMessage(nextUserToFollow));
    } else
      // display default text
      followModeItem.setTooltip(null);
  }

  private String getFollowUserMessage(User user) {
    return CoreUtils.format(Messages.FollowModeAction_follow_user, user);
  }
}
