package saros.core.util;

import com.intellij.openapi.project.Project;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.filesystem.IntellijReferencePoint;
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
  public void startSession(Set<IReferencePoint> referencePoints, List<JID> contacts) {
    Set<Project> sharedProjects = new HashSet<>();

    for (IResource resource : referencePoints) {
      sharedProjects.add(((IntellijReferencePoint) resource.getReferencePoint()).getProject());
    }

    // Workaround while we only allow a single reference point (-> single project) to be shared
    if (sharedProjects.size() != 1) {
      throw new IllegalStateException(
          "Saros/I currently only supports sharing of a single project. Found projects: "
              + sharedProjects);
    }
    SharedIDEContext.preregisterProject(sharedProjects.toArray(new Project[1])[0]);

    CollaborationUtils.startSession(referencePoints, contacts);
  }

  @Override
  public void leaveSession() {
    projectUtils.runWithProject(CollaborationUtils::leaveSession);
  }

  @Override
  public void addReferencePointsToSession(Set<IReferencePoint> referencePoints) {
    CollaborationUtils.addResourcesToSession(referencePoints);
  }

  @Override
  public void addContactsToSession(List<JID> contacts) {
    CollaborationUtils.addContactsToSession(contacts);
  }
}
