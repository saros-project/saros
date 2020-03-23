package saros.util;

import java.util.List;
import java.util.Set;
import saros.filesystem.IProject;
import saros.filesystem.ResourceAdapterFactory;
import saros.net.xmpp.JID;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.ICollaborationUtils;

/** This delegates the {@link ICollaborationUtils} methods the actual eclipse implementation. */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class EclipseCollaborationUtilsImpl implements ICollaborationUtils {

  @Override
  public void startSession(Set<IProject> projects, List<JID> contacts) {
    CollaborationUtils.startSession(ResourceAdapterFactory.convertBack(projects), contacts);
  }

  @Override
  public void leaveSession() {
    CollaborationUtils.leaveSession();
  }

  @Override
  public void addProjectsToSession(Set<IProject> projects) {
    CollaborationUtils.addResourcesToSession(ResourceAdapterFactory.convertBack(projects));
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }
}
