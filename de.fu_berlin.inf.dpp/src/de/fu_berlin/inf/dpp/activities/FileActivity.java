package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("fileActivity")
public class FileActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed, Moved
    }

    @XStreamAsAttribute
    private final Type type;

    @XStreamAsAttribute
    private final IPath path;

    @XStreamAsAttribute
    private IPath oldPath;

    @XStreamOmitField
    private InputStream inputStream;

    public FileActivity(String source, Type type, IPath path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    /**
     * Constructor for {@link FileActivity}s with incoming file data.
     * 
     * @param source
     *            of the file data.
     * @param path
     *            where to save the data.
     * @param in
     *            data stream.
     */
    public FileActivity(String source, IPath path, InputStream in) {
        this(source, Type.Created, path);
        this.inputStream = in;
    }

    /**
     * Constructor for moved files.
     * 
     * @param oldPath
     *            path where the file moved from
     * @param newPath
     *            path where the file moved to
     */
    public FileActivity(String source, IPath oldPath, IPath newPath) {
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

    /**
     * @return the inputStream for the contents of this file for incoming file
     *         creation activities. <code>null</code> otherwise.
     */
    public InputStream getContents() {
        return this.inputStream;
    }

    @Override
    public String toString() {
        if (type == Type.Moved)
            return "FileActivity(type: Moved, old path: " + this.oldPath
                + ", new path: " + this.path + ")";
        return "FileActivity(type: " + this.type + ", path: " + this.path + ")";
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
        FileActivity other = (FileActivity) obj;
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

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }
}
