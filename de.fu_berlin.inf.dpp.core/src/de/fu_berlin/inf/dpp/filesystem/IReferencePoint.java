package de.fu_berlin.inf.dpp.filesystem;

/**
 * The IReferencePoint represents the absolute path of the root of the shared
 * subtree of {@link IResource}s. For IDEs the root of the subtree is a
 * container, on which {@link IResource}, like {@link IFolder} and {@link IFile}
 * , is accessible via the relative path from IReferencePoint to
 * {@link IResource}.
 * 
 * This interface is under development: The IReferencePoint is the absolute path
 * of {@link IProject}
 * 
 */
public interface IReferencePoint {

    /*
     * Nothing here
     */
}
