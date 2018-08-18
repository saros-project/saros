package de.fu_berlin.inf.dpp.session;

import java.util.Set;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;

public interface IReferencePointManager {

    void put(IReferencePoint referencePoint, IProject project);

    IProject get(IReferencePoint referencePoint);

    IFile getFile(IReferencePoint referencePoint, String relativePath);

    IFile getFile(IReferencePoint referencePoint, IPath relativePath);

    IFolder getFolder(IReferencePoint referencePoint, String relativePath);

    IFolder getFolder(IReferencePoint referencePoint, IPath relativePath);

    IResource findMember(IReferencePoint referencePoint, IPath relativePath);

    Set<IProject> getProjects(Set<IReferencePoint> referencePoints);
}