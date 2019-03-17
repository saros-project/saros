package saros.filesystem;

import java.util.Set;
import saros.activities.SPath;

/**
 * The IReferencePointManager maps an IReferencePoint to IProject. It also provides the
 * functionality of IProject, which is needed in Saros Core. There are two different cases, which
 * the IReferencePointManager is needed. Components (Saros Core excluded), which still based on
 * IProject and it needs the IProject from the IReferencePoint. Components, which not based on
 * IProject anymore, have an IReferencePoint and needs the functionality from an IProject, like
 * getting the existing members within the IReferencePoint
 */
public interface IReferencePointManager {

  /**
   * Insert a pair of reference point and project, if the reference point has no mapping to the
   * project
   *
   * @param referencePoint the key of the pair
   * @param project the value of the pair
   */
  void putIfAbsent(IReferencePoint referencePoint, IProject project);

  /**
   * Insert a set of projects. The IReferencePointManager determinates the reference points of the
   * project automatically
   *
   * @param projects Set of project, which should be inserted
   */
  void putSetOfProjects(Set<IProject> projects);

  /**
   * Returns the IProject given by the IReferencePoint
   *
   * <p>ATTENTION: Don't use this in Saros Core, because Saros is in a huge refactoring process in
   * adjusting it to reference points. Components, which still not adjusted to reference points can
   * get the IProject given by an IReferencePoint
   *
   * @param referencePoint the key for which the IProject should be returned
   * @return the IProject given by referencePoint
   * @deprecated Don't use this in Saros Core, because the Core of Saros is in a refactoring process
   *     on which Saros is adjusted to reference points and the logic about project (especially
   *     {@link IProject}) will be removed.
   */
  @Deprecated
  IProject getProject(IReferencePoint referencePoint);

  /**
   * Returns a set of IProjects given by a set of IReferencePoints
   *
   * <p>ATTENTION: Don't use this in Saros Core, because Saros is in a huge refactoring process in
   * adjusting it to reference points. Components, which still not adjusted to reference points can
   * get the IProject given by an IReferencePoint
   *
   * @param referencePoints a set of referencePoints for which the set of IProjects should be
   *     returned
   * @return a set of IProject given by referencePoint
   * @deprecated Don't use this in Saros Core, because the Core of Saros is in a refactoring process
   *     on which Saros is adjusted to reference points and the logic about project (especially
   *     {@link IProject}) will be removed.
   */
  @Deprecated
  Set<IProject> getProjects(Set<IReferencePoint> referencePoints);

  /**
   * Returns a handle to the folder with given name.
   *
   * @param referencePoint The reference point on which the folder has to be determined
   * @param name Name of the folder
   * @return Returns a handle to the folder
   */
  IFolder getFolder(IReferencePoint referencePoint, String name);

  /**
   * Returns a handle to the file with given name.
   *
   * @param referencePoint The reference point on which the file has to be determined
   * @param name Name of the file
   * @return Returns a handle to the file
   */
  IFile getFile(IReferencePoint referencePoint, String name);

  /**
   * Checks if the reference point exists
   *
   * @param referencePoint The reference point of the corresponding project
   * @return True, if the reference point exists, otherwise false
   */
  boolean projectExists(IReferencePoint referencePoint);

  /**
   * Returns the name of the reference point
   *
   * @param referencePoint The reference point of the corresponding project
   * @return Returns the name of the reference point
   */
  String getName(IReferencePoint referencePoint);

  /**
   * Returns default charset of the reference point
   *
   * @param referencePoint The reference point of the corresponding project
   * @return default charset of the reference point, or null, when it was not possible to get the
   *     member resources
   */
  String getDefaultCharSet(IReferencePoint referencePoint);

  /**
   * Returns a list of existing member resources within the given reference point.
   *
   * @param referencePoint The reference point of the corresponding project
   * @return An array with existing member resources, or null, when it was not possible to get the
   *     member resources
   */
  IResource[] members(IReferencePoint referencePoint);

  /**
   * Creates and Returns a SPath given by the reference point and the relative path
   *
   * @param referencePoint The reference point of the corresponding project
   * @param projectRelativePath The relative path outgoing from the reference point
   * @return Returns a SPath
   */
  SPath createSPath(IReferencePoint referencePoint, IPath projectRelativePath);
}
