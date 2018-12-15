package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.picocontainer.annotations.Inject;

/** Button to follow a user. Displays a PopupMenu containing all session users to choose from. */
public class FollowButton extends ToolbarButton {
  private static final String FOLLOW_ICON_PATH = "/icons/famfamfam/followmode.png";
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

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
          session.addListener(sessionListener);
          updateMenu();
          setEnabledFromUIThread(true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          oldSarosSession.removeListener(sessionListener);
          updateMenu();
          setEnabledFromUIThread(false);
        }
      };

  private final ISharedEditorListener editorListener =
      new ISharedEditorListener() {
        @Override
        public void followModeChanged(final User target, final boolean isFollowed) {
          updateMenu();
        }
      };

  private String menuItemPrefix;

  @Inject private ISarosSessionManager sessionManager;

  @Inject private EditorManager editorManager;

  /**
   * Creates a Follow button with Popupmenu, registers sessionListeners and editorlisteners.
   *
   * <p>The FollowButton is created as dissabled.
   */
  public FollowButton() {
    super(FollowModeAction.NAME, "Follow", FOLLOW_ICON_PATH, "Enter follow mode");
    SarosPluginContext.initComponent(this);

    followModeAction = new FollowModeAction();

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);

    editorManager.addSharedEditorListener(editorListener);

    createMenu();
    setEnabled(false);

    final JButton button = this;
    addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent ev) {
            popupMenu.show(button, 0, button.getBounds().y + button.getBounds().height);
          }
        });
  }

  private void createMenu() {
    popupMenu = new JPopupMenu();

    menuItemPrefix = "Follow ";

    for (User user : followModeAction.getCurrentRemoteSessionUsers()) {
      JMenuItem menuItem = createItemForUser(user);
      popupMenu.add(menuItem);
    }

    popupMenu.addSeparator();

    JMenuItem leaveItem = new JMenuItem("Leave follow mode");
    leaveItem.setActionCommand(null);
    leaveItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            followModeAction.execute(e.getActionCommand());
          }
        });
    leaveItem.setEnabled(followModeAction.getCurrentlyFollowedUser() != null);

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

    User currentlyFollowedUser = followModeAction.getCurrentlyFollowedUser();
    if (currentlyFollowedUser != null) {
      String currentUserName = ModelFormatUtils.getDisplayName(currentlyFollowedUser);
      if (currentUserName.equalsIgnoreCase(userNameShort)) {
        menuItem.setEnabled(false);
      }
    }

    menuItem.setActionCommand(userName);
    menuItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            followModeAction.execute(e.getActionCommand());
          }
        });
    return menuItem;
  }

  private void updateMenu() {
    UIUtil.invokeAndWaitIfNeeded(
        new Runnable() {
          @Override
          public void run() {
            createMenu();
          }
        });
  }
}
