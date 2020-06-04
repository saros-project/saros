package saros.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IResource;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.net.xmpp.JID;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.ICollaborationUtils;

/** This delegates the {@link ICollaborationUtils} methods the actual eclipse implementation. */
// TODO: make CollaborationUtils non static and let it implement the interface
// directly
public class EclipseCollaborationUtilsImpl implements ICollaborationUtils {

  @Override
  public void startSession(Set<IReferencePoint> referencePoints, List<JID> contacts) {
    CollaborationUtils.startSession(getDelegates(referencePoints), contacts);
  }

  @Override
  public void leaveSession() {
    CollaborationUtils.leaveSession();
  }

  @Override
  public void addReferencePointsToSession(Set<IReferencePoint> referencePoints) {
    CollaborationUtils.addResourcesToSession(getDelegates(referencePoints));
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }

  private List<IResource> getDelegates(Set<IReferencePoint> referencePoints) {
    if (referencePoints == null) {
      return null;
    }

    return referencePoints
        .stream()
        .map(ResourceConverter::getDelegate)
        .collect(Collectors.toList());
  }
}
