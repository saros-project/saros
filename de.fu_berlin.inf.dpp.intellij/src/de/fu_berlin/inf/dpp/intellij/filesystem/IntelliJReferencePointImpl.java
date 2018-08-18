package de.fu_berlin.inf.dpp.intellij.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;

public class IntelliJReferencePointImpl implements IReferencePoint {

    IntelliJPathImpl path;

    public IntelliJReferencePointImpl(IntelliJPathImpl path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntelliJReferencePointImpl other = (IntelliJReferencePointImpl) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}