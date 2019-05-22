package saros.intellij.ui.views.buttons;

import com.intellij.util.ui.UIUtil;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import saros.SarosPluginContext;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.intellij.ui.actions.FollowModeAction;
import saros.intellij.ui.util.IconManager;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.util.ModelFormatUtils;

/** Button to follow a user. Displays a PopupMenu containing all session users to choose from. */
public class FollowButton extends ToolbarButton {
  private JPopupMenu popupMenu;
  private final FollowModeAction followModeAction;

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(final User user) {
          updateMenu();
        }

        @Override
        public void userJoined(final User user) {
          updateMenu();
        }
      };

  private final IFollowModeListener followModeListener =
      new IFollowModeListener() {
        @Override
        public void stoppedFollowing(Reason reason) {
          updateMenu();
        }

        @Override
        public void startedFollowing(User target) {
          updateMenu();
        }
      };

  @SuppressWarnings("FieldCanBeLocal")
  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
          FollowButton.this.session = session;
          FollowButton.this.session.addListener(sessionListener);

          followModeManager = session.getComponent(FollowModeManager.class);
          followModeManager.addListener(followModeListener);

          updateMenu();
          setEnabledFromUIThread(true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          FollowButton.this.session.removeListener(sessionListener);
          FollowButton.this.session = null;

          followModeManager.removeListener(followModeListener);
          followModeManager = null;

          updateMenu();
          setEnabledFromUIThread(false);
        }
      };

  private String menuItemPrefix;

  @Inject private ISarosSessionManager sessionManager;

  private volatile ISarosSession session;
  private volatile FollowModeManager followModeManager;

  /**
   * Creates a Follow button with a JPopupMenu, registers session listeners and editor listeners.
   *
   * <p>The FollowButton is created as disabled.
   */
  public FollowButton() {
    super(FollowModeAction.NAME, "Follow", IconManager.FOLLOW_ICON);
    SarosPluginContext.initComponent(this);

    followModeAction = new FollowModeAction();

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);

    createMenu();
    setEnabled(false);

    final JButton button = this;
    addActionListener(
        ev -> popupMenu.show(button, 0, button.getBounds().y + button.getBounds().height));
  }

  private void createMenu() {
    popupMenu = new JPopupMenu();

    menuItemPrefix = "Follow ";

    ISarosSession currentSession = session;
    FollowModeManager currentFollowModeManager = followModeManager;

    if (currentSession == null || currentFollowModeManager == null) {
      return;
    }

    for (User user : currentSession.getRemoteUsers()) {
      JMenuItem menuItem = createItemForUser(user);
      popupMenu.add(menuItem);
    }

    popupMenu.addSeparator();

    JMenuItem leaveItem = new JMenuItem("Leave follow mode");
    leaveItem.addActionListener(e -> followModeAction.execute(null));
    leaveItem.setEnabled(currentFollowModeManager.getFollowedUser() != null);

    popupMenu.add(leaveItem);
  }

  private JMenuItem createItemForUser(User user) {
    String userName = ModelFormatUtils.getDisplayName(user);
    String userNameShort = userName;
    int index = userNameShort.indexOf("@");
    if (index > -1) {
      userNameShort = userNameShort.substring(0, index);
    }

    JMenuItem menuItem = new JMenuItem(menuItemPrefix + userNameShort);

    FollowModeManager currentFollowModeManager = followModeManager;

    User currentlyFollowedUser = null;

    if (currentFollowModeManager != null) {
      currentlyFollowedUser = currentFollowModeManager.getFollowedUser();
    }

    if (currentlyFollowedUser != null) {
      String currentUserName = ModelFormatUtils.getDisplayName(currentlyFollowedUser);
      if (currentUserName.equalsIgnoreCase(userNameShort)) {
        menuItem.setEnabled(false);
      }
    }

    menuItem.setActionCommand(userName);
    menuItem.addActionListener(e -> followModeAction.execute(e.getActionCommand()));
    return menuItem;
  }

  private void updateMenu() {
    UIUtil.invokeAndWaitIfNeeded((Runnable) this::createMenu);
  }
}
