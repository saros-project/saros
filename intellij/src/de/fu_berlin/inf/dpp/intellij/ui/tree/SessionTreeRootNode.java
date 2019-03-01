package de.fu_berlin.inf.dpp.intellij.ui.tree;

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.picocontainer.annotations.Inject;

/** Session tree root node. */
public class SessionTreeRootNode extends DefaultMutableTreeNode {
  public static final String TREE_TITLE = "Session";
  public static final String TREE_TITLE_NO_SESSIONS = "No Session Running";

  private final SessionAndContactsTreeView treeView;
  private final Map<ISarosSession, DefaultMutableTreeNode> sessionNodeList =
      new HashMap<ISarosSession, DefaultMutableTreeNode>();
  private final Map<User, DefaultMutableTreeNode> userNodeList =
      new HashMap<User, DefaultMutableTreeNode>();
  private final DefaultTreeModel treeModel;

  @Inject private ISarosSessionManager sessionManager;

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
        public void resourcesAdded(final IProject project) {
          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  addProjectNode(project);
                }
              });
        }
      };

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession newSarosSession) {
          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  newSarosSession.addListener(sessionListener);
                  createSessionNode(newSarosSession);
                }
              });
        }

        @Override
        public void sessionEnded(final ISarosSession oldSarosSession, SessionEndReason reason) {

          UIUtil.invokeLaterIfNeeded(
              new Runnable() {
                @Override
                public void run() {
                  oldSarosSession.removeListener(sessionListener);
                  removeSessionNode(oldSarosSession);
                }
              });
        }
      };

  public SessionTreeRootNode(SessionAndContactsTreeView treeView) {
    super(treeView);
    SarosPluginContext.initComponent(this);
    this.treeView = treeView;
    treeModel = (DefaultTreeModel) this.treeView.getModel();
    setUserObject(TREE_TITLE_NO_SESSIONS);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  private void createSessionNode(ISarosSession newSarosSession) {

    DefaultMutableTreeNode newSession =
        new DefaultMutableTreeNode(new SessionInfo(newSarosSession));

    sessionNodeList.put(newSarosSession, newSession);

    treeModel.insertNodeInto(newSession, this, getChildCount());
    treeModel.reload(this);

    add(newSession);

    setUserObject(TREE_TITLE);

    // userJoined is not fired for the host
    if (!newSarosSession.isHost()) {
      addUserNode(newSarosSession.getHost());
    }

    addUserNode(newSarosSession.getLocalUser());

    treeView.expandRow(1);
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

    treeModel.reload(this);
    treeView.expandRow(2);
  }

  private void addProjectNode(IProject project) {
    for (DefaultMutableTreeNode nSession : sessionNodeList.values()) {
      ISarosSession session = ((SessionInfo) nSession.getUserObject()).getSession();

      ProjectInfo projInfo;
      if (session.isCompletelyShared(project)) {
        projInfo = new ProjectInfo(project);
      } else {
        projInfo = new ProjectInfo(project, session.getSharedResources(project));
      }

      DefaultMutableTreeNode nProject = new DefaultMutableTreeNode(projInfo);
      treeModel.insertNodeInto(nProject, nSession, nSession.getChildCount());

      treeModel.reload(nSession);
    }
  }

  private void addUserNode(User user) {
    DefaultMutableTreeNode nUser = new DefaultMutableTreeNode(new UserInfo(user));
    userNodeList.put(user, nUser);
    treeModel.insertNodeInto(nUser, this, getChildCount());

    treeView.getContactTreeRootNode().hideContact(user.getJID());

    treeModel.reload(this);
  }

  private void removeUserNode(User user) {
    DefaultMutableTreeNode nUser = userNodeList.remove(user);

    if (nUser == null) return;

    remove(nUser);
    treeView.getContactTreeRootNode().showContact(user.getJID());
    treeModel.reload();
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
      super("Shared Modules and Projects");
      this.session = session;
    }

    public ISarosSession getSession() {
      return session;
    }
  }

  protected class UserInfo extends LeafInfo {
    private final User user;

    public UserInfo(User user) {
      super(
          (user.isHost() ? "Host " : "") + ModelFormatUtils.getDisplayName(user),
          IconManager.CONTACT_ONLINE_ICON);
      this.user = user;
    }

    public User getUser() {
      return user;
    }
  }

  protected class ProjectInfo extends LeafInfo {
    private final IProject project;
    private List<IResource> resList;

    public ProjectInfo(IProject project) {
      super(project.getName());
      this.project = project;
    }

    public ProjectInfo(IProject project, List<IResource> resources) {
      this(project);
      resList = resources;
    }

    public IProject getProject() {
      return project;
    }

    @Override
    public String toString() {
      if (resList != null) {
        StringBuilder sbOut = new StringBuilder();
        sbOut.append(project.getName());
        sbOut.append(" : ");
        for (IResource res : resList) {
          if (res.getType() == IResource.FILE) {
            sbOut.append(res.getName());
            sbOut.append("; ");
          }
        }

        return sbOut.toString();
      } else {
        return project.getName();
      }
    }
  }
}
