package de.fu_berlin.inf.dpp.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;

public class ReferencePointManager implements IReferencePointManager {

    HashMap<IReferencePoint, IProject> referencePointToProjectMapper;

    public ReferencePointManager() {
        referencePointToProjectMapper = new HashMap<IReferencePoint, IProject>();
    }

    @Override
    public synchronized void put(IReferencePoint referencePoint,
        IProject project) {
        if (!referencePointToProjectMapper.containsKey(referencePoint)) {
            referencePointToProjectMapper.put(referencePoint, project);
        }
    }

    @Override
    public synchronized IProject get(IReferencePoint referencePoint) {
        if (referencePointToProjectMapper.containsKey(referencePoint)) {
            return referencePointToProjectMapper.get(referencePoint);
        }

        return null;
    }

    @Override
    public synchronized IFile getFile(IReferencePoint referencePoint,
        String relativePath) {
        IProject project = get(referencePoint);
        return project.getFile(relativePath);
    }

    @Override
    public synchronized IFile getFile(IReferencePoint referencePoint,
        IPath relativePath) {
        IProject project = get(referencePoint);
        return project.getFile(relativePath);
    }

    @Override
    public synchronized IFolder getFolder(IReferencePoint referencePoint,
        String relativePath) {
        IProject project = get(referencePoint);
        return project.getFolder(relativePath);
    }

    @Override
    public synchronized IFolder getFolder(IReferencePoint referencePoint,
        IPath relativePath) {
        IProject project = get(referencePoint);
        return project.getFolder(relativePath);
    }

    @Override
    public synchronized IResource findMember(IReferencePoint referencePoint,
        IPath relativePath) {
        IProject project = get(referencePoint);
        return project.findMember(relativePath);
    }

    @Override
    public synchronized Set<IProject> getProjects(
        Set<IReferencePoint> referencePoints) {
        Set<IProject> projectSet = new HashSet<IProject>();
        for (IReferencePoint referencePoint : referencePoints) {
            if (referencePointToProjectMapper.containsKey(referencePoint))
                projectSet.add(get(referencePoint));
        }

        return projectSet;
    }

}