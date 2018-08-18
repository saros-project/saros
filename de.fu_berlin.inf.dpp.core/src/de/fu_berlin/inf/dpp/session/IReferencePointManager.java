package de.fu_berlin.inf.dpp.session;

import java.util.Set;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;

/**
 * The IReferencePointManager maps an IReferencePoint to IProject
 */
public interface IReferencePointManager {

    /**
     * Insert a pair of reference point and project
     * 
     * @param referencePoint
     *            the key of the pair
     * @param project
     *            the value of the pair
     */
    void put(IReferencePoint referencePoint, IProject project);

    /**
     * Returns the IProject given by the IReferencePoint
     * 
     * @param referencePoint
     *            the key for which the IProject should be returned
     * @return the IProject given by referencePoint
     */
    IProject get(IReferencePoint referencePoint);

    /**
     * Returns a set of IProjects given by a set of IReferencePoints
     * 
     * @param referencePoints
     *            a set of referencePoints for which the set of IProjects should
     *            returned
     * @return a set of IProject given by referencePoint
     */
    Set<IProject> getProjects(Set<IReferencePoint> referencePoints);
}