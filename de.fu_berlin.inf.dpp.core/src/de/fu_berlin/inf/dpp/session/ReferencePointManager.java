package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** The implementation of {@link IReferencePointManager} */
public class ReferencePointManager implements IReferencePointManager {

  private final ConcurrentHashMap<IReferencePoint, IFolder> referencePointToProjectMapper;

  public ReferencePointManager() {
    referencePointToProjectMapper = new ConcurrentHashMap<IReferencePoint, IFolder>();
  }

  @Override
  public void putIfAbsent(IReferencePoint referencePoint, IFolder project) {
    if (!referencePointToProjectMapper.containsKey(referencePoint)) {
      referencePointToProjectMapper.put(referencePoint, project);
    }
  }

  @Override
  public IFolder get(IReferencePoint referencePoint) {
    return referencePointToProjectMapper.get(referencePoint);
  }

  @Override
  public Set<IFolder> getProjects(Set<IReferencePoint> referencePoints) {
    Set<IFolder> projectSet = new HashSet<IFolder>();

    for (IReferencePoint referencePoint : referencePoints) {
      if (referencePointToProjectMapper.containsKey(referencePoint))
        projectSet.add(get(referencePoint));
    }

    return projectSet;
  }
}
