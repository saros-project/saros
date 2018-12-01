package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** The implementation of {@link IReferencePointManager} */
public class ReferencePointManager implements IReferencePointManager {

  HashMap<IReferencePoint, IProject> referencePointToProjectMapper;

  public ReferencePointManager() {
    referencePointToProjectMapper = new HashMap<IReferencePoint, IProject>();
  }

  @Override
  public synchronized void put(IReferencePoint referencePoint, IProject project) {
    if (!referencePointToProjectMapper.containsKey(referencePoint)) {
      referencePointToProjectMapper.put(referencePoint, project);
    }
  }

  @Override
  public synchronized IProject get(IReferencePoint referencePoint) {
    return referencePointToProjectMapper.get(referencePoint);
  }

  @Override
  public synchronized Set<IProject> getProjects(Set<IReferencePoint> referencePoints) {
    Set<IProject> projectSet = new HashSet<IProject>();

    for (IReferencePoint referencePoint : referencePoints) {
      if (referencePointToProjectMapper.containsKey(referencePoint))
        projectSet.add(get(referencePoint));
    }

    return projectSet;
  }
}
