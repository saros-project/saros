package saros.core.util;

import java.util.List;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IResource;
import saros.intellij.ui.util.UIProjectUtils;
import saros.net.xmpp.JID;
import saros.ui.util.ICollaborationUtils;

/**
 * This delegates the {@link ICollaborationUtils} methods to the actual implementation in Intellij.
 */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class IntellijCollaborationUtilsImpl implements ICollaborationUtils {

  private final UIProjectUtils projectUtils;

  public IntellijCollaborationUtilsImpl(UIProjectUtils projectUtils) {
    this.projectUtils = projectUtils;
  }

  @Override
  public void startSession(List<IResource> resources, List<JID> contacts) {
    CollaborationUtils.startSession(resources, contacts);
  }

  @Override
  public void leaveSession() {
    projectUtils.runWithProject(CollaborationUtils::leaveSession);
  }

  @Override
  public void addResourcesToSession(List<IResource> resources) {
    CollaborationUtils.addResourcesToSession(resources);
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }
}
