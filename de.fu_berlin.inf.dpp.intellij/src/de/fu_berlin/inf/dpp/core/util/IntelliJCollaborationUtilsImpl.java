package de.fu_berlin.inf.dpp.core.util;

import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;
import java.util.List;

/**
 * This delegates the {@link ICollaborationUtils} methods to the actual implementation in IntelliJ.
 */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class IntelliJCollaborationUtilsImpl implements ICollaborationUtils {

  @Override
  public void startSession(List<IResource> resources, List<JID> contacts) {
    CollaborationUtils.startSession(resources, contacts);
  }

  @Override
  public void leaveSession() {
    CollaborationUtils.leaveSession();
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
