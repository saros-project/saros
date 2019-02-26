package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.io.IOException;
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

  @Override
  public IFolder getFolder(IReferencePoint referencePoint, String name) {
    IProject project = getProject(referencePoint);

    return project.getFolder(name);
  }

  @Override
  public IFile getFile(IReferencePoint referencePoint, String name) {
    IProject project = getProject(referencePoint);

    return project.getFile(name);
  }

  @Override
  public boolean projectExists(IReferencePoint referencePoint) {
    IProject project = getProject(referencePoint);

    return project.exists();
  }

  @Override
  public String getName(IReferencePoint referencePoint) {
    IProject project = getProject(referencePoint);

    return project.getName();
  }

  @Override
  public String getDefaultCharSet(IReferencePoint referencePoint) throws IOException {
    IProject project = getProject(referencePoint);

    return project.getDefaultCharset();
  }

  @Override
  public IResource[] members(IReferencePoint referencePoint) throws IOException {
    IProject project = getProject(referencePoint);

    return project.members();
  }

  @Override
  public SPath createSPath(IReferencePoint referencePoint, IPath projectRelativePath) {
    IProject project = getProject(referencePoint);

    return new SPath(project, projectRelativePath);
  }

  private IProject getProject(IReferencePoint referencePoint) {
    if (referencePoint == null) throw new IllegalArgumentException("ReferencePoint is null");

    IProject project = get(referencePoint);

    if (project == null)
      throw new NullPointerException("Cannot find project for reference point " + referencePoint);

    return project;
  }
}
