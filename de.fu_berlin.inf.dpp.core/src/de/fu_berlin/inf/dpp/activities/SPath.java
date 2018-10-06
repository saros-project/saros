package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.misc.xstream.SPathConverter;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;

/**
 * A <i>SPath</i> points to a {@link IResource resource} in a
 * {@link IReferencePoint project}. The specific resource does not need to
 * exist, neither during the marshaling nor during unmarshaling.
 * <p>
 * <i>SPath</i> objects can be marshaled and unmarshaled.
 * 
 * @see SPathConverter
 */

@XStreamAlias("SPath")
public class SPath {

    /**
     * @JTourBusStop 4, Some Basics:
     * 
     *               Individual IProjects use IPaths to identify their
     *               resources. However, because Saros needs to keep track of
     *               resources across multiple projects, it encapsulates IPaths
     *               in an SPath that includes additional identifying
     *               information.
     */

    private IReferencePointManager referencePointManager;

    /**
     * The local IReferencePoint on which the resource referenced to which this
     * SPath represents
     */
    private IReferencePoint referencePoint;

    /**
     * The relative path of the resource from the referencePoint this SPath
     * represents.
     */
    private final IPath relativePathFromReferencePoint;

    /**
     * Default constructor, initializing this SPath as a reference to the
     * resource identified by the given path in the given referencePoint.
     * 
     * 
     * @throws IllegalArgumentException
     *             if referencePoint is <code>null</code><br>
     * @throws IllegalArgumentException
     *             if referencePointManager is <code>null</code><br>
     * @throws IllegalArgumentException
     *             if the path is <code>null</code> or is not relative
     */
    public SPath(IReferencePoint referencePoint, IPath path,
        IReferencePointManager referencePointManager) {
        if (referencePoint == null)
            throw new IllegalArgumentException("project is null");

        if (path == null)
            throw new IllegalArgumentException("path is null");

        if (path.isAbsolute())
            throw new IllegalArgumentException("path is absolute: " + path);

        if (referencePointManager == null)
            throw new IllegalArgumentException("referencePointManager is null");

        this.referencePoint = referencePoint;
        this.referencePointManager = referencePointManager;
        this.relativePathFromReferencePoint = path;
    }

    /**
     * Convenience constructor, which retrieves path and referencePoint from the
     * given resource
     * 
     */
    public SPath(IResource resource,
        IReferencePointManager referencePointmanager) {
        this(resource.getProject().getReferencePoint(), resource
            .getProjectRelativePath(), referencePointmanager);

    }

    /**
     * Returns the relative path of the resource from the referencePoint
     * represented by this SPath.
     * 
     * @return relative path of the resource from the referencePoint
     */
    public IPath getRelativePathFromReferencePoint() {
        return relativePathFromReferencePoint;
    }

    /**
     * Returns a handle for an IFile represented by this SPath.
     * 
     * @return the IFile contained in the associated referencePoint for the
     *         given relative path from referencePoint
     */
    public IFile getFile() {
        return referencePointManager.getFile(referencePoint,
            relativePathFromReferencePoint);
    }

    /**
     * Returns the IResource represented by this SPath.
     * <p>
     * <b>Note:</b> This operation might perform disk I/O.
     * 
     * @return the resource represented by this SPath or <code>null</code> if
     *         such or resource does not exist
     */
    public IResource getResource() {
        return referencePointManager.findMember(referencePoint,
            relativePathFromReferencePoint);
    }

    /**
     * Returns a handle for an IFolder represented by this SPath.
     * 
     * @return the IFolder contained in the associated referencePoint for the
     *         given relative path from referencePoint
     * 
     */
    public IFolder getFolder() {
        return referencePointManager.getFolder(referencePoint,
            relativePathFromReferencePoint);
    }

    /**
     * Returns the project in which the referenced resource is located.
     */
    public IProject getProject() {
        return referencePointManager.get(referencePoint);
    }

    /**
     * Returns the referencePoint in which the resource referenced to.
     */
    public IReferencePoint getReferencePoint() {
        return referencePoint;
    }

    /**
     * Convenience method for getting the full path of the file identified by
     * this SPath.
     */
    public IPath getFullPath() {
        final IPath pathToReferencePoint = referencePointManager.get(
            referencePoint).getFullPath();
        return pathToReferencePoint.append(relativePathFromReferencePoint);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ObjectUtils.hashCode(referencePoint);
        result = prime * result
            + ObjectUtils.hashCode(relativePathFromReferencePoint);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (!(obj instanceof SPath))
            return false;

        SPath other = (SPath) obj;

        return ObjectUtils.equals(referencePoint, other.referencePoint)
            && ObjectUtils.equals(relativePathFromReferencePoint,
                other.relativePathFromReferencePoint);
    }

    @Override
    public String toString() {
        return "SPath [referencePoint=" + referencePoint
            + ", projectRelativePath=" + relativePathFromReferencePoint + "]";
    }
}
