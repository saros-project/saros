package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.util.ui.UIUtil;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jetbrains.annotations.NotNull;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.intellij.ui.Messages;
import saros.intellij.ui.actions.FollowModeAction;
import saros.intellij.ui.util.IconManager;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.util.ModelFormatUtils;

/** Button to follow a user. Displays a PopupMenu containing all session users to choose from. */
public class FollowButton extends AbstractSessionToolbarButton {
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

  private String menuItemPrefix;

  private volatile ISarosSession session;
  private volatile FollowModeManager followModeManager;

  /**
   * Creates a Follow button with a JPopupMenu, registers session listeners and editor listeners.
   *
   * <p>The FollowButton is created as disabled.
   */
  public FollowButton(@NotNull Project project) {
    super(project, FollowModeAction.NAME, Messages.FollowButton_tooltip, IconManager.FOLLOW_ICON);

    followModeAction = new FollowModeAction();

    createMenu();
    setEnabled(false);

    final JButton button = this;
    addActionListener(
        ev -> popupMenu.show(button, 0, button.getBounds().y + button.getBounds().height));

    setInitialState();
  }

  @Override
  void disposeComponents() {
    ISarosSession currentSession = session;
    if (currentSession != null) {
      currentSession.removeListener(sessionListener);
    }

    FollowModeManager currentFollowModeManager = followModeManager;
    if (currentFollowModeManager != null) {
      currentFollowModeManager.removeListener(followModeListener);
    }
  }

  @Override
  void sessionStarted(final ISarosSession session) {
    FollowButton.this.session = session;
    FollowButton.this.session.addListener(sessionListener);

    followModeManager = session.getComponent(FollowModeManager.class);
    followModeManager.addListener(followModeListener);

    updateMenu();
    setEnabledFromUIThread(true);
  }

  @Override
  void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
    FollowButton.this.session.removeListener(sessionListener);
    FollowButton.this.session = null;

    followModeManager.removeListener(followModeListener);
    followModeManager = null;

    updateMenu();
    setEnabledFromUIThread(false);
  }

  private void createMenu() {
    popupMenu = new JBPopupMenu();

    popupMenu.setForeground(FOREGROUND_COLOR);
    popupMenu.setBackground(BACKGROUND_COLOR);

    menuItemPrefix = Messages.FollowButton_user_entry_prefix;

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

    JMenuItem leaveItem = new JBMenuItem(Messages.FollowButton_leave_follow_mode_entry);

    leaveItem.setForeground(FOREGROUND_COLOR);
    leaveItem.setBackground(BACKGROUND_COLOR);

    leaveItem.addActionListener(e -> followModeAction.execute(session, followModeManager, null));
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

    JMenuItem menuItem = new JBMenuItem(menuItemPrefix + " " + userNameShort);

    menuItem.setForeground(FOREGROUND_COLOR);
    menuItem.setBackground(BACKGROUND_COLOR);

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
    menuItem.addActionListener(
        e -> followModeAction.execute(session, followModeManager, e.getActionCommand()));

    return menuItem;
  }

  private void updateMenu() {
    UIUtil.invokeAndWaitIfNeeded((Runnable) this::createMenu);
  }
}
