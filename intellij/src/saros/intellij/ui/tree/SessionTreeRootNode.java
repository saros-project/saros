package saros.intellij.ui.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import saros.SarosPluginContext;
import saros.filesystem.IReferencePoint;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.views.SarosMainPanelView;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.util.CoreUtils;

/**
 * Session tree root node.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link SarosMainPanelView}.
 */
public class SessionTreeRootNode extends DefaultMutableTreeNode implements Disposable {
  public static final String TREE_TITLE = "Session";
  public static final String TREE_TITLE_NO_SESSIONS = "No Session Running";

  private final SessionAndContactsTreeView treeView;
  private final Map<ISarosSession, DefaultMutableTreeNode> sessionNodeList =
      new HashMap<ISarosSession, DefaultMutableTreeNode>();
  private final Map<User, DefaultMutableTreeNode> userNodeList =
      new HashMap<User, DefaultMutableTreeNode>();
  private final DefaultTreeModel treeModel;

  @Inject private ISarosSessionManager sessionManager;

  private volatile ISarosSession session;

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(final User user) {
          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  removeUserNode(user);
                }
              });
        }

        @Override
        public void userJoined(final User user) {
          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  addUserNode(user);
                }
              });
        }

        @Override
        public void resourcesAdded(final IReferencePoint referencePoint) {
          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  addReferencePointNode(referencePoint);
                }
              });
        }
      };

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession newSarosSession) {
          SessionTreeRootNode.this.sessionStarted(newSarosSession);
        }

        @Override
        public void sessionEnded(final ISarosSession oldSarosSession, SessionEndReason reason) {

          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  oldSarosSession.removeListener(sessionListener);
                  removeSessionNode(oldSarosSession);

                  session = null;
                }
              });
        }
      };

  public SessionTreeRootNode(SessionAndContactsTreeView treeView) {
    super(treeView);

    Disposer.register(treeView, this);

    SarosPluginContext.initComponent(this);
    this.treeView = treeView;
    treeModel = (DefaultTreeModel) this.treeView.getModel();
    setUserObject(TREE_TITLE_NO_SESSIONS);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);

    ISarosSession currentSession = session;

    if (currentSession != null) {
      currentSession.removeListener(sessionListener);
    }
  }

  /** Sets up the view in cases where there already is a running session when it is initialized. */
  void setInitialState() {
    ISarosSession session = sessionManager.getSession();

    if (session != null) {
      sessionStarted(session);
      populateInitialState();
    }
  }

  /** Adds components to session node that would otherwise be added by the state listeners. */
  private void populateInitialState() {
    session.getReferencePoints().forEach(this::addReferencePointNode);

    User host = session.getHost();
    User localUser = session.getLocalUser();

    session.getUsers().stream()
        .filter(user -> !user.equals(host) && !user.equals(localUser))
        .forEach(this::addUserNode);
  }

  private void sessionStarted(final ISarosSession newSarosSession) {
    session = newSarosSession;

    UIUtil.invokeLaterIfNeeded(
        () -> {
          newSarosSession.addListener(sessionListener);
          createSessionNode(newSarosSession);
        });
  }

  private void createSessionNode(ISarosSession newSarosSession) {

    DefaultMutableTreeNode newSession =
        new DefaultMutableTreeNode(new SessionInfo(newSarosSession));

    sessionNodeList.put(newSarosSession, newSession);

    treeModel.insertNodeInto(newSession, this, getChildCount());
    treeView.reloadModelNode(this);

    add(newSession);

    setUserObject(TREE_TITLE);

    // userJoined is not fired for the host
    if (!newSarosSession.isHost()) {
      addUserNode(newSarosSession.getHost());
    }

    addUserNode(newSarosSession.getLocalUser());

    treeView.expandPath(new TreePath(getPath()));
    treeView.expandPath(new TreePath(newSession.getPath()));
  }

  private void removeSessionNode(ISarosSession oldSarosSession) {

    DefaultMutableTreeNode nSession = sessionNodeList.get(oldSarosSession);
    if (nSession != null) {
      treeModel.removeNodeFromParent(nSession);
      sessionNodeList.remove(oldSarosSession);
      removeAllUserNodes();
    }

    if (sessionNodeList.isEmpty()) {
      setUserObject(TREE_TITLE_NO_SESSIONS);
    }

    treeView.reloadModelNode(this);
  }

  private void addReferencePointNode(IReferencePoint referencePoint) {
    for (DefaultMutableTreeNode sessionNode : sessionNodeList.values()) {
      ReferencePointInfo referencePointInfo = new ReferencePointInfo(referencePoint);

      DefaultMutableTreeNode referencePointNode = new DefaultMutableTreeNode(referencePointInfo);
      treeModel.insertNodeInto(referencePointNode, sessionNode, sessionNode.getChildCount());

      treeView.reloadModelNode(sessionNode);
    }
  }

  private void addUserNode(User user) {
    DefaultMutableTreeNode nUser = new DefaultMutableTreeNode(new UserInfo(user));
    userNodeList.put(user, nUser);
    treeModel.insertNodeInto(nUser, this, getChildCount());

    sortNodes();

    treeView.getContactTreeRootNode().hideContact(user.getJID());

    treeView.reloadModelNode(this);
  }

  /**
   * Sorts the nodes displayed in the session view.
   *
   * <p>Any non-user node displayed as part of the session tree is sorted to the front. The relative
   * position between such nodes is preserved.
   *
   * <p>Sorts the user nodes so that the session host is always displayed first. Sorts the remaining
   * user nodes alphabetically.
   */
  private void sortNodes() {
    ((Vector<?>) this.children)
        .sort(
            (Comparator<Object>)
                (o1, o2) -> {
                  DefaultMutableTreeNode n1 = (DefaultMutableTreeNode) o1;
                  DefaultMutableTreeNode n2 = (DefaultMutableTreeNode) o2;

                  Object u1 = n1.getUserObject();
                  Object u2 = n2.getUserObject();

                  if (u1 instanceof UserInfo && u2 instanceof UserInfo) {
                    UserInfo i1 = (UserInfo) n1.getUserObject();
                    UserInfo i2 = (UserInfo) n2.getUserObject();

                    if (i1.user.isHost()) {
                      return -1;
                    } else if (i2.user.isHost()) {
                      return 1;
                    }

                    return i1.title.compareTo(i2.title);

                  } else if (u1 instanceof UserInfo) {
                    return 1;

                  } else if (u2 instanceof UserInfo) {
                    return -1;

                  } else {
                    return 0;
                  }
                });
  }

  private void removeUserNode(User user) {
    DefaultMutableTreeNode nUser = userNodeList.remove(user);

    if (nUser == null) return;

    remove(nUser);
    treeView.getContactTreeRootNode().showContact(user.getJID());

    treeView.reloadModelNode(this);
  }

  private void removeAllUserNodes() {
    List<DefaultMutableTreeNode> userNodesToRemove = new ArrayList<>(userNodeList.values());

    for (DefaultMutableTreeNode userNode : userNodesToRemove)
      removeUserNode(((UserInfo) userNode.getUserObject()).getUser());

    assert userNodeList.isEmpty();
  }

  protected class SessionInfo extends LeafInfo {
    private final ISarosSession session;

    private SessionInfo(ISarosSession session) {
      super("Shared Root Directories");
      this.session = session;
    }

    public ISarosSession getSession() {
      return session;
    }
  }

  protected static class UserInfo extends LeafInfo {
    private final User user;

    public UserInfo(User user) {
      super(getUserNodeLabel(user), IconManager.CONTACT_ONLINE_ICON);
      this.user = user;
    }

    public User getUser() {
      return user;
    }

    /**
     * Returns the label to use for the node of the given user.
     *
     * @param user the user whose node label to return
     * @return the label to use for the node of the given user
     */
    private static String getUserNodeLabel(User user) {
      String userName = CoreUtils.determineUserDisplayName(user);

      if (!user.isHost()) {
        return userName;

      } else {
        return "<html><i><b>Host</b></i> " + userName + "</html>";
      }
    }
  }

  private static class ReferencePointInfo extends LeafInfo {
    private final IReferencePoint referencePoint;

    public ReferencePointInfo(IReferencePoint referencePoint) {
      super(referencePoint.getName());
      this.referencePoint = referencePoint;
    }

    public IReferencePoint getReferencePoint() {
      return referencePoint;
    }

    @Override
    public String toString() {
      return referencePoint.getName();
    }
  }
}
