package saros.core.util;

import java.util.List;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IResource;
import saros.intellij.ui.util.UIProjectUtils;
import saros.net.xmpp.JID;
import saros.ui.util.ICollaborationUtils;

/**
 * This delegates the {@link ICollaborationUtils} methods to the actual implementation in IntelliJ.
 */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class IntelliJCollaborationUtilsImpl implements ICollaborationUtils {

  private final UIProjectUtils projectUtils;

  public IntelliJCollaborationUtilsImpl(UIProjectUtils projectUtils) {
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
  public void addResourcesToSession(List<IResource> resoruces) {
    CollaborationUtils.addResourcesToSession(resoruces);
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }
}
