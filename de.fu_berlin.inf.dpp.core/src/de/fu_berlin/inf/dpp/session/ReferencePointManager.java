package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** The implementation of {@link IReferencePointManager} */
public class ReferencePointManager implements IReferencePointManager {

  ConcurrentHashMap<IReferencePoint, IProject> referencePointToProjectMapper;

  public ReferencePointManager() {
    referencePointToProjectMapper = new ConcurrentHashMap<>();
  }

  @Override
  public void put(IReferencePoint referencePoint, IProject project) {
    if (referencePoint == null) throw new IllegalArgumentException("ReferencePoint is null");

    if (project == null) throw new IllegalArgumentException("Project is null");

    if (!referencePointToProjectMapper.containsKey(referencePoint)) {
      referencePointToProjectMapper.put(referencePoint, project);
    }
  }

  @Override
  public IProject get(IReferencePoint referencePoint) {
    if (referencePoint == null) throw new IllegalArgumentException("ReferencePoint is null");

    return referencePointToProjectMapper.get(referencePoint);
  }

  @Override
  public Set<IProject> getProjects(Set<IReferencePoint> referencePoints) {
    if (referencePoints == null)
      throw new IllegalArgumentException("Set of reference points is null");

    Set<IProject> projectSet = new HashSet<IProject>();

    for (IReferencePoint referencePoint : referencePoints) {
      if (referencePointToProjectMapper.containsKey(referencePoint))
        projectSet.add(get(referencePoint));
    }

    return projectSet;
  }
}
