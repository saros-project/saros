package saros.filesystem;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import saros.activities.SPath;

/** The implementation of {@link IReferencePointManager} */
public class CoreReferencePointManager implements IReferencePointManager {

  private static final Logger log = Logger.getLogger(CoreReferencePointManager.class);

  private final ConcurrentHashMap<IReferencePoint, IProject> referencePointToProjectMapper;

  public CoreReferencePointManager() {
    referencePointToProjectMapper = new ConcurrentHashMap<>();
  }

  @Override
  public void put(IReferencePoint referencePoint, IProject project) {
    if (referencePoint == null) throw new IllegalArgumentException("ReferencePoint is null");

    if (project == null) throw new IllegalArgumentException("Project is null");

    referencePointToProjectMapper.putIfAbsent(referencePoint, project);
  }

  @Override
  public IProject getProject(IReferencePoint referencePoint) {
    if (referencePoint == null) throw new IllegalArgumentException("ReferencePoint is null");

    IProject project = referencePointToProjectMapper.get(referencePoint);

    if (project == null)
      throw new IllegalArgumentException(
          "Could not find a project mapping for the given reference point " + referencePoint);

    return project;
  }

  @Override
  public void putSetOfProjects(Set<IProject> projects) {
    if (projects == null) throw new IllegalArgumentException("Set of projects is null");

    for (IProject project : projects) {
      put(project.getReferencePoint(), project);
    }
  }

  @Override
  public Set<IProject> getProjects(Set<IReferencePoint> referencePoints) {
    if (referencePoints == null)
      throw new IllegalArgumentException("Set of reference points is null");

    Set<IProject> projectSet = new HashSet<>();

    for (IReferencePoint referencePoint : referencePoints) {
      if (referencePointToProjectMapper.containsKey(referencePoint))
        projectSet.add(getProject(referencePoint));
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
  public String getDefaultCharSet(IReferencePoint referencePoint) {
    IProject project = getProject(referencePoint);
    String charset = null;

    try {
      charset = project.getDefaultCharset();
    } catch (Exception e) {
      log.error(
          String.format(
              "It was not possible to get the default charset from project {0} on reference point {1}",
              referencePoint, project));
    } finally {
      return charset;
    }
  }

  @Override
  public IResource[] members(IReferencePoint referencePoint) {
    IProject project = getProject(referencePoint);
    IResource[] resources = new IResource[0];

    try {
      resources = project.members();
    } catch (Exception e) {
      log.error(
          String.format(
              "It was not possible to get the members from project %s on reference point %s",
              referencePoint, project));
    } finally {
      return resources;
    }
  }

  @Override
  public SPath createSPath(IReferencePoint referencePoint, IPath projectRelativePath) {
    IProject project = getProject(referencePoint);

    return new SPath(project, projectRelativePath);
  }
}
