package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IFolder_V2;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** The implementation of {@link IReferencePointManager} */
public class ReferencePointManager implements IReferencePointManager {

  private final HashMap<IReferencePoint, IFolder_V2> referencePointToProjectMapper;

  public ReferencePointManager() {
    referencePointToProjectMapper = new HashMap<IReferencePoint, IFolder_V2>();
  }

  @Override
  public synchronized void put(IReferencePoint referencePoint, IFolder_V2 project) {
    if (!referencePointToProjectMapper.containsKey(referencePoint)) {
      referencePointToProjectMapper.put(referencePoint, project);
    }
  }

  @Override
  public synchronized IFolder_V2 get(IReferencePoint referencePoint) {
    return referencePointToProjectMapper.get(referencePoint);
  }

  @Override
  public synchronized Set<IFolder_V2> getProjects(Set<IReferencePoint> referencePoints) {
    Set<IFolder_V2> projectSet = new HashSet<IFolder_V2>();

    for (IReferencePoint referencePoint : referencePoints) {
      if (referencePointToProjectMapper.containsKey(referencePoint))
        projectSet.add(get(referencePoint));
    }

    return projectSet;
  }
}
