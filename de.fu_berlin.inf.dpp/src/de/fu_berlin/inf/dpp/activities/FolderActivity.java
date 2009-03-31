package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.util.Util;

public class FolderActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed
    }

    private final Type type;

    private final IPath path;

    public FolderActivity(String source, Type type, IPath path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    public IPath getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "FolderActivity(type:" + this.type + ",path:" + this.path + ")";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public void toXML(StringBuilder sb) {
        sb.append("<folder ");
        sourceToXML(sb);
        sb.append("path=\"").append(
            Util.urlEscape(getPath().toPortableString())).append("\" ");
        sb.append("type=\"").append(getType()).append("\" ");
        sb.append("/>");
    }
}
