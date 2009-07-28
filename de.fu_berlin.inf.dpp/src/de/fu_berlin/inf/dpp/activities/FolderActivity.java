package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("folderActivity")
public class FolderActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed, Moved
    }

    @XStreamAsAttribute
    private final Type type;

    @XStreamAsAttribute
    private final IPath path;

    @XStreamAsAttribute
    private IPath oldPath;

    public FolderActivity(String source, Type type, IPath path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    /**
     * Constructor for moved folders.
     * 
     * @param oldPath
     *            path where the folder moved from
     * @param newPath
     *            path where the folder moved to
     */
    public FolderActivity(String source, IPath oldPath, IPath newPath) {
        this(source, Type.Moved, newPath);
        this.oldPath = oldPath;
    }

    public IPath getPath() {
        return this.path;
    }

    public IPath getOldPath() {
        return this.oldPath;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((oldPath == null) ? 0 : oldPath.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FolderActivity other = (FolderActivity) obj;
        if (oldPath == null) {
            if (other.oldPath != null)
                return false;
        } else if (!oldPath.equals(other.oldPath))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (type == Type.Moved)
            return "FolderActivity(type: Moved, old path: " + this.oldPath
                + ", new path: " + this.path + ")";
        return "FolderActivity(type: " + this.type + ", path: " + this.path
            + ")";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }
}
