package saros.util;

import java.util.List;
import saros.filesystem.IResource;
import saros.filesystem.ResourceAdapterFactory;
import saros.net.xmpp.JID;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.ICollaborationUtils;

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
