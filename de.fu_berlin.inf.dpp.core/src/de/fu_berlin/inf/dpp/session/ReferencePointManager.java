package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** The implementation of {@link IReferencePointManager} */
public class ReferencePointManager implements IReferencePointManager {

  private final HashMap<IReferencePoint, IFolder> referencePointToProjectMapper;

  public ReferencePointManager() {
    referencePointToProjectMapper = new HashMap<IReferencePoint, IFolder>();
  }

  @Override
  public synchronized void put(IReferencePoint referencePoint, IFolder project) {
    if (!referencePointToProjectMapper.containsKey(referencePoint)) {
      referencePointToProjectMapper.put(referencePoint, project);
    }
  }

  @Override
  public synchronized IFolder get(IReferencePoint referencePoint) {
    return referencePointToProjectMapper.get(referencePoint);
  }

  @Override
  public synchronized Set<IFolder> getProjects(Set<IReferencePoint> referencePoints) {
    Set<IFolder> projectSet = new HashSet<IFolder>();

    for (IReferencePoint referencePoint : referencePoints) {
      if (referencePointToProjectMapper.containsKey(referencePoint))
        projectSet.add(get(referencePoint));
    }

    return projectSet;
  }
}
