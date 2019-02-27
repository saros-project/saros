package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.io.IOException;
import java.util.Set;

/** The IReferencePointManager maps an IReferencePoint to IProject */
public interface IReferencePointManager {

  /**
   * Insert a pair of reference point and project
   *
   * @param referencePoint the key of the pair
   * @param project the value of the pair
   */
  void put(IReferencePoint referencePoint, IProject project);

  /**
   * ATTENTION: Don't use this in Saros Core! Returns the IProject given by the IReferencePoint
   *
   * @param referencePoint the key for which the IProject should be returned
   * @return the IProject given by referencePoint
   */
  @Deprecated
  IProject get(IReferencePoint referencePoint);

  /**
   * Returns a set of IProjects given by a set of IReferencePoints
   *
   * @param referencePoints a set of referencePoints for which the set of IProjects should returned
   * @return a set of IProject given by referencePoint
   */
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
   * @param referencePoint
   * @return True, if the reference point exists, otherwise false
   */
  boolean projectExists(IReferencePoint referencePoint);

  /**
   * Returns the name of the reference point
   *
   * @param referencePoint
   * @return Returns the name of the reference point
   */
  String getName(IReferencePoint referencePoint);

  /**
   * Returns default charset of the reference point
   *
   * @param referencePoint
   * @return default charset of the reference point
   */
  String getDefaultCharSet(IReferencePoint referencePoint) throws IOException;

  /**
   * Returns a list of existing member resources within the given reference point.
   *
   * @param referencePoint
   * @return An array with existing member resources
   * @throws IOException
   */
  IResource[] members(IReferencePoint referencePoint) throws IOException;

  /**
   * Creates and Returns a SPath given by the reference point and the relative path
   *
   * @param projectRelativePath
   * @return Returns a SPath
   */
  SPath createSPath(IReferencePoint referencePoint, IPath projectRelativePath);
}
