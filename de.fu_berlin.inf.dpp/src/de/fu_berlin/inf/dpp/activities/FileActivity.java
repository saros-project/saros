package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

public class FileActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed
    }

    private final Type type;

    private final IPath path;

    private InputStream inputStream;

    public FileActivity(Type type, IPath path) {
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
        this(Type.Created, path);
        setSource(source);
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
    public boolean equals(Object obj) {
        if (obj instanceof FileActivity) {
            FileActivity activity = (FileActivity) obj;

            return (getPath().equals(activity.getPath()) && getType().equals(
                activity.getType()));
        }

        return false;
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public void toXML(StringBuilder sb) {
        sb.append("<file ");
        sb.append("path=\"").append(getPath()).append("\" ");
        sb.append("type=\"").append(getType()).append("\" ");
        sb.append("/>");
    }
}
