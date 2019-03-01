package de.fu_berlin.inf.dpp.util;

import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;
import java.util.List;

/** This delegates the {@link ICollaborationUtils} methods the actual eclipse implementation. */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class EclipseCollaborationUtilsImpl implements ICollaborationUtils {

  @Override
  public void startSession(List<IResource> resources, List<JID> contacts) {
    CollaborationUtils.startSession(ResourceAdapterFactory.convertBack(resources), contacts);
  }

  @Override
  public void leaveSession() {
    CollaborationUtils.leaveSession();
  }

  @Override
  public void addResourcesToSession(List<IResource> resources) {
    CollaborationUtils.addResourcesToSession(ResourceAdapterFactory.convertBack(resources));
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }
}
