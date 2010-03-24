package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * IResourceDeltaVisitor which collects all changes as full paths to the
 * affected resource. Used for debugging.
 */
public class ToStringResourceDeltaVisitor implements IResourceDeltaVisitor {

    StringBuilder sb = new StringBuilder();

    public boolean visit(IResourceDelta delta) throws CoreException {

        switch (delta.getKind()) {

        case IResourceDelta.NO_CHANGE:
            return false;
        case IResourceDelta.CHANGED:
            sb.append("C ");
            break;
        case IResourceDelta.ADDED:
            sb.append("A ");
            break;
        case IResourceDelta.REMOVED:
            sb.append("R ");
            break;
        default:
            sb.append("? ");
        }
        IResource resource = delta.getResource();
        if (resource != null) {
            sb.append(resource.getFullPath().toPortableString());
        } else {
            sb.append("No resource");
        }
        sb.append("\n");

        return true;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}