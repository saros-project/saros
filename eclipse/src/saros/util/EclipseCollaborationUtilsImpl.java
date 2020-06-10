package saros.util;

import java.util.List;
import java.util.Set;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceAdapterFactory;
import saros.net.xmpp.JID;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.ICollaborationUtils;

/** This delegates the {@link ICollaborationUtils} methods the actual eclipse implementation. */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class EclipseCollaborationUtilsImpl implements ICollaborationUtils {

  @Override
  public void startSession(Set<IReferencePoint> referencePoints, List<JID> contacts) {
    CollaborationUtils.startSession(ResourceAdapterFactory.convertBack(referencePoints), contacts);
  }

  @Override
  public void leaveSession() {
    CollaborationUtils.leaveSession();
  }

  @Override
  public void addReferencePointsToSession(Set<IReferencePoint> referencePoints) {
    CollaborationUtils.addResourcesToSession(ResourceAdapterFactory.convertBack(referencePoints));
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }
}
