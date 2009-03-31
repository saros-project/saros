package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.util.Util;

public class FileActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed
    }

    private final Type type;

    private final IPath path;

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

    public IPath getPath() {
        return this.path;
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
        return "FileActivity(type:" + this.type + ",path:" + this.path + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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
        if (!(obj instanceof FileActivity))
            return false;
        FileActivity other = (FileActivity) obj;
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

    public void toXML(StringBuilder sb) {
        sb.append("<file ");
        sourceToXML(sb);
        sb.append("path=\"").append(
            Util.urlEscape(getPath().toPortableString())).append("\" ");
        sb.append("type=\"").append(getType()).append("\" ");
        sb.append("/>");
    }
}
